package eu.mrogalski.saidit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textview.MaterialTextView
import com.siya.epistemophile.R
import com.siya.epistemophile.features.recorder.RecordingViewModel
import dagger.hilt.android.AndroidEntryPoint
import eu.mrogalski.android.TimeFormat
import eu.mrogalski.saidit.ui.Waveform

@AndroidEntryPoint
class SaidItFragment : Fragment() {

    private lateinit var recordingViewModel: RecordingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_background_recorder, container, false)
        recordingViewModel = ViewModelProvider(this).get(RecordingViewModel::class.java)

        val waveformView = rootView.findViewById<ComposeView>(R.id.waveform_view)
        waveformView.setContent {
            Waveform(amplitude = recordingViewModel.amplitude)
        }

        return rootView
    }
}
