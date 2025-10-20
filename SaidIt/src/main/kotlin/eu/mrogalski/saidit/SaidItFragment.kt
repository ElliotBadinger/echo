package eu.mrogalski.saidit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.siya.epistemophile.R
import com.siya.epistemophile.features.recorder.RecordingUiState
import com.siya.epistemophile.features.recorder.RecordingViewModel
import dagger.hilt.android.AndroidEntryPoint
import eu.mrogalski.android.TimeFormat
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SaidItFragment : Fragment(), SaveClipBottomSheet.SaveClipListener {

    companion object {
        private const val TOUR_START_DELAY = 500L
    }

    @Inject
    lateinit var viewModel: RecordingViewModel

    // UI Elements
    private var recordingGroup: View? = null
    private var listeningGroup: View? = null
    private var recordingTime: MaterialTextView? = null
    private var historySize: MaterialTextView? = null
    private var listeningToggleGroup: MaterialButtonToggleGroup? = null

    // State
    private var isRecording = false
    private var memorizedDuration = 0f

    @VisibleForTesting
    internal var progressDialogForTest: AlertDialog? = null

    @VisibleForTesting
    internal val listeningToggleListener = MaterialButtonToggleGroup.OnButtonCheckedListener { _, checkedId, isChecked ->
        if (isChecked) {
            when (checkedId) {
                R.id.listening_button -> viewModel.toggleListening(true)
                R.id.disabled_button -> viewModel.toggleListening(false)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_background_recorder, container, false)
        val activity = requireActivity()

        // Find UI elements
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        recordingGroup = rootView.findViewById(R.id.recording_group)
        listeningGroup = rootView.findViewById(R.id.listening_group)
        recordingTime = rootView.findViewById(R.id.recording_time)
        historySize = rootView.findViewById(R.id.history_size)
        val saveClipButton: MaterialButton = rootView.findViewById(R.id.save_clip_button)
        val settingsButton: MaterialButton = rootView.findViewById(R.id.settings_button)
        val recordingsButton: MaterialButton = rootView.findViewById(R.id.recordings_button)
        val stopRecordingButton: MaterialButton = rootView.findViewById(R.id.rec_stop_button)
        listeningToggleGroup = rootView.findViewById(R.id.listening_toggle_group)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_help -> {
                    startActivity(android.content.Intent(requireActivity(), HowToActivity::class.java))
                    true
                }
                else -> false
            }
        }

        settingsButton.setOnClickListener {
            startActivity(android.content.Intent(activity, SettingsActivity::class.java))
        }

        recordingsButton.setOnClickListener {
            startActivity(android.content.Intent(activity, RecordingsActivity::class.java))
        }

        stopRecordingButton.setOnClickListener { viewModel.stopRecording() }

        saveClipButton.setOnClickListener {
            val bottomSheet = SaveClipBottomSheet.newInstance(memorizedDuration)
            bottomSheet.setSaveClipListener(this)
            bottomSheet.show(parentFragmentManager, "SaveClipBottomSheet")
        }

        listeningToggleGroup?.addOnButtonCheckedListener(listeningToggleListener)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { renderUi(it) } }
            }
        }
    }

    private fun renderUi(state: RecordingUiState) {
        val recording = state.isRecording
        if (isRecording != recording) {
            isRecording = recording
            recordingGroup?.visibility = if (recording) View.VISIBLE else View.GONE
            listeningGroup?.visibility = if (recording) View.GONE else View.VISIBLE
        }

        // Update timers from ViewModel state
        memorizedDuration = state.memorizedSeconds
        if (recording) {
            recordingTime?.text = TimeFormat.shortTimer(state.recordedSeconds)
        } else {
            historySize?.text = TimeFormat.shortTimer(state.memorizedSeconds)
        }

        // Update listening toggle without bouncing the listener
        listeningToggleGroup?.removeOnButtonCheckedListener(listeningToggleListener)
        if (state.isListening) {
            listeningToggleGroup?.check(R.id.listening_button)
            listeningGroup?.alpha = 1.0f
        } else {
            listeningToggleGroup?.check(R.id.disabled_button)
            listeningGroup?.alpha = 0.5f
        }
        listeningToggleGroup?.addOnButtonCheckedListener(listeningToggleListener)
    }

    override fun onSaveClip(fileName: String, durationInSeconds: Float) {
        val progressDialog = MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Saving Recording")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        progressDialog.setOnDismissListener { progressDialogForTest = null }
        progressDialog.show()
        progressDialogForTest = progressDialog

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveRecording(durationInSeconds, fileName)
            if (progressDialog.isShowing) progressDialog.dismiss()
        }
    }

    @VisibleForTesting
    fun isProgressDialogShowingForTest(): Boolean = progressDialogForTest?.isShowing == true

    @VisibleForTesting
    fun getProgressDialogForTest(): AlertDialog? = progressDialogForTest

    fun startTour() {
        // A small delay to ensure the UI is fully drawn before starting the tour.
        view?.postDelayed(::startInteractiveTour, TOUR_START_DELAY)
    }

    private fun startInteractiveTour() {
        val currentActivity = activity ?: return
        val currentView = view ?: return

        val sequence = TapTargetSequence(currentActivity)
        sequence.targets(
            TapTarget.forView(currentView.findViewById<View>(R.id.listening_toggle_group),
                getString(R.string.tour_listening_toggle_title),
                getString(R.string.tour_listening_toggle_desc))
                .cancelable(false).tintTarget(false),
            TapTarget.forView(currentView.findViewById<View>(R.id.history_size),
                getString(R.string.tour_memory_holds_title),
                getString(R.string.tour_memory_holds_desc))
                .cancelable(false).tintTarget(false),
            TapTarget.forView(currentView.findViewById<View>(R.id.save_clip_button),
                getString(R.string.tour_save_clip_title),
                getString(R.string.tour_save_clip_desc))
                .cancelable(false).tintTarget(false),
            TapTarget.forView(currentView.findViewById<View>(R.id.bottom_buttons_layout),
                getString(R.string.tour_bottom_buttons_title),
                getString(R.string.tour_bottom_buttons_desc))
                .cancelable(false).tintTarget(false)
        )
        sequence.start()
    }
}

