package eu.mrogalski.saidit

import android.net.Uri

/**
 * Immutable data class representing a recording item with metadata.
 * 
 * This class encapsulates the essential information about a recorded audio file,
 * including its location, display name, creation timestamp, and duration.
 * 
 * @property uri The URI location of the recording file
 * @property name The display name of the recording
 * @property date The creation timestamp in milliseconds since epoch
 * @property duration The duration of the recording in milliseconds
 * 
 * @since 1.0
 */
data class RecordingItem(
    val uri: Uri,
    val name: String,
    val date: Long,
    val duration: Long
) {
    init {
        require(name.isNotBlank()) { "Recording name cannot be blank" }
        require(date >= 0) { "Recording date must be non-negative" }
        require(duration >= 0) { "Recording duration must be non-negative" }
    }
    
    /**
     * Returns a human-readable string representation of the recording duration.
     * 
     * @return Formatted duration string (e.g., "2:30" for 2 minutes 30 seconds)
     */
    fun getFormattedDuration(): String {
        val seconds = duration / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
    
    /**
     * Checks if this recording is considered recent (within the last 24 hours).
     * Uses >= comparison to include recordings exactly at the 24-hour boundary.
     * 
     * @return true if the recording was created within the last 24 hours
     */
    fun isRecent(): Boolean {
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return date >= twentyFourHoursAgo
    }
}