package eu.mrogalski.saidit

import android.app.AlarmManager
import android.content.Context
import android.net.Uri
import android.os.Build
import com.siya.epistemophile.audio.pcm.PcmAudioHelper
import com.siya.epistemophile.audio.pcm.WavAudioFormat
import com.siya.epistemophile.audio.pcm.WavFileWriter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.TemporaryFolder
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper
import java.io.File
import java.io.IOException
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class SaidItServiceTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @get:Rule @JvmField val tempDir = TemporaryFolder()

    @Test
    fun enableListening_reportsListeningStateViaCallback() = runTest(mainDispatcherRule.testDispatcher) {
        val controller = Robolectric.buildService(SaidItService::class.java)
        val service = controller.create().get()
        service.overrideAudioDispatcherForTest(mainDispatcherRule.testDispatcher)

        val memory = AudioMemory(FakeClock())
        service.overrideAudioMemoryForTest(memory)
        memory.allocate(AudioMemory.CHUNK_SIZE.toLong())
        fillAudioMemory(memory, generatePcmBytes(8_000, 1_600))

        service.isTestEnvironment = true
        service.enableListening()
        service.isTestEnvironment = false

        val captured = mutableListOf<ServiceState>()
        service.getState(object : SaidItService.StateCallback {
            override fun state(
                listeningEnabled: Boolean,
                recording: Boolean,
                bufferedSeconds: Float,
                totalSeconds: Float,
                recordedSeconds: Float
            ) {
                captured += ServiceState(listeningEnabled, recording, bufferedSeconds, totalSeconds, recordedSeconds)
            }
        })

        advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        val state = captured.single()
        assertTrue(state.listeningEnabled)
        assertFalse(state.recording)
        assertTrue(state.bufferedSeconds > 0f)
        assertTrue(state.totalSeconds > 0f)
        controller.destroy()
    }

    @Test
    fun dumpRecording_persistsPcmBuffersToCache() = runTest(mainDispatcherRule.testDispatcher) {
        val controller = Robolectric.buildService(SaidItService::class.java)
        val service = controller.create().get()
        service.overrideAudioDispatcherForTest(mainDispatcherRule.testDispatcher)

        val memory = AudioMemory(FakeClock())
        service.overrideAudioMemoryForTest(memory)
        memory.allocate(AudioMemory.CHUNK_SIZE.toLong())
        val pcmBytes = generatePcmBytes(8_000, 4_000)
        fillAudioMemory(memory, pcmBytes)

        service.setSampleRate(8_000)
        service.audioSampleWriterFactory = { _, _, _, file -> FileBackedSampleWriter(file) }
        service.isTestEnvironment = true
        service.enableListening()
        service.isTestEnvironment = false

        val bytesPerSecond = (1f / service.getBytesToSeconds()).toInt()
        val memorySeconds = pcmBytes.size.toFloat() / bytesPerSecond + 0.05f
        val fileName = "unit_test_dump"

        service.dumpRecording(memorySeconds, null, fileName)

        advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        val outputFile = File(service.cacheDir, "$fileName.m4a")
        assertTrue(outputFile.exists())
        val persisted = outputFile.readBytes()
        assertEquals(pcmBytes.size, persisted.size)
        assertTrue(pcmBytes.contentEquals(persisted))
        controller.destroy()
    }

    @Test
    fun dumpRecording_notifiesReceiverOnIoFailure() = runTest(mainDispatcherRule.testDispatcher) {
        val controller = Robolectric.buildService(SaidItService::class.java)
        val service = controller.create().get()
        service.overrideAudioDispatcherForTest(mainDispatcherRule.testDispatcher)

        val memory = AudioMemory(FakeClock())
        service.overrideAudioMemoryForTest(memory)
        memory.allocate(AudioMemory.CHUNK_SIZE.toLong())
        fillAudioMemory(memory, generatePcmBytes(8_000, 2_000))

        service.setSampleRate(8_000)
        val failure = IOException("expected failure")
        service.audioSampleWriterFactory = { _, _, _, _ -> FailingAudioSampleWriter(failure) }
        service.isTestEnvironment = true
        service.enableListening()
        service.isTestEnvironment = false

        var receivedError: Exception? = null
        val receiver = object : SaidItService.WavFileReceiver {
            override fun onSuccess(fileUri: Uri) = Unit
            override fun onFailure(e: Exception) { receivedError = e }
        }

        val bytesPerSecond = (1f / service.getBytesToSeconds()).toInt()
        val memorySeconds = 1.0f + (1f / bytesPerSecond)
        service.dumpRecording(memorySeconds, receiver, "failing_dump")

        advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        assertNotNull(receivedError)
        assertEquals(failure, receivedError)
        controller.destroy()
    }

    @Test
    fun scheduleAutoSave_registersRepeatingAlarm() {
        val controller = Robolectric.buildService(SaidItService::class.java)
        val service = controller.create().get()

        val prefs = service.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("auto_save_enabled", true)
            .putInt("auto_save_duration", 120)
            .apply()

        service.scheduleAutoSave()

        val alarmManager = service.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadow = shadowOf(alarmManager)
        val scheduled = shadow.nextScheduledAlarm
        assertNotNull("Alarm should be scheduled", scheduled)
        scheduled!!
        assertEquals(AlarmManager.ELAPSED_REALTIME_WAKEUP, scheduled.type)
        assertEquals(120_000L, scheduled.interval)
        val pendingShadow = shadowOf(scheduled.operation)
        assertEquals(SaidItService::class.java.name, pendingShadow.savedIntent.component?.className)
        controller.destroy()
    }

    @Test
    fun enableListening_allocationFailureRevertsToReadyState() = runTest(mainDispatcherRule.testDispatcher) {
        val controller = Robolectric.buildService(SaidItService::class.java)
        val service = controller.create().get()
        service.overrideAudioDispatcherForTest(mainDispatcherRule.testDispatcher)

        val failingMemory = object : AudioMemory(FakeClock()) {
            var attempts = 0
            override fun allocate(sizeToEnsure: Long): Result<Unit> {
                attempts++
                return Result.failure(AudioMemoryException("failed to allocate"))
            }
        }
        service.overrideAudioMemoryForTest(failingMemory)

        val initialState = service.stateForTest()
        service.isTestEnvironment = false
        service.enableListening()

        advanceUntilIdle()
        ShadowLooper.idleMainLooper()

        assertEquals(1, failingMemory.attempts)
        assertEquals(initialState, service.stateForTest())
        controller.destroy()
    }

    private fun generatePcmBytes(sampleRate: Int, sampleCount: Int): ByteArray {
        val wavFormat = WavAudioFormat.mono16Bit(sampleRate)
        val samples = IntArray(sampleCount) { index ->
            (Short.MAX_VALUE * sin(2.0 * PI * index / sampleCount)).toInt()
        }
        val wavFile = tempDir.newFile("input_${sampleRate}_$sampleCount.wav")
        WavFileWriter(wavFormat, wavFile).use { writer -> writer.write(samples) }
        val rawFile = tempDir.newFile("input_${sampleRate}_$sampleCount.pcm")
        PcmAudioHelper.convertWavToRaw(wavFile, rawFile)
        return rawFile.readBytes()
    }

    private fun fillAudioMemory(memory: AudioMemory, pcmBytes: ByteArray) {
        var offset = 0
        memory.fill(AudioMemory.Consumer { array, arrayOffset, available ->
            if (offset >= pcmBytes.size) {
                return@Consumer Result.success(0)
            }
            val toCopy = minOf(available, pcmBytes.size - offset)
            System.arraycopy(pcmBytes, offset, array, arrayOffset, toCopy)
            offset += toCopy
            Result.success(toCopy)
        }).getOrThrow()
    }

    private data class ServiceState(
        val listeningEnabled: Boolean,
        val recording: Boolean,
        val bufferedSeconds: Float,
        val totalSeconds: Float,
        val recordedSeconds: Float
    )
}
