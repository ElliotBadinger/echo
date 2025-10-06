package eu.mrogalski.saidit

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.siya.epistemophile.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowLooper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RecordingsAdapterTest {

    private val activityController = Robolectric.buildActivity(FragmentActivity::class.java).setup()
    private val context: Context = activityController.get().apply { setTheme(R.style.SaidIt) }
    private val fixedNowMillis = 1705320000000L // 2024-01-15T12:00:00Z

    @Test
    fun `records are grouped under appropriate headers`() {
        val recordings = listOf(
            RecordingItem(Uri.parse("content://today"), "Today", fixedNowMillis / 1000, 5000),
            RecordingItem(
                Uri.parse("content://yesterday"),
                "Yesterday",
                (fixedNowMillis - TimeUnit.DAYS.toMillis(1)) / 1000,
                7000
            ),
            RecordingItem(
                Uri.parse("content://older"),
                "Older",
                (fixedNowMillis - TimeUnit.DAYS.toMillis(2)) / 1000,
                9000
            )
        )

        val adapter = RecordingsAdapter(
            context,
            recordings,
            playbackSessionFactory = RecordingsAdapter.PlaybackSessionFactory { FakePlaybackSession() },
            nowProvider = { fixedNowMillis }
        )

        val labels = adapter.snapshotLabels()
        val olderHeader = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            .format(Date(fixedNowMillis - TimeUnit.DAYS.toMillis(2)))
        assertEquals(
            listOf(
                "H:${context.getString(R.string.recordings_header_today)}",
                "R:Today",
                "H:${context.getString(R.string.recordings_header_yesterday)}",
                "R:Yesterday",
                "H:$olderHeader",
                "R:Older"
            ),
            labels
        )
    }

    @Test
    fun `deleting recording removes orphaned header`() {
        val uri = Uri.parse("content://single")
        val deletedUris = mutableListOf<Uri>()
        val recordings = listOf(
            RecordingItem(uri, "Clip", fixedNowMillis / 1000, 3000)
        )

        val adapter = RecordingsAdapter(
            context,
            recordings,
            playbackSessionFactory = RecordingsAdapter.PlaybackSessionFactory { FakePlaybackSession() },
            nowProvider = { fixedNowMillis },
            deleteRecording = { target ->
                deletedUris += target
                true
            }
        )

        adapter.removeEntryForTesting(position = 1)

        assertEquals(listOf(uri), deletedUris)
        assertTrue(adapter.snapshotLabels().isEmpty())
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `playback toggles between play and pause`() {
        val uri = Uri.parse("content://play")
        val recordings = listOf(
            RecordingItem(uri, "Clip", fixedNowMillis / 1000, 6000)
        )
        val fakeSession = FakePlaybackSession()

        val adapter = RecordingsAdapter(
            context,
            recordings,
            playbackSessionFactory = RecordingsAdapter.PlaybackSessionFactory { fakeSession },
            nowProvider = { fixedNowMillis }
        )

        val recyclerView = createRecyclerView(adapter)
        val holder = checkNotNull(recyclerView.findViewHolderForAdapterPosition(1)) as RecordingsAdapter.BaseViewHolder
        adapter.onBindViewHolder(holder, 1)

        val playButton = holder.itemView.findViewById<MaterialButton>(R.id.play_button)
        playButton.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue(fakeSession.started)
        assertTrue(fakeSession.isPlaying)

        playButton.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertTrue(fakeSession.paused)
        assertFalse(fakeSession.isPlaying)

        fakeSession.complete()
        playButton.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertEquals(2, fakeSession.startCount)
    }

    private fun createRecyclerView(adapter: RecordingsAdapter): RecyclerView {
        return RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
            measure(
                View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1200, View.MeasureSpec.AT_MOST)
            )
            layout(0, 0, measuredWidth, measuredHeight)
        }
    }

    private class FakePlaybackSession : RecordingsAdapter.PlaybackSession {
        private var completion: (() -> Unit)? = null
        private var playing = false
        var startCount = 0
            private set
        var started = false
            private set
        var paused = false
            private set

        override val isPlaying: Boolean
            get() = playing

        override fun prepare(uri: Uri) {
            // no-op
        }

        override fun start() {
            playing = true
            started = true
            startCount++
        }

        override fun pause() {
            playing = false
            paused = true
        }

        override fun release() {
            playing = false
        }

        override fun setOnCompletionListener(onComplete: () -> Unit) {
            completion = onComplete
        }

        fun complete() {
            completion?.invoke()
        }
    }

}
