package com.siya.epistemophile.audio

import android.media.MediaPlayer
import java.io.File

class AudioPlayer(
    private val inputFile: File,
    private val playerProvider: () -> MediaPlayer = { MediaPlayer() }
) {

    private var player: MediaPlayer? = null

    fun start() {
        try {
            val instance = playerProvider().apply {
                setDataSource(inputFile.absolutePath)
                prepare()
                start()
            }
            player = instance
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        player?.apply {
            stop()
            release()
        }
        player = null
    }
}
