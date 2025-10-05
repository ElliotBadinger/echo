package eu.mrogalski.saidit

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.siya.epistemophile.R

/**
 * Implementation of WavFileReceiver that posts a notification
 * when a recording file is successfully saved.
 * 
 * This receiver is designed for background operation and doesn't
 * show any UI for failures - only success notifications.
 */
class NotifyFileReceiver(
    private val context: Context
) : SaidItService.WavFileReceiver {

    /**
     * Called when a recording file is successfully saved.
     * Posts a notification if the app has notification permission.
     * 
     * @param fileUri The URI of the saved audio file
     */
    override fun onSuccess(fileUri: Uri) {
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Check if we have permission to post notifications
        if (!hasNotificationPermission()) {
            return
        }
        
        notificationManager.notify(
            NOTIFICATION_ID,
            buildNotificationForFile(context, fileUri, NOTIFICATION_TITLE)
        )
    }

    /**
     * Called when saving a recording file fails.
     * Does nothing as this is designed for silent background operation.
     * 
     * @param e The exception that caused the failure
     */
    override fun onFailure(e: Exception) {
        // Do nothing for background notifications
        // Failures are handled silently in background mode
    }

    /**
     * Checks if the app has permission to post notifications.
     */
    private fun hasNotificationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val NOTIFICATION_ID = 43
        private const val NOTIFICATION_TITLE = "Recording Saved"
        private const val AUDIO_MIME_TYPE = "audio/mp4"
        private const val CHANNEL_ID = "SaidItServiceChannel"

        /**
         * Builds a notification for a saved recording file.
         * 
         * @param context The application context
         * @param fileUri The URI of the saved audio file
         * @param fileName The name to display in the notification
         * @return The built notification ready to be posted
         */
        @JvmStatic
        fun buildNotificationForFile(
            context: Context,
            fileUri: Uri,
            fileName: String
        ): Notification {
            // Create intent to open the recording when notification is clicked
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, AUDIO_MIME_TYPE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.recording_saved))
                .setContentText(fileName)
                .setSmallIcon(R.drawable.ic_stat_notify_recorded)
                .setTicker(fileName)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        }
    }
}
