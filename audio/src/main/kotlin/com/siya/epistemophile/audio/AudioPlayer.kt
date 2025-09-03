package com.siya.epistemophile.audio

import android.media.MediaPlayer
import java.io.File

class AudioPlayer(private val inputFile: File) {

    private var player: MediaPlayer? = null

    fun start() {
        try {
            player = MediaPlayer().apply {
                setDataSource(inputFile.absolutePath)
                prepare()
                start()
            }
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
