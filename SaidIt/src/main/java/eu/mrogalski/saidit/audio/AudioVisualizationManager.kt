package eu.mrogalski.saidit.audio

import com.siya.epistemophile.audio.AudioVisualizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AudioVisualizationManager {

    interface VisualizationListener {
        fun onAmplitudeUpdate(normalizedAmplitude: Float)
        fun onError(error: String)
    }

    private var listener: VisualizationListener? = null
    private val audioVisualizer = AudioVisualizer()
    private val scope = CoroutineScope(Dispatchers.Main)

    fun setVisualizationListener(listener: VisualizationListener) {
        this.listener = listener
    }

    fun start() {
        audioVisualizer.start(0)
        scope.launch {
            audioVisualizer.amplitude.collect { amplitude ->
                listener?.onAmplitudeUpdate(normalizeAmplitude(amplitude.toFloat()))
            }
        }
    }

    fun stop() {
        audioVisualizer.stop()
    }

    private fun normalizeAmplitude(amplitude: Float): Float {
        // Normalize to 0.0 - 1.0 range
        // 32767 is max value for 16-bit audio
        var normalized = amplitude / 32767f

        // Apply logarithmic scaling for better visualization
        if (normalized > 0) {
            normalized = (Math.log10(1 + normalized * 9.0) / Math.log10(10.0)).toFloat()
        }

        return Math.max(0f, Math.min(1f, normalized))
    }

    private fun notifyError(error: String) {
        listener?.onError(error)
    }
}
