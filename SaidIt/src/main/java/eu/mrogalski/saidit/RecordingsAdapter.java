package eu.mrogalski.saidit;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.RecordingViewHolder> {

    private final List<RecordingItem> recordings;
    private final Context context;
    private MediaPlayer mediaPlayer;
    private int playingPosition = -1;

    public RecordingsAdapter(Context context, List<RecordingItem> recordings) {
        this.context = context;
        this.recordings = recordings;
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            playingPosition = -1;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recording, parent, false);
        return new RecordingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
        RecordingItem recording = recordings.get(position);
        holder.bind(recording);

        if (position == playingPosition) {
            holder.playButton.setIconResource(R.drawable.ic_pause);
        } else {
            holder.playButton.setIconResource(R.drawable.ic_play_arrow);
        }
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    class RecordingViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView infoTextView;
        private final MaterialButton playButton;
        private final MaterialButton deleteButton;

        public RecordingViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.recording_name_text);
            infoTextView = itemView.findViewById(R.id.recording_info_text);
            playButton = itemView.findViewById(R.id.play_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }

        public void bind(RecordingItem recording) {
            nameTextView.setText(recording.getName());

            Date date = new Date(recording.getDate() * 1000); // MediaStore date is in seconds
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String dateString = formatter.format(date);

            long durationMillis = recording.getDuration();
            String durationString = String.format(Locale.getDefault(), "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(durationMillis),
                    TimeUnit.MILLISECONDS.toSeconds(durationMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMillis))
            );

            infoTextView.setText(String.format("%s | %s", durationString, dateString));

            playButton.setOnClickListener(v -> handlePlayback(recording, getAdapterPosition()));

            deleteButton.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Delete Recording")
                        .setMessage("Are you sure you want to permanently delete this file?")
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton("Delete", (dialog, which) -> {
                            int currentPosition = getAdapterPosition();
                            if (currentPosition != RecyclerView.NO_POSITION) {
                                // Stop playback if the deleted item is the one playing
                                if (playingPosition == currentPosition) {
                                    releasePlayer();
                                }

                                RecordingItem itemToDelete = recordings.get(currentPosition);
                                ContentResolver contentResolver = context.getContentResolver();
                                int deletedRows = contentResolver.delete(itemToDelete.getUri(), null, null);
                                
                                if (deletedRows > 0) {
                                    recordings.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                    notifyItemRangeChanged(currentPosition, recordings.size());
                                    // Adjust playing position if an item before it was removed
                                    if (playingPosition > currentPosition) {
                                        playingPosition--;
                                    }
                                }
                            }
                        })
                        .show();
            });
        }

        private void handlePlayback(RecordingItem recording, int position) {
            if (playingPosition == position) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playButton.setIconResource(R.drawable.ic_play_arrow);
                } else {
                    mediaPlayer.start();
                    playButton.setIconResource(R.drawable.ic_pause);
                }
            } else {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }

                int previousPlayingPosition = playingPosition;
                playingPosition = position;

                if (previousPlayingPosition != -1) {
                    notifyItemChanged(previousPlayingPosition);
                }

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(context, recording.getUri());
                    mediaPlayer.prepare();
                    mediaPlayer.setOnCompletionListener(mp -> {
                        playingPosition = -1;
                        notifyItemChanged(position);
                    });
                    mediaPlayer.start();
                    playButton.setIconResource(R.drawable.ic_pause);
                } catch (IOException e) {
                    e.printStackTrace();
                    playingPosition = -1;
                }
            }
        }
    }
}
