package eu.mrogalski.saidit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.siya.epistemophile.R;

public class PromptFileReceiver implements SaidItService.WavFileReceiver {
    private final Activity activity;
    private final AlertDialog progressDialog;

    public PromptFileReceiver(Activity activity, AlertDialog dialog) {
        this.activity = activity;
        this.progressDialog = dialog;
    }

    public PromptFileReceiver(Activity activity) {
        this(activity, null);
    }

    @Override
    public void onSuccess(final Uri fileUri) {
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.recording_done_title)
                        .setMessage("Recording saved to your music folder.")
                        .setPositiveButton(R.string.open, (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(fileUri, "audio/mp4");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(intent);
                        })
                        .setNeutralButton(R.string.share, (dialog, which) -> {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("audio/mp4");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(Intent.createChooser(shareIntent, "Send to"));
                        })
                        .setNegativeButton(R.string.dismiss, null)
                        .show();
            });
        }
    }

    @Override
    public void onFailure(Exception e) {
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.error_title)
                        .setMessage(R.string.error_saving_failed)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            });
        }
    }
}
