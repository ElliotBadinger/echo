package eu.mrogalski.saidit

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ActivityCompat
import com.siya.epistemophile.R
import eu.mrogalski.saidit.NotifyFileReceiver
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files

class SaidItService : Service() {
    companion object {
        private const val TAG = "SaidItService"
        private const val FOREGROUND_NOTIFICATION_ID = 458
        private const val YOUR_NOTIFICATION_CHANNEL_ID = "SaidItServiceChannel"
        private const val ACTION_AUTO_SAVE = "eu.mrogalski.saidit.ACTION_AUTO_SAVE"
        
        private const val STATE_READY = 0
        private const val STATE_LISTENING = 1
        private const val STATE_RECORDING = 2
    }

    @Volatile
    private var sampleRate: Int = 0
    @Volatile
    private var fillRate: Int = 0
    
    // Test environment flag
    var isTestEnvironment = false

    private var mediaFile: File? = null
    private var audioRecord: AudioRecord? = null
    private var aacWriter: AacMp4Writer? = null
    private val audioMemory = AudioMemory(SystemClockWrapper())

    // Coroutine scope for audio operations
    private val audioScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var audioJob: Job? = null
    
    @Volatile
    private var state = STATE_READY

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Reading native sample rate")

        val preferences = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
        sampleRate = preferences.getInt(SaidIt.SAMPLE_RATE_KEY, AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC))
        Log.d(TAG, "Sample rate: $sampleRate")
        fillRate = 2 * sampleRate

        if (preferences.getBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, true)) {
            innerStartListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording(null)
        innerStopListening()
        audioScope.cancel()
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder = BackgroundRecorderBinder()

    override fun onUnbind(intent: Intent): Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_AUTO_SAVE) {
            val preferences = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
            if (preferences.getBoolean("auto_save_enabled", false)) {
                Log.d(TAG, "Executing auto-save...")
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.US).format(java.util.Date())
                val autoName = "Auto-save_$timestamp"
                val autoSaveDurationSeconds = preferences.getInt("auto_save_duration", 300)
                dumpRecording(autoSaveDurationSeconds.toFloat(), NotifyFileReceiver(this), autoName)
            }
            return START_STICKY
        }
        startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        return START_STICKY
    }

    private fun innerStartListening() {
        if (state != STATE_READY) return
        state = STATE_LISTENING

        Log.d(TAG, "Starting listening")
        startService(Intent(this, this::class.java))
        val memorySize = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
            .getLong(SaidIt.AUDIO_MEMORY_SIZE_KEY, Runtime.getRuntime().maxMemory() / 4)

        audioJob = audioScope.launch {
            try {
                Log.d(TAG, "Executing: START LISTENING")
                @SuppressLint("MissingPermission")
                val newAudioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioMemory.CHUNK_SIZE
                )

                if (newAudioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "Audio: INITIALIZATION ERROR")
                    newAudioRecord.release()
                    state = STATE_READY
                    return@launch
                }

                audioRecord = newAudioRecord
                val allocateResult = audioMemory.allocate(memorySize)
                if (allocateResult.isFailure) {
                    Log.e(TAG, "Failed to allocate audio memory", allocateResult.exceptionOrNull())
                    newAudioRecord.release()
                    state = STATE_READY
                    return@launch
                }
                
                if (!isTestEnvironment) {
                    newAudioRecord.startRecording()
                    startAudioReading()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting listening", e)
                state = STATE_READY
            }
        }

        scheduleAutoSave()
    }

    private suspend fun startAudioReading() {
        val filler = AudioMemory.Consumer { array, offset, count ->
            audioRecord?.let { record ->
                val read = record.read(array, offset, count, AudioRecord.READ_NON_BLOCKING)
                if (read < 0) {
                    Log.e(TAG, "AUDIO RECORD ERROR: $read")
                    return@Consumer Result.success(0)
                }
                if (aacWriter != null && read > 0) {
                    aacWriter?.write(array, offset, read)
                }
                Result.success(read)
            } ?: Result.success(0)
        }

        while (currentCoroutineContext().isActive && state != STATE_READY) {
            val fillResult = audioMemory.fill(filler)
            if (fillResult.isFailure) {
                val errorMessage = getString(R.string.error_during_recording_into) + (mediaFile?.name ?: "")
                withContext(Dispatchers.Main) {
                    showToast(errorMessage)
                }
                Log.e(TAG, errorMessage, fillResult.exceptionOrNull())
                stopRecording(NotifyFileReceiver(this@SaidItService))
                break
            }
            delay(50) // ~50ms intervals
        }
    }

    private fun innerStopListening() {
        if (state == STATE_READY) return
        state = STATE_READY

        Log.d(TAG, "Stopping listening")
        cancelAutoSave()
        stopForeground(true)
        stopService(Intent(this, this::class.java))

        audioJob?.cancel()
        audioScope.launch {
            audioRecord?.let { record ->
                try {
                    record.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing audio record", e)
                }
            }
            audioRecord = null
            audioMemory.allocate(0) // Deallocate memory - ignore result as this is cleanup
        }
    }

    fun enableListening() {
        if (isTestEnvironment) {
            state = STATE_LISTENING
            return
        }
        getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
            .edit().putBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, true).apply()
        innerStartListening()
    }

    fun disableListening() {
        if (isTestEnvironment) {
            state = STATE_READY
            return
        }
        getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
            .edit().putBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, false).apply()
        innerStopListening()
    }

    fun startRecording(prependedMemorySeconds: Float) {
        if (state == STATE_RECORDING) return
        if (state == STATE_READY) innerStartListening()
        state = STATE_RECORDING

        audioScope.launch {
            flushAudioRecord()
            try {
                if (isTestEnvironment) {
                    mediaFile = null
                    aacWriter = null
                    return@launch
                }
                
                mediaFile = File.createTempFile("saidit", ".m4a", cacheDir)
                aacWriter = AacMp4Writer(sampleRate, 1, 96_000, mediaFile!!)
                Log.d(TAG, "Recording to: ${mediaFile!!.absolutePath}")

                if (prependedMemorySeconds > 0) {
                    val bytesPerSecond = (1f / getBytesToSeconds()).toInt()
                    val bytesToDump = (prependedMemorySeconds * bytesPerSecond).toInt()
                    audioMemory.dump(AudioMemory.LegacyConsumer { array, offset, count ->
                        aacWriter?.write(array, offset, count)
                        count
                    }, bytesToDump)
                }
            } catch (e: IOException) {
                Log.e(TAG, "ERROR creating AAC/MP4 file", e)
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.error_creating_recording_file))
                }
                state = STATE_LISTENING
            }
        }
    }

    fun stopRecording(wavFileReceiver: WavFileReceiver?) {
        if (state != STATE_RECORDING) return
        state = STATE_LISTENING

        audioScope.launch {
            flushAudioRecord()
            aacWriter?.let { writer ->
                try {
                    writer.close()
                } catch (e: IOException) {
                    Log.e(TAG, "CLOSING ERROR", e)
                }
            }
            
            if (wavFileReceiver != null && mediaFile != null) {
                saveFileToMediaStore(mediaFile!!, mediaFile!!.name, wavFileReceiver)
            }
            aacWriter = null
        }
    }

    fun dumpRecording(memorySeconds: Float, wavFileReceiver: WavFileReceiver?, newFileName: String?) {
        if (state == STATE_READY) return

        audioScope.launch {
            flushAudioRecord()
            var dumpFile: File? = null
            try {
                if (isTestEnvironment) {
                    withContext(Dispatchers.Main) {
                        wavFileReceiver?.onSuccess(Uri.EMPTY)
                    }
                    return@launch
                }
                
                val fileName = newFileName?.replace("[^a-zA-Z0-9.-]".toRegex(), "_") ?: "SaidIt_dump"
                dumpFile = File(cacheDir, "$fileName.m4a")
                val dumper = AacMp4Writer(sampleRate, 1, 96_000, dumpFile)
                Log.d(TAG, "Dumping to: ${dumpFile.absolutePath}")
                
                val bytesPerSecond = (1f / getBytesToSeconds()).toInt()
                val bytesToDump = (memorySeconds * bytesPerSecond).toInt()
                audioMemory.dump(AudioMemory.LegacyConsumer { array, offset, count ->
                    dumper.write(array, offset, count)
                    count
                }, bytesToDump)
                dumper.close()

                wavFileReceiver?.let { receiver ->
                    saveFileToMediaStore(dumpFile, (newFileName ?: "SaidIt Recording") + ".m4a", receiver)
                }
            } catch (e: IOException) {
                Log.e(TAG, "ERROR dumping AAC/MP4 file", e)
                withContext(Dispatchers.Main) {
                    showToast(getString(R.string.error_saving_recording))
                }
                dumpFile?.let { file ->
                    if (!file.delete()) {
                        Log.w(TAG, "Could not delete dump file: ${file.absolutePath}")
                    }
                }
                withContext(Dispatchers.Main) {
                    wavFileReceiver?.onFailure(e)
                }
            }
        }
    }

    fun scheduleAutoSave() {
        val preferences = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
        val autoSaveEnabled = preferences.getBoolean("auto_save_enabled", false)
        if (autoSaveEnabled) {
            val durationMillis = preferences.getInt("auto_save_duration", 600) * 1000L
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, SaidItService::class.java).apply {
                action = ACTION_AUTO_SAVE
            }
            val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            Log.d(TAG, "Scheduling auto-save for every ${durationMillis / 1000} seconds.")
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + durationMillis, durationMillis, pendingIntent)
        }
    }

    fun cancelAutoSave() {
        Log.d(TAG, "Cancelling auto-save.")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, SaidItService::class.java).apply {
            action = ACTION_AUTO_SAVE
        }
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    private suspend fun flushAudioRecord() {
        // Ensure any pending audio data is processed
        audioRecord?.let { record ->
            val buffer = ByteArray(AudioMemory.CHUNK_SIZE)
            while (record.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING) > 0) {
                // Drain buffer
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@SaidItService, message, Toast.LENGTH_LONG).show()
    }

    fun getMemorySize(): Long = audioMemory.getAllocatedMemorySize()

    fun setMemorySize(memorySize: Long) {
        val preferences = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
        preferences.edit().putLong(SaidIt.AUDIO_MEMORY_SIZE_KEY, memorySize).apply()

        if (preferences.getBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, true)) {
            audioScope.launch {
                val allocateResult = audioMemory.allocate(memorySize)
                if (allocateResult.isFailure) {
                    Log.e(TAG, "Failed to allocate audio memory for size $memorySize", allocateResult.exceptionOrNull())
                }
            }
        }
    }

    fun getSamplingRate(): Int = sampleRate

    fun setSampleRate(newSampleRate: Int) {
        if (state == STATE_RECORDING) return
        if (state == STATE_READY) {
            val preferences = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
            preferences.edit().putInt(SaidIt.SAMPLE_RATE_KEY, newSampleRate).apply()
            sampleRate = newSampleRate
            fillRate = 2 * sampleRate
            return
        }

        val preferences = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
        preferences.edit().putInt(SaidIt.SAMPLE_RATE_KEY, newSampleRate).apply()

        innerStopListening()
        sampleRate = newSampleRate
        fillRate = 2 * sampleRate
        innerStartListening()
    }

    fun getState(stateCallback: StateCallback) {
        val preferences = getSharedPreferences(SaidIt.PACKAGE_NAME, MODE_PRIVATE)
        val listeningEnabled = preferences.getBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, true)
        val recording = (state == STATE_RECORDING)
        
        audioScope.launch {
            flushAudioRecord()
            val stats = audioMemory.getStats(fillRate)

            var recorded = 0
            aacWriter?.let { writer ->
                recorded += writer.getTotalSampleBytesWritten()
                recorded += stats.estimation
            }
            
            val bytesToSeconds = getBytesToSeconds()
            withContext(Dispatchers.Main) {
                stateCallback.state(
                    listeningEnabled,
                    recording,
                    (if (stats.overwriting) stats.total else stats.filled + stats.estimation) * bytesToSeconds,
                    stats.total * bytesToSeconds,
                    recorded * bytesToSeconds
                )
            }
        }
    }

    fun getBytesToSeconds(): Float = 1f / fillRate

    private suspend fun saveFileToMediaStore(sourceFile: File, displayName: String, receiver: WavFileReceiver) {
        withContext(Dispatchers.IO) {
            val resolver = contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }

            val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = resolver.insert(collection, values)

            if (itemUri == null) {
                Log.e(TAG, "Error creating MediaStore entry.")
                withContext(Dispatchers.Main) {
                    receiver.onFailure(IOException("Failed to create MediaStore entry."))
                }
                return@withContext
            }

            try {
                Files.newInputStream(sourceFile.toPath()).use { input ->
                    resolver.openOutputStream(itemUri)?.use { output ->
                        input.copyTo(output)
                    } ?: throw IOException("Failed to open output stream for $itemUri")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error saving file to MediaStore", e)
                resolver.delete(itemUri, null, null)
                withContext(Dispatchers.Main) {
                    receiver.onFailure(e)
                }
                return@withContext
            } finally {
                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
                
                if (!sourceFile.delete()) {
                    Log.w(TAG, "Could not delete source file: ${sourceFile.absolutePath}")
                }
            }

            withContext(Dispatchers.Main) {
                receiver.onSuccess(itemUri)
            }
        }
    }

    private fun buildNotification(): Notification {
        val channel = NotificationChannel(YOUR_NOTIFICATION_CHANNEL_ID, "SaidIt Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notificationIntent = Intent(this, SaidItActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, YOUR_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(getText(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_hearing)
            .setContentIntent(pendingIntent)
            .build()
    }

    interface WavFileReceiver {
        fun onSuccess(fileUri: Uri)
        fun onFailure(e: Exception)
    }

    interface StateCallback {
        fun state(listeningEnabled: Boolean, recording: Boolean, memorized: Float, totalMemory: Float, recorded: Float)
    }

    inner class BackgroundRecorderBinder : Binder() {
        fun getService(): SaidItService = this@SaidItService
    }

    // NotifyFileReceiver has been moved to a separate file as a top-level class
}