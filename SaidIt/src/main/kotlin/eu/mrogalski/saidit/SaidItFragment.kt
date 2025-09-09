package eu.mrogalski.saidit

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.textview.MaterialTextView
import eu.mrogalski.android.TimeFormat
import com.siya.epistemophile.R
import java.io.File

/**
 * Main fragment for the SaidIt audio recording application.
 * 
 * This fragment manages the UI for audio recording functionality, including:
 * - Recording controls and status display
 * - Service connection management
 * - User interaction handling
 * - File saving and sharing workflows
 * 
 * The fragment communicates with SaidItService for audio processing and
 * implements SaveClipBottomSheet.SaveClipListener for clip saving functionality.
 */
class SaidItFragment : Fragment(), SaveClipBottomSheet.SaveClipListener {

    companion object {
        private const val YOUR_NOTIFICATION_CHANNEL_ID = "SaidItServiceChannel"
        private const val NOTIFICATION_ID = 43
        private const val TOUR_START_DELAY = 500L
        private const val UI_UPDATE_DELAY = 100L
    }

    private var echo: SaidItService? = null

    // UI Elements
    private var recordingGroup: View? = null
    private var listeningGroup: View? = null
    private var recordingTime: MaterialTextView? = null
    private var historySize: MaterialTextView? = null
    private var listeningToggleGroup: MaterialButtonToggleGroup? = null

    // State
    private var isRecording = false
    private var memorizedDuration = 0f

    fun setService(service: SaidItService?) {
        this.echo = service
        view?.postOnAnimation(updater)
    }

    private val listeningToggleListener = MaterialButtonToggleGroup.OnButtonCheckedListener { _, checkedId, isChecked ->
        if (isChecked) {
            when (checkedId) {
                R.id.listening_button -> echo?.enableListening()
                R.id.disabled_button -> echo?.disableListening()
            }
        }
    }

    private val updater: Runnable = Runnable {
        view?.let { view ->
            echo?.let { service ->
                service.getState(serviceStateCallback)
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

        // Find new UI elements
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

        // Set listeners
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_help -> {
                    startActivity(Intent(requireActivity(), HowToActivity::class.java))
                    true
                }
                else -> false
            }
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }

        recordingsButton.setOnClickListener {
            startActivity(Intent(activity, RecordingsActivity::class.java))
        }

        stopRecordingButton.setOnClickListener {
            echo?.stopRecording(PromptFileReceiver(activity))
        }

        saveClipButton.setOnClickListener {
            val bottomSheet = SaveClipBottomSheet.newInstance(memorizedDuration)
            bottomSheet.setSaveClipListener(this)
            bottomSheet.show(parentFragmentManager, "SaveClipBottomSheet")
        }

        listeningToggleGroup?.addOnButtonCheckedListener(listeningToggleListener)

        return rootView
    }

    override fun onSaveClip(fileName: String, durationInSeconds: Float) {
        echo?.let { service ->
            val progressDialog = MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Saving Recording")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create()
            progressDialog.show()

            service.dumpRecording(durationInSeconds, PromptFileReceiver(requireActivity(), progressDialog), fileName)
        }
    }

    private val serviceStateCallback = object : SaidItService.StateCallback {
        override fun state(
            listeningEnabled: Boolean,
            recording: Boolean,
            memorized: Float,
            totalMemory: Float,
            recorded: Float
        ) {
            memorizedDuration = memorized
            
            if (isRecording != recording) {
                isRecording = recording
                recordingGroup?.visibility = if (recording) View.VISIBLE else View.GONE
                listeningGroup?.visibility = if (recording) View.GONE else View.VISIBLE
            }

            if (isRecording) {
                recordingTime?.text = TimeFormat.shortTimer(recorded)
            } else {
                historySize?.text = TimeFormat.shortTimer(memorized)
            }

            // Update listening toggle state without triggering listener
            listeningToggleGroup?.removeOnButtonCheckedListener(listeningToggleListener)
            if (listeningEnabled) {
                listeningToggleGroup?.check(R.id.listening_button)
                listeningGroup?.alpha = 1.0f
            } else {
                listeningToggleGroup?.check(R.id.disabled_button)
                listeningGroup?.alpha = 0.5f
            }
            listeningToggleGroup?.addOnButtonCheckedListener(listeningToggleListener)

            view?.postOnAnimationDelayed(updater, UI_UPDATE_DELAY)
        }
    }

    override fun onStart() {
        super.onStart()
        val activity = activity as? SaidItActivity
        activity?.let { saidItActivity ->
            echo = saidItActivity.getEchoService()
            view?.postOnAnimation(updater)
        }
    }

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

    // --- File Receiver and Notification Logic ---

    private fun buildNotificationForFile(context: Context, fileUri: Uri, fileName: String): Notification {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "audio/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, YOUR_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.recording_saved))
            .setContentText(fileName)
            .setSmallIcon(R.drawable.ic_stat_notify_recorded)
            .setTicker(fileName)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    inner class NotifyFileReceiver(private val context: Context) : SaidItService.WavFileReceiver {
        override fun onSuccess(fileUri: Uri) {
            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notificationManager.notify(NOTIFICATION_ID, buildNotificationForFile(context, fileUri, "Recording Saved"))
        }

        override fun onFailure(e: Exception) {
            // Do nothing for background notifications
        }
    }

    inner class PromptFileReceiver(
        private val activity: Activity,
        private val progressDialog: AlertDialog? = null
    ) : SaidItService.WavFileReceiver {

        override fun onSuccess(fileUri: Uri) {
            if (activity.isFinishing) return
            
            activity.runOnUiThread {
                progressDialog?.takeIf { it.isShowing }?.dismiss()

                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.recording_done_title)
                    .setMessage("Recording saved to your music folder.")
                    .setPositiveButton(R.string.open) { _: android.content.DialogInterface, _: Int ->
                        Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(fileUri, "audio/mp4")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivity(this)
                        }
                    }
                    .setNeutralButton(R.string.share) { _: android.content.DialogInterface, _: Int ->
                        Intent(Intent.ACTION_SEND).apply {
                            type = "audio/mp4"
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            activity.startActivity(Intent.createChooser(this, "Send to"))
                        }
                    }
                    .setNegativeButton(R.string.dismiss, null as android.content.DialogInterface.OnClickListener?)
                    .show()
            }
        }

        override fun onFailure(e: Exception) {
            if (activity.isFinishing) return
            
            activity.runOnUiThread {
                progressDialog?.takeIf { it.isShowing }?.dismiss()

                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.error_title)
                    .setMessage(R.string.error_saving_failed)
                    .setPositiveButton(android.R.string.ok, null as android.content.DialogInterface.OnClickListener?)
                    .show()
            }
        }
    }
}