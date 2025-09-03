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

        audioRecorder.start()
        Thread.sleep(1000)
        audioRecorder.stop()

        audioPlayer.start()
        Thread.sleep(1000)
        audioPlayer.stop()
    }
}
