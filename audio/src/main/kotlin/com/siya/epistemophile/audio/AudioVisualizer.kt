package com.siya.epistemophile.audio

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioVisualizer {

    private var visualizer: Visualizer? = null
    private val _amplitude = MutableStateFlow(0)
    val amplitude: Flow<Int> = _amplitude.asStateFlow()

    fun start(audioSessionId: Int) {
        visualizer = Visualizer(audioSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer?,
                    waveform: ByteArray?,
                    samplingRate: Int
                ) {
                    if (waveform != null) {
                        _amplitude.value = waveform.map { it.toInt() * it.toInt() }.average().toInt()
                    }
                }

                override fun onFftDataCapture(
                    visualizer: Visualizer?,
                    fft: ByteArray?,
                    samplingRate: Int
                ) {
                    // Do nothing
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false)
            enabled = true
        }
    }

    fun stop() {
        visualizer?.apply {
            enabled = false
            release()
        }
        visualizer = null
    }
}
