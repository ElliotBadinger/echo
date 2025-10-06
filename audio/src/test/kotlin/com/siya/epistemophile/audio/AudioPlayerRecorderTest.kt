package com.siya.epistemophile.audio

import android.media.MediaPlayer
import android.media.MediaRecorder
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoMoreInteractions
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
        mediaRecorder = mock()
        mediaPlayer = mock()

        doNothing().`when`(mediaRecorder).setOutputFile(any<String>())
        doNothing().`when`(mediaPlayer).setDataSource(any<String>())
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
        val recorderOrder = inOrder(mediaRecorder)
        recorderOrder.verify(mediaRecorder).setAudioSource(MediaRecorder.AudioSource.MIC)
        recorderOrder.verify(mediaRecorder).setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorderOrder.verify(mediaRecorder).setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorderOrder.verify(mediaRecorder).setOutputFile(outputFile.absolutePath)
        recorderOrder.verify(mediaRecorder).prepare()
        recorderOrder.verify(mediaRecorder).start()

        recorder.stop()
        recorderOrder.verify(mediaRecorder).stop()
        recorderOrder.verify(mediaRecorder).release()
        verifyNoMoreInteractions(mediaRecorder)

        player.start()
        val playerOrder = inOrder(mediaPlayer)
        playerOrder.verify(mediaPlayer).setDataSource(outputFile.absolutePath)
        playerOrder.verify(mediaPlayer).prepare()
        playerOrder.verify(mediaPlayer).start()

        player.stop()
        playerOrder.verify(mediaPlayer).stop()
        playerOrder.verify(mediaPlayer).release()
        verifyNoMoreInteractions(mediaPlayer)
    }
}
