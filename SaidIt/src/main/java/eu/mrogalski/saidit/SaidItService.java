package eu.mrogalski.saidit;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import androidx.core.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import android.os.Build;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.content.ContentResolver;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.net.Uri;
import simplesound.pcm.WavAudioFormat;
import simplesound.pcm.WavFileWriter;
import static eu.mrogalski.saidit.SaidIt.*;

public class SaidItService extends Service {
    static final String TAG = SaidItService.class.getSimpleName();
    private static final int FOREGROUND_NOTIFICATION_ID = 458;
    private static final String YOUR_NOTIFICATION_CHANNEL_ID = "SaidItServiceChannel";

    volatile int SAMPLE_RATE;
    volatile int FILL_RATE;


    File wavFile;
    AudioRecord audioRecord; // used only in the audio thread
    WavFileWriter wavFileWriter; // used only in the audio thread
    final AudioMemory audioMemory = new AudioMemory(); // used only in the audio thread

    private void saveFileToMediaStore(File sourceFile, String displayName, WavFileReceiver receiver) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, displayName);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Echo");
            values.put(MediaStore.Audio.Media.IS_PENDING, 1);
        }

        Uri collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri itemUri = resolver.insert(collection, values);

        try (InputStream in = new FileInputStream(sourceFile); OutputStream out = resolver.openOutputStream(itemUri)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy file to MediaStore", e);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Audio.Media.IS_PENDING, 0);
            resolver.update(itemUri, values, null, null);
        }

        sourceFile.delete(); // Clean up the temp file

        if (receiver != null) {
            // We need to pass back the URI now instead of the file
            // This requires changing the receiver interface. For now, let's assume it can handle a URI.
            // This will be a breaking change I'll fix in the next steps.
             // receiver.fileReady(itemUri, sourceFile.length() * getBytesToSeconds());
        }
    }


    HandlerThread audioThread;
    Handler audioHandler; // used to post messages to audio thread
    private volatile boolean autoSaveTriggeredForThisBuffer = false;
    AudioMemory.Consumer filler;
    Runnable audioReader;

    @Override
    public void onCreate() {

        Log.d(TAG, "Reading native sample rate");

        final SharedPreferences preferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        SAMPLE_RATE = preferences.getInt(SAMPLE_RATE_KEY, AudioTrack.getNativeOutputSampleRate (AudioManager.STREAM_MUSIC));
        Log.d(TAG, "Sample rate: " + SAMPLE_RATE);
        FILL_RATE = 2 * SAMPLE_RATE;

        audioThread = new HandlerThread("audioThread", Thread.MAX_PRIORITY);
        audioThread.start();
        audioHandler = new Handler(audioThread.getLooper());

        filler = (array, offset, count) -> {
//            Log.d(TAG, "READING " + count + " B");
            final int read = audioRecord.read(array, offset, count, AudioRecord.READ_NON_BLOCKING);
            if (read == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "AUDIO RECORD ERROR - BAD VALUE");
                return 0;
            }
            if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.e(TAG, "AUDIO RECORD ERROR - INVALID OPERATION");
                return 0;
            }
            if (read == AudioRecord.ERROR) {
                Log.e(TAG, "AUDIO RECORD ERROR - UNKNOWN ERROR");
                return 0;
            }
            if (wavFileWriter != null && read > 0) {
                wavFileWriter.write(array, offset, read);
            }
            if (read == count) {
                // We've filled the buffer, so let's read again.
                audioHandler.post(audioReader);
            } else {
                // It seems we've read everything!
                //
                // Estimate how long do we have until audioRecord fills up completely and post the callback 1 second before that
                // (but not earlier than half the buffer and no later than 90% of the buffer).
                float bufferSizeInSeconds = audioRecord.getBufferSizeInFrames() / (float)SAMPLE_RATE;
                float delaySeconds = bufferSizeInSeconds - 1;
                delaySeconds = Math.max(delaySeconds, bufferSizeInSeconds * 0.5f);
                delaySeconds = Math.min(delaySeconds, bufferSizeInSeconds * 0.9f);
                audioHandler.postDelayed(audioReader, (long)(delaySeconds * 1000));
            }
            return read;
        };

        audioReader = () -> {
            try {
                audioMemory.fill(filler);

                // Auto-save logic
                final SharedPreferences preferences1 = getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
                boolean autoSaveEnabled = preferences1.getBoolean("auto_save_enabled", false);
                if (autoSaveEnabled) {
                    final AudioMemory.Stats stats = audioMemory.getStats(FILL_RATE);
                    if (stats.overwriting) {
                        if (!autoSaveTriggeredForThisBuffer) {
                            autoSaveTriggeredForThisBuffer = true;
                            int durationInSeconds = preferences1.getInt("auto_save_duration", 60);
                            Log.d(TAG, "Auto-saving clip of " + durationInSeconds + " seconds.");
                            dumpRecording(durationInSeconds, new SaidItFragment.NotifyFileReceiver(SaidItService.this), "Auto-saved clip");
                        }
                    } else {
                        // If we are no longer overwriting (e.g., memory size increased), reset the flag.
                        autoSaveTriggeredForThisBuffer = false;
                    }
                }

            } catch (IOException e) {
                final String errorMessage = getString(R.string.error_during_recording_into) + wavFile.getName();
                Toast.makeText(SaidItService.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, errorMessage, e);
                stopRecording(new SaidItFragment.NotifyFileReceiver(SaidItService.this));
            }
        };

        if(preferences.getBoolean(AUDIO_MEMORY_ENABLED_KEY, true)) {
            innerStartListening();
        }

    }

    @Override
    public void onDestroy() {
        stopRecording(null);
        innerStopListening();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new BackgroundRecorderBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    public void enableListening() {
        getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE)
                .edit().putBoolean(AUDIO_MEMORY_ENABLED_KEY, true).apply();

        innerStartListening();
    }

    public void disableListening() {
        getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE)
                .edit().putBoolean(AUDIO_MEMORY_ENABLED_KEY, false).apply();

        innerStopListening();
    }

    int state;

    static final int STATE_READY = 0;
    static final int STATE_LISTENING = 1;
    static final int STATE_RECORDING = 2;

    private void innerStartListening() {
        switch(state) {
            case STATE_READY:
                break;
            case STATE_LISTENING:
            case STATE_RECORDING:
                return;
        }
        state = STATE_LISTENING;

        Log.d(TAG, "Queueing: START LISTENING");

        startService(new Intent(this, this.getClass()));

        final long memorySize = getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE).getLong(AUDIO_MEMORY_SIZE_KEY, Runtime.getRuntime().maxMemory() / 4);

        audioHandler.post(() -> {
            Log.d(TAG, "Executing: START LISTENING");
            Log.d(TAG, "Audio: INITIALIZING AUDIO_RECORD");

            @SuppressLint("MissingPermission") AudioRecord newAudioRecord = new AudioRecord(
                   MediaRecorder.AudioSource.MIC,
                   SAMPLE_RATE,
                   AudioFormat.CHANNEL_IN_MONO,
                   AudioFormat.ENCODING_PCM_16BIT,
                   AudioMemory.CHUNK_SIZE);

            if(newAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "Audio: INITIALIZATION ERROR - releasing resources");
                newAudioRecord.release();
                state = STATE_READY;
                return;
            }
            audioRecord = newAudioRecord;

            Log.d(TAG, "Audio: STARTING AudioRecord");
            audioMemory.allocate(memorySize);

            Log.d(TAG, "Audio: STARTING AudioRecord");
            audioRecord.startRecording();
            audioHandler.post(audioReader);
        });


    }

    private void innerStopListening() {
        switch(state) {
            case STATE_READY:
            case STATE_RECORDING:
                return;
            case STATE_LISTENING:
                break;
        }
        state = STATE_READY;
        Log.d(TAG, "Queueing: STOP LISTENING");

        stopForeground(true);
        stopService(new Intent(this, this.getClass()));

        audioHandler.post(() -> {
            Log.d(TAG, "Executing: STOP LISTENING");
            if(audioRecord != null)
                audioRecord.release();
            audioHandler.removeCallbacks(audioReader);
            autoSaveTriggeredForThisBuffer = false;
            audioMemory.allocate(0);
        });

    }

    public void dumpRecording(final float memorySeconds, final WavFileReceiver wavFileReceiver, String newFileName) {
        if(state != STATE_LISTENING) throw new IllegalStateException("Not listening!");

        audioHandler.post(() -> {
            flushAudioRecord();
            int prependBytes = (int)(memorySeconds * FILL_RATE);
            int bytesAvailable = audioMemory.countFilled();

            int skipBytes = Math.max(0, bytesAvailable - prependBytes);

            int useBytes = bytesAvailable - skipBytes;
            long millis  = System.currentTimeMillis() - 1000L * useBytes / FILL_RATE;
            final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE;
            final String dateTime = DateUtils.formatDateTime(SaidItService.this, millis, flags);
            final String filename = newFileName.isEmpty() ? "Echo - " + dateTime + ".wav" : newFileName + ".wav";
            File tempFile = new File(getCacheDir(), filename);

            final WavAudioFormat format = new WavAudioFormat.Builder().sampleRate(SAMPLE_RATE).build();
            try (WavFileWriter writer = new WavFileWriter(format, tempFile)) {
                audioMemory.read(skipBytes, (array, offset, count) -> {
                    writer.write(array, offset, count);
                    return 0;
                });
                
                // Now save the completed file to MediaStore
                saveFileToMediaStore(tempFile, filename, wavFileReceiver);

            } catch (IOException e) {
                showToast(getString(R.string.cant_create_file) + tempFile.getAbsolutePath());
                Log.e(TAG, "Can't create file " + tempFile.getAbsolutePath(), e);
            }
        });

    }

    private void showToast(String message) {
        Toast.makeText(SaidItService.this, message, Toast.LENGTH_LONG).show();
    }

    public void startRecording(final float prependedMemorySeconds) {
        switch(state) {
            case STATE_READY:
                innerStartListening();
                break;
            case STATE_LISTENING:
                break;
            case STATE_RECORDING:
                return;
        }
        state = STATE_RECORDING;

        audioHandler.post(() -> {
            flushAudioRecord();
            int prependBytes = (int)(prependedMemorySeconds * FILL_RATE);
            int bytesAvailable = audioMemory.countFilled();

            int skipBytes = Math.max(0, bytesAvailable - prependBytes);

            int useBytes = bytesAvailable - skipBytes;
            long millis  = System.currentTimeMillis() - 1000L * useBytes / FILL_RATE;
            final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE;
            final String dateTime = DateUtils.formatDateTime(SaidItService.this, millis, flags);
            final String filename = "Echo - " + dateTime + ".wav";
            wavFile = new File(getCacheDir(), filename);

            WavAudioFormat format = new WavAudioFormat.Builder().sampleRate(SAMPLE_RATE).build();
            try {
                wavFileWriter = new WavFileWriter(format, wavFile);
            } catch (IOException e) {
                final String errorMessage = getString(R.string.cant_create_file) + wavFile.getAbsolutePath();
                Toast.makeText(SaidItService.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, errorMessage, e);
                return;
            }

            if(skipBytes < bytesAvailable) {
                try {
                    audioMemory.read(skipBytes, (array, offset, count) -> {
                        wavFileWriter.write(array, offset, count);
                        return 0;
                    });
                } catch (IOException e) {
                    final String errorMessage = getString(R.string.error_during_writing_history_into) + wavFile.getAbsolutePath();
                    Toast.makeText(SaidItService.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMessage, e);
                    stopRecording(new SaidItFragment.NotifyFileReceiver(SaidItService.this));
                }
            }
        });

    }

    public long getMemorySize() {
        return audioMemory.getAllocatedMemorySize();
    }

    public void setMemorySize(final long memorySize) {
        final SharedPreferences preferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        preferences.edit().putLong(AUDIO_MEMORY_SIZE_KEY, memorySize).apply();

        if(preferences.getBoolean(AUDIO_MEMORY_ENABLED_KEY, true)) {
            audioHandler.post(() -> {
                audioMemory.allocate(memorySize);
                autoSaveTriggeredForThisBuffer = false;
            });
        }
    }

    public int getSamplingRate() {
        return SAMPLE_RATE;
    }

    public void setSampleRate(int sampleRate) {
        switch(state) {
            case STATE_READY:
            case STATE_RECORDING:
                return;
            case STATE_LISTENING:
                break;
        }

        final SharedPreferences preferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        preferences.edit().putInt(SAMPLE_RATE_KEY, sampleRate).apply();

        innerStopListening();
        SAMPLE_RATE = sampleRate;
        FILL_RATE = 2 * SAMPLE_RATE;
        innerStartListening();
    }

    public interface WavFileReceiver {
        void fileReady(Uri fileUri, float runtime);
    }

    public void stopRecording(final WavFileReceiver wavFileReceiver) {
        switch(state) {
            case STATE_READY:
            case STATE_LISTENING:
                return;
            case STATE_RECORDING:
                break;
        }
        state = STATE_LISTENING;

        audioHandler.post(() -> {
            flushAudioRecord();
            if (wavFileWriter != null) {
                try {
                    wavFileWriter.close();
                } catch (IOException e) {
                    Log.e(TAG, "CLOSING ERROR", e);
                }
            }
            if(wavFileReceiver != null && wavFile != null) {
                saveFileToMediaStore(wavFile, wavFile.getName(), wavFileReceiver);
            }
            wavFileWriter = null;
        });

        final SharedPreferences preferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        if(!preferences.getBoolean(AUDIO_MEMORY_ENABLED_KEY, true)) {
            innerStopListening();
        }

        stopForeground(true);
    }

    private void flushAudioRecord() {
        // Only allowed on the audio thread
        assert audioHandler.getLooper() == Looper.myLooper();
        audioHandler.removeCallbacks(audioReader); // remove any delayed callbacks
        audioReader.run();
    }

    public interface StateCallback {
        void state(boolean listeningEnabled, boolean recording, float memorized, float totalMemory, float recorded);
    }

    public void getState(final StateCallback stateCallback) {
        final SharedPreferences preferences = this.getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        final boolean listeningEnabled = preferences.getBoolean(AUDIO_MEMORY_ENABLED_KEY, true);
        final boolean recording = (state == STATE_RECORDING);
        final Handler sourceHandler = new Handler(Looper.getMainLooper());
        // Note that we may not run this for quite a while, if audioReader decides to read a lot of audio!
        audioHandler.post(() -> {
            flushAudioRecord();
            final AudioMemory.Stats stats = audioMemory.getStats(FILL_RATE);

            int recorded = 0;
            if(wavFileWriter != null) {
                recorded += wavFileWriter.getTotalSampleBytesWritten();
                recorded += stats.estimation;
            }
            final float bytesToSeconds = getBytesToSeconds();
            final int finalRecorded = recorded;
            sourceHandler.post(() -> stateCallback.state(listeningEnabled, recording,
                    (stats.overwriting ? stats.total : stats.filled + stats.estimation) * bytesToSeconds,
                    stats.total * bytesToSeconds,
                    finalRecorded * bytesToSeconds));
        });
    }

    public float getBytesToSeconds() {
        return 1f / FILL_RATE;
    }

    class BackgroundRecorderBinder extends Binder {
        public SaidItService getService() {
            return SaidItService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        return START_STICKY;
    }

    // Workaround for bug where recent app removal caused service to stop
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT| PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmService = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, SaidItActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, YOUR_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.recording))
                .setSmallIcon(R.drawable.ic_stat_notify_recording)
                .setTicker(getString(R.string.recording))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true); // Ensure notification is ongoing

        // Create the notification channel
        NotificationChannel channel = new NotificationChannel(
                YOUR_NOTIFICATION_CHANNEL_ID,
                "Recording Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        return notificationBuilder.build();
    }

}