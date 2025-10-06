package eu.mrogalski

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/**
 * Kotlin utility object for string formatting operations.
 * Modernized from Java with improved null safety and Kotlin idioms.
 */
object StringFormat {
    
    /**
     * Formats a file size in bytes to a human-readable string with appropriate units.
     * 
     * @param size The file size in bytes
     * @return A formatted string like "1.2 MB" or "0" for non-positive sizes
     */
    @JvmStatic
    fun shortFileSize(size: Long): String {
        if (size <= 0) return "0"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        val formattedSize = size / 1024.0.pow(digitGroups)
        val symbols = DecimalFormatSymbols(Locale.US)
        return DecimalFormat("#,##0.#", symbols).format(formattedSize) + " " + units[digitGroups]
    }
}
