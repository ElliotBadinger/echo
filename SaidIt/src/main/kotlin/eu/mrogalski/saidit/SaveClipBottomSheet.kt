package eu.mrogalski.saidit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.siya.epistemophile.R
import eu.mrogalski.android.TimeFormat

/**
 * Bottom sheet dialog for saving audio clips with customizable duration.
 * Allows users to specify a file name and select the duration of the clip to save.
 */
class SaveClipBottomSheet : BottomSheetDialogFragment() {

    /**
     * Interface for handling save clip events.
     */
    fun interface SaveClipListener {
        fun onSaveClip(fileName: String, durationInSeconds: Float)
    }

    private var memorizedDuration: Float = 0f
    private var listener: SaveClipListener? = null

    companion object {
        private const val ARG_MEMORIZED_DURATION = "memorized_duration"
        
        /**
         * Creates a new instance of SaveClipBottomSheet with the specified memorized duration.
         * @param memorizedDuration The total duration of memorized audio in seconds
         * @return A new SaveClipBottomSheet instance
         */
        @JvmStatic
        fun newInstance(memorizedDuration: Float): SaveClipBottomSheet {
            return SaveClipBottomSheet().apply {
                arguments = bundleOf(ARG_MEMORIZED_DURATION to memorizedDuration)
            }
        }
    }

    /**
     * Sets the listener for save clip events.
     * @param listener The listener to handle save events
     */
    fun setSaveClipListener(listener: SaveClipListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memorizedDuration = arguments?.getFloat(ARG_MEMORIZED_DURATION) ?: 0f
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_save_clip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
    }

    private fun setupViews(view: View) {
        val fileNameInput = view.findViewById<TextInputEditText>(R.id.recording_name)
        val durationChipGroup = view.findViewById<ChipGroup>(R.id.duration_chip_group)
        val durationAllChip = view.findViewById<Chip>(R.id.duration_all)
        val saveButton = view.findViewById<MaterialButton>(R.id.save_button)
        
        // Update the "All memory" chip with the actual duration
        durationAllChip.text = buildString {
            append(getString(R.string.all_memory))
            append(" (")
            append(TimeFormat.shortTimer(memorizedDuration))
            append(")")
        }
        
        // Set default selection
        view.findViewById<Chip>(R.id.duration_1m).isChecked = true
        
        saveButton.setOnClickListener {
            handleSaveClick(fileNameInput, durationChipGroup)
        }
    }

    private fun handleSaveClick(
        fileNameInput: TextInputEditText,
        durationChipGroup: ChipGroup
    ) {
        val fileName = fileNameInput.text?.toString()?.trim() ?: ""
        
        if (fileName.isEmpty()) {
            Toast.makeText(context, "Please enter a file name", Toast.LENGTH_SHORT).show()
            return
        }
        
        val durationInSeconds = getDurationFromChipSelection(durationChipGroup.checkedChipId)
        
        listener?.onSaveClip(fileName, durationInSeconds)
        dismiss()
    }

    private fun getDurationFromChipSelection(checkedChipId: Int): Float {
        return when (checkedChipId) {
            R.id.duration_1m -> 60f
            R.id.duration_5m -> 300f
            R.id.duration_30m -> 1800f
            R.id.duration_all -> memorizedDuration
            else -> 0f
        }
    }
}
