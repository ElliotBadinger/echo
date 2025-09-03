package eu.mrogalski.saidit;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.siya.epistemophile.R;

public class NotifyFileReceiver implements SaidItService.WavFileReceiver {
    private final Context context;

    public NotifyFileReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onSuccess(final Uri fileUri) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(43, buildNotificationForFile(context, fileUri, "Recording Saved"));
    }

    @Override
    public void onFailure(Exception e) {
        // Do nothing for background notifications
    }

    static Notification buildNotificationForFile(Context context, Uri fileUri, String fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "audio/mp4");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "SaidItServiceChannel")
                .setContentTitle(context.getString(R.string.recording_saved))
                .setContentText(fileName)
                .setSmallIcon(R.drawable.ic_stat_notify_recorded)
                .setTicker(fileName)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return notificationBuilder.build();
    }
}
