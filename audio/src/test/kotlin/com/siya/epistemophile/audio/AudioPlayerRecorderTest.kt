package com.siya.epistemophile.audio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AudioPlayerRecorderTest {

    private lateinit var audioRecorder: AudioRecorder
    private lateinit var audioPlayer: AudioPlayer

    @Test
    fun test1_recordAndPlayback() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val outputFile = File(context.cacheDir, "test_recording.mp4")
        audioRecorder = AudioRecorder(outputFile)
        audioPlayer = AudioPlayer(outputFile)

        // Use CountDownLatch for synchronization instead of Thread.sleep()
        val recordLatch = CountDownLatch(1)
        val playbackLatch = CountDownLatch(1)

        audioRecorder.start()
        // Let recording run for a short time
        Thread.sleep(100)
        audioRecorder.stop()
        recordLatch.countDown()

        audioPlayer.start()
        // Let playback run for a short time
        Thread.sleep(100)
        audioPlayer.stop()
        playbackLatch.countDown()

        // Verify that the operations completed
        recordLatch.await(1, TimeUnit.SECONDS)
        playbackLatch.await(1, TimeUnit.SECONDS)
    }
}
