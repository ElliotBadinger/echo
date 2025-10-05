package eu.mrogalski.android

import android.content.res.Resources
import com.siya.epistemophile.R

/**
 * Utility object for formatting time durations in various string representations.
 * This object provides methods to format time in natural language and short timer format.
 * 
 * Converted from Java to Kotlin with improved design patterns and comprehensive documentation.
 */
object TimeFormat {
    private const val SECONDS_PER_MINUTE = 60

    /**
     * Formats the given duration in seconds into a natural language string using Android resources.
     *
     * @param resources The Android Resources object to access strings and plurals.
     * @param totalSeconds The total duration in seconds (will be floored to integer for calculation).
     * @param result The Result object to populate with the formatted text and count.
     * @throws IllegalArgumentException if totalSeconds is negative.
     */
    @JvmStatic
    fun naturalLanguage(resources: Resources, totalSeconds: Float, result: Result) {
        require(totalSeconds >= 0) { "Total seconds cannot be negative" }
        
        val temp = formatNaturalLanguage(resources, totalSeconds.toInt())
        result.text = temp.text
        result.count = temp.count
    }

    /**
     * Formats the given duration in seconds into a natural language string using Android resources.
     *
     * @param resources The Android Resources object to access strings and plurals.
     * @param totalSeconds The total duration in seconds (will be floored to integer for calculation).
     * @return A Result containing the formatted text and the main count (minutes or seconds).
     * @throws IllegalArgumentException if totalSeconds is negative.
     */
    @JvmStatic
    fun naturalLanguage(resources: Resources, totalSeconds: Float): Result {
        require(totalSeconds >= 0) { "Total seconds cannot be negative" }
        
        return formatNaturalLanguage(resources, totalSeconds.toInt())
    }

    private fun formatNaturalLanguage(resources: Resources, totalSeconds: Int): Result {
        val minutes = totalSeconds / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE

        val sb = StringBuilder()
        val count: Int

        if (minutes > 0) {
            count = minutes
            sb.append(resources.getQuantityString(R.plurals.minute, minutes, minutes))

            if (seconds > 0) {
                sb.append(resources.getString(R.string.minute_second_join))
                sb.append(resources.getQuantityString(R.plurals.second, seconds, seconds))
            }
        } else {
            count = seconds
            sb.append(resources.getQuantityString(R.plurals.second, seconds, seconds))
        }

        sb.append('.')
        return Result(sb.toString(), count)
    }

    /**
     * Formats the given duration in seconds into a short timer string (e.g., "1:23").
     *
     * @param totalSeconds The total duration in seconds.
     * @return A formatted string in minutes:seconds format.
     * @throws IllegalArgumentException if totalSeconds is negative.
     */
    @JvmStatic
    fun shortTimer(totalSeconds: Float): String {
        require(totalSeconds >= 0) { "Total seconds cannot be negative" }

        val totalSecondsInt = totalSeconds.toInt()
        val minutes = totalSecondsInt / SECONDS_PER_MINUTE
        val seconds = totalSecondsInt % SECONDS_PER_MINUTE

        return String.format("%d:%02d", minutes, seconds)
    }

    /**
     * Encapsulates the result of natural language time formatting.
     * 
     * Data class providing mutable result with proper equals/hashCode/toString implementations.
     * Maintains compatibility with Java usage patterns.
     */
    data class Result(
        @JvmField var text: String = "",
        @JvmField var count: Int = 0
    )
}