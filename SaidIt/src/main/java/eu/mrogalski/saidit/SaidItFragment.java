package eu.mrogalski.saidit;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;

import eu.mrogalski.android.TimeFormat;

public class SaidItFragment extends Fragment implements SaveClipBottomSheet.SaveClipListener {

    private static final String YOUR_NOTIFICATION_CHANNEL_ID = "SaidItServiceChannel";
    private SaidItService echo;

    // UI Elements
    private View recordingGroup;
    private View listeningGroup;
    private MaterialTextView recordingTime;
    private MaterialTextView historySize;
    private MaterialButtonToggleGroup listeningToggleGroup;

    // State
    private boolean isRecording = false;
    private float memorizedDuration = 0;

    private final Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (getView() == null || echo == null) return;
            echo.getState(serviceStateCallback);
        }
    };

    private final ServiceConnection echoConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            SaidItService.BackgroundRecorderBinder typedBinder = (SaidItService.BackgroundRecorderBinder) binder;
            echo = typedBinder.getService();
            if (getView() != null) {
                getView().postOnAnimation(updater);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            echo = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_background_recorder, container, false);
        final Activity activity = requireActivity();

        // Find new UI elements
        recordingGroup = rootView.findViewById(R.id.recording_group);
        listeningGroup = rootView.findViewById(R.id.listening_group);
        recordingTime = rootView.findViewById(R.id.recording_time);
        historySize = rootView.findViewById(R.id.history_size);
        MaterialButton saveClipButton = rootView.findViewById(R.id.save_clip_button);
        MaterialButton settingsButton = rootView.findViewById(R.id.settings_button);
        MaterialButton stopRecordingButton = rootView.findViewById(R.id.rec_stop_button);
        listeningToggleGroup = rootView.findViewById(R.id.listening_toggle_group);

        // Set listeners
        settingsButton.setOnClickListener(v -> startActivity(new Intent(activity, SettingsActivity.class)));

        stopRecordingButton.setOnClickListener(v -> {
            if (echo != null) {
                echo.stopRecording(new PromptFileReceiver(activity), "");
            }
        });

        saveClipButton.setOnClickListener(v -> {
            SaveClipBottomSheet bottomSheet = SaveClipBottomSheet.newInstance(memorizedDuration);
            bottomSheet.setSaveClipListener(this);
            bottomSheet.show(getParentFragmentManager(), "SaveClipBottomSheet");
        });

        listeningToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && echo != null) {
                if (checkedId == R.id.listening_button) {
                    echo.enableListening();
                } else if (checkedId == R.id.disabled_button) {
                    echo.disableListening();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onSaveClip(String fileName, float durationInSeconds) {
        if (echo != null) {
            AlertDialog progressDialog = new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Saving Recording")
                    .setMessage("Please wait...")
                    .setCancelable(false)
                    .create();
            progressDialog.show();

            echo.dumpRecording(durationInSeconds, new PromptFileReceiver(getActivity(), progressDialog), fileName);
        }
    }


    private final SaidItService.StateCallback serviceStateCallback = new SaidItService.StateCallback() {
        @Override
        public void state(final boolean listeningEnabled, final boolean recording, final float memorized, final float totalMemory, final float recorded) {
            memorizedDuration = memorized;
            
            if (isRecording != recording) {
                isRecording = recording;
                recordingGroup.setVisibility(recording ? View.VISIBLE : View.GONE);
                listeningGroup.setVisibility(recording ? View.GONE : View.VISIBLE);
            }

            if (isRecording) {
                recordingTime.setText(TimeFormat.shortTimer(recorded));
            } else {
                historySize.setText(TimeFormat.shortTimer(memorized));
            }

            // Update listening toggle state without triggering listener
            listeningToggleGroup.removeOnButtonCheckedListener(null);
            if (listeningEnabled) {
                listeningToggleGroup.check(R.id.listening_button);
                listeningGroup.setAlpha(1.0f);
            } else {
                listeningToggleGroup.check(R.id.disabled_button);
                listeningGroup.setAlpha(0.5f);
            }

            if (getView() != null) {
                getView().postOnAnimationDelayed(updater, 100);
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        final Activity activity = requireActivity();
        activity.bindService(new Intent(activity, SaidItService.class), echoConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().unbindService(echoConnection);
        echo = null;
    }

    // --- File Receiver and Notification Logic ---

    static Notification buildNotificationForFile(Context context, File outFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", outFile);
        intent.setDataAndType(fileUri, "audio/wav");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, YOUR_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.recording_saved))
                .setContentText(outFile.getName())
                .setSmallIcon(R.drawable.ic_stat_notify_recorded)
                .setTicker(outFile.getName())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return notificationBuilder.build();
    }

    static class NotifyFileReceiver implements SaidItService.WavFileReceiver {
        private final Context context;

        public NotifyFileReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void fileReady(final File file, float runtime) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(43, buildNotificationForFile(context, file));
        }
    }


    static class PromptFileReceiver implements SaidItService.WavFileReceiver {
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
        public void fileReady(final File file, float runtime) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (activity != null && !activity.isFinishing()) {
                Uri fileUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", file);
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.recording_done_title)
                        .setMessage(file.getName())
                        .setPositiveButton(R.string.open, (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(fileUri, "audio/wav");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(intent);
                        })
                        .setNeutralButton(R.string.share, (dialog, which) -> {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("audio/wav");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(Intent.createChooser(shareIntent, "Send to"));
                        })
                        .setNegativeButton(R.string.dismiss, null)
                        .show();
            }
        }
    }
}
