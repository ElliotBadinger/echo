package com.siya.epistemophile.audio

import android.media.MediaPlayer
import android.media.MediaRecorder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class AudioPlayerRecorderTest {

    private lateinit var outputFile: File
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer

    @Before
    fun setUp() {
        outputFile = kotlin.io.path.createTempFile(prefix = "test_recording", suffix = ".mp4").toFile()
        mediaRecorder = mockk(relaxed = true)
        mediaPlayer = mockk(relaxed = true)

        every { mediaRecorder.setOutputFile(any<String>()) } returns Unit
        every { mediaPlayer.setDataSource(any<String>()) } returns Unit
    }

    @After
    fun tearDown() {
        outputFile.delete()
    }

    @Test
    fun `start and stop recorder and player`() {
        val recorder = AudioRecorder(outputFile) { mediaRecorder }
        val player = AudioPlayer(outputFile) { mediaPlayer }

        recorder.start()
        verifyOrder {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setOutputFile(outputFile.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()
        }

        recorder.stop()
        verifyOrder {
            mediaRecorder.stop()
            mediaRecorder.release()
        }
        confirmVerified(mediaRecorder)

        player.start()
        verifyOrder {
            mediaPlayer.setDataSource(outputFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
        }

        player.stop()
        verifyOrder {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        confirmVerified(mediaPlayer)
    }
}
