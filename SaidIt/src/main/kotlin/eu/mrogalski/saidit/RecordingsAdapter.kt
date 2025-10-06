package eu.mrogalski.saidit

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.siya.epistemophile.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class RecordingsAdapter @JvmOverloads constructor(
    private val context: Context,
    recordings: List<RecordingItem>,
    private val playbackSessionFactory: PlaybackSessionFactory = PlaybackSessionFactory { MediaPlayerPlaybackSession(context) },
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
    private val deleteRecording: (Uri) -> Boolean = { uri ->
        context.contentResolver.delete(uri, null, null) > 0
    }
) : RecyclerView.Adapter<RecordingsAdapter.BaseViewHolder>() {

    private val items: MutableList<RecordingListItem> = buildItems(recordings)

    @VisibleForTesting
    internal fun snapshotLabels(): List<String> = items.map {
        when (it) {
            is RecordingListItem.Header -> "H:${it.title}"
            is RecordingListItem.Entry -> "R:${it.recording.name}"
        }
    }

    private val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    private var playbackSession: PlaybackSession? = null
    private var playingPosition: Int? = null

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is RecordingListItem.Header -> VIEW_TYPE_HEADER
        is RecordingListItem.Entry -> VIEW_TYPE_RECORDING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.list_item_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.list_item_recording, parent, false)
                RecordingViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(items[position] as RecordingListItem.Header)
            is RecordingViewHolder -> {
                val entry = items[position] as RecordingListItem.Entry
                holder.bind(entry)
                holder.updatePlayButton(position == playingPosition)
            }
        }
    }

    fun releasePlayer() {
        val hadSession = playbackSession != null
        releasePlayback()
        playingPosition = null
        if (hadSession) {
            notifyDataSetChanged()
        }
    }

    private fun onPlaybackRequested(holder: RecordingViewHolder, position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        val entry = items.getOrNull(position) as? RecordingListItem.Entry ?: return

        val currentSession = playbackSession
        if (playingPosition == position && currentSession != null) {
            if (currentSession.isPlaying) {
                currentSession.pause()
                holder.updatePlayButton(false)
            } else {
                currentSession.start()
                holder.updatePlayButton(true)
            }
            return
        }

        val previousPosition = playingPosition
        releasePlayback()

        val session = playbackSessionFactory.create()
        try {
            session.prepare(entry.recording.uri)
            session.setOnCompletionListener {
                playingPosition = null
                playbackSession = null
                notifyItemChanged(position)
            }
            session.start()
            playbackSession = session
            playingPosition = position
            holder.updatePlayButton(true)
            previousPosition?.let { notifyItemChanged(it) }
        } catch (ioException: IOException) {
            playbackSession = null
            playingPosition = null
            holder.updatePlayButton(false)
        }
    }

    private fun onDeleteRequested(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        val entry = items.getOrNull(position) as? RecordingListItem.Entry ?: return

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.recordings_delete_title)
            .setMessage(R.string.recordings_delete_message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.recordings_delete_confirm) { _, _ ->
                removeEntry(position, entry)
            }
            .show()
    }

    private fun removeEntry(position: Int, entry: RecordingListItem.Entry) {
        val removed = deleteRecording(entry.recording.uri)
        if (!removed) {
            return
        }

        if (playingPosition == position) {
            releasePlayback()
            playingPosition = null
        }

        playbackSession = null

        items.removeAt(position)
        notifyItemRemoved(position)
        adjustPlayingPositionAfterRemoval(position)
        removeHeaderIfOrphaned(position)
    }

    private fun adjustPlayingPositionAfterRemoval(removedIndex: Int) {
        playingPosition = when (val current = playingPosition) {
            null -> null
            removedIndex -> null
            else -> if (current > removedIndex) current - 1 else current
        }
    }

    private fun removeHeaderIfOrphaned(afterRemovalIndex: Int) {
        val headerIndex = afterRemovalIndex - 1
        if (headerIndex < 0) return
        if (items.getOrNull(headerIndex) !is RecordingListItem.Header) return

        val headerHasItems = items.getOrNull(headerIndex + 1) is RecordingListItem.Entry
        if (!headerHasItems) {
            items.removeAt(headerIndex)
            notifyItemRemoved(headerIndex)
            adjustPlayingPositionAfterRemoval(headerIndex)
        }
    }

    private fun releasePlayback() {
        playbackSession?.release()
        playbackSession = null
    }

    private fun buildItems(recordings: List<RecordingItem>): MutableList<RecordingListItem> {
        if (recordings.isEmpty()) return mutableListOf()

        val result = mutableListOf<RecordingListItem>()
        var lastHeader: String? = null
        recordings.forEach { recording ->
            val header = headerLabel(recording.date)
            if (header != lastHeader) {
                result.add(RecordingListItem.Header(header))
                lastHeader = header
            }
            result.add(RecordingListItem.Entry(recording))
        }
        return result
    }

    private fun headerLabel(timestampSeconds: Long): String {
        val now = Calendar.getInstance().apply { timeInMillis = nowProvider() }
        val target = Calendar.getInstance().apply {
            timeInMillis = TimeUnit.SECONDS.toMillis(timestampSeconds)
        }

        val sameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        if (sameDay) {
            return context.getString(R.string.recordings_header_today)
        }

        now.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        return if (yesterday) {
            context.getString(R.string.recordings_header_yesterday)
        } else {
            dateFormatter.format(Date(TimeUnit.SECONDS.toMillis(timestampSeconds)))
        }
    }

    private fun formatInfo(recording: RecordingItem): String {
        val duration = formatDuration(recording.duration)
        val date = dateFormatter.format(Date(TimeUnit.SECONDS.toMillis(recording.date)))
        return context.getString(R.string.recording_info_format, duration, date)
    }

    private fun formatDuration(durationMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) -
            TimeUnit.MINUTES.toSeconds(minutes)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    abstract class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal abstract fun bind(item: RecordingListItem)
    }

    private inner class HeaderViewHolder(view: View) : BaseViewHolder(view) {
        private val headerText: TextView = view.findViewById(R.id.header_text_view)

        override fun bind(item: RecordingListItem) {
            val header = item as RecordingListItem.Header
            headerText.text = header.title
        }
    }

    private inner class RecordingViewHolder(view: View) : BaseViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.recording_name_text)
        private val infoText: TextView = view.findViewById(R.id.recording_info_text)
        private val playButton: MaterialButton = view.findViewById(R.id.play_button)
        private val deleteButton: MaterialButton = view.findViewById(R.id.delete_button)
        override fun bind(item: RecordingListItem) {
            val entry = item as RecordingListItem.Entry
            nameText.text = entry.recording.name
            infoText.text = formatInfo(entry.recording)

            playButton.setOnClickListener {
                val currentPosition = layoutPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onPlaybackRequested(this, currentPosition)
                }
            }

            deleteButton.setOnClickListener {
                val currentPosition = layoutPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onDeleteRequested(currentPosition)
                }
            }
        }

        fun updatePlayButton(isPlaying: Boolean) {
            val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
            playButton.setIconResource(iconRes)
        }
    }

    internal sealed class RecordingListItem {
        data class Header(val title: String) : RecordingListItem()
        data class Entry(val recording: RecordingItem) : RecordingListItem()
    }

    fun interface PlaybackSessionFactory {
        fun create(): PlaybackSession
    }

    interface PlaybackSession {
        val isPlaying: Boolean
        @Throws(IOException::class)
        fun prepare(uri: Uri)
        fun start()
        fun pause()
        fun release()
        fun setOnCompletionListener(onComplete: () -> Unit)
    }

    private class MediaPlayerPlaybackSession(private val context: Context) : PlaybackSession {
        private val mediaPlayer = android.media.MediaPlayer()

        override val isPlaying: Boolean
            get() = mediaPlayer.isPlaying

        @Throws(IOException::class)
        override fun prepare(uri: Uri) {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.prepare()
        }

        override fun start() {
            mediaPlayer.start()
        }

        override fun pause() {
            mediaPlayer.pause()
        }

        override fun release() {
            mediaPlayer.reset()
            mediaPlayer.release()
        }

        override fun setOnCompletionListener(onComplete: () -> Unit) {
            mediaPlayer.setOnCompletionListener {
                onComplete()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_RECORDING = 1
    }
}
