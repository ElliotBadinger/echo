package eu.mrogalski.saidit

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.siya.epistemophile.R

/**
 * Activity that displays a list of audio recordings from the device's MediaStore.
 * Supports playback through RecordingsAdapter and shows empty state when no recordings exist.
 */
class RecordingsActivity : AppCompatActivity() {

    private var adapter: RecordingsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordings)

        setupToolbar()
        setupRecordingsList()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecordingsList() {
        val recyclerView = findViewById<RecyclerView>(R.id.recordings_recycler_view)
        val emptyView = findViewById<TextView>(R.id.empty_view)

        // Load recordings and set up adapter
        val recordings = getRecordings()
        adapter = RecordingsAdapter(this, recordings)
        recyclerView.adapter = adapter

        // Show empty view if no recordings, otherwise show list
        if (recordings.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    /**
     * Queries the MediaStore for audio recordings matching supported MIME types.
     * Returns a list of RecordingItem objects sorted by date added (newest first).
     */
    private fun getRecordings(): List<RecordingItem> {
        val recordingItems = mutableListOf<RecordingItem>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION
        )
        
        val selection = "${MediaStore.Audio.Media.MIME_TYPE} IN (?, ?, ?)"
        val selectionArgs = arrayOf("audio/mp4", "audio/m4a", "audio/aac")
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val date = cursor.getLong(dateColumn)
                val duration = cursor.getLong(durationColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                    id
                )
                recordingItems.add(RecordingItem(contentUri, name, date, duration))
            }
        }
        
        return recordingItems
    }

    override fun onStop() {
        super.onStop()
        adapter?.releasePlayer()
    }
}
