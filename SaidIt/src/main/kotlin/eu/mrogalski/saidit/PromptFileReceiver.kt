package eu.mrogalski.saidit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.siya.epistemophile.R

/**
 * Implementation of WavFileReceiver that prompts the user with dialog options
 * when a recording file is successfully saved or when saving fails.
 * 
 * Handles UI interactions on the main thread and provides options to:
 * - Open the saved recording
 * - Share the recording with other apps
 * - Dismiss the dialog
 */
class PromptFileReceiver @JvmOverloads constructor(
    private val activity: Activity?,
    private val progressDialog: AlertDialog? = null
) : SaidItService.WavFileReceiver {

    /**
     * Called when a recording file is successfully saved.
     * Shows a dialog with options to open or share the file.
     * 
     * @param fileUri The URI of the saved audio file
     */
    override fun onSuccess(fileUri: Uri) {
        activity?.takeIf { !it.isFinishing }?.runOnUiThread {
            dismissProgressDialog()
            showSuccessDialog(fileUri)
        }
    }

    /**
     * Called when saving a recording file fails.
     * Shows an error dialog to inform the user.
     * 
     * @param e The exception that caused the failure
     */
    override fun onFailure(e: Exception) {
        activity?.takeIf { !it.isFinishing }?.runOnUiThread {
            dismissProgressDialog()
            showErrorDialog()
        }
    }

    /**
     * Dismisses the progress dialog if it's showing.
     */
    private fun dismissProgressDialog() {
        progressDialog?.takeIf { it.isShowing }?.dismiss()
    }

    /**
     * Shows a success dialog with options to open or share the recording.
     */
    private fun showSuccessDialog(fileUri: Uri) {
        activity?.let { context ->
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.recording_done_title)
                .setMessage("Recording saved to your music folder.")
                .setPositiveButton(R.string.open) { _, _ ->
                    openRecording(fileUri)
                }
                .setNeutralButton(R.string.share) { _, _ ->
                    shareRecording(fileUri)
                }
                .setNegativeButton(R.string.dismiss, null)
                .show()
        }
    }

    /**
     * Opens the recording using an appropriate audio player app.
     */
    private fun openRecording(fileUri: Uri) {
        activity?.let { context ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, AUDIO_MIME_TYPE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Shares the recording with other apps using the system share sheet.
     */
    private fun shareRecording(fileUri: Uri) {
        activity?.let { context ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = AUDIO_MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Send to"))
        }
    }

    /**
     * Shows an error dialog when saving fails.
     */
    private fun showErrorDialog() {
        activity?.let { context ->
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error_title)
                .setMessage(R.string.error_saving_failed)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    companion object {
        private const val AUDIO_MIME_TYPE = "audio/mp4"
    }
}
