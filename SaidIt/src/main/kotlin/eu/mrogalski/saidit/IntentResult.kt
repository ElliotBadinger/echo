package eu.mrogalski.saidit

/**
 * Modern Kotlin data class representing the result of a barcode scanning intent.
 * 
 * This immutable data class encapsulates all information returned from barcode scanning
 * operations, providing type-safe access to scan results with null safety guarantees.
 * The class follows modern Android development patterns while maintaining compatibility
 * with existing Java code.
 * 
 * @property contents The raw content of the scanned barcode, or null if no content available
 * @property formatName The name of the barcode format (e.g., "QR_CODE", "UPC_A"), or null if unknown
 * @property rawBytes The raw bytes of the barcode content, or null if not applicable
 * @property orientation The rotation of the image in degrees that resulted in successful scan, or null if not available
 * @property errorCorrectionLevel The name of the error correction level used in the barcode, or null if not applicable
 * 
 * @since 2.0.0
 */
data class IntentResult(
    val contents: String? = null,
    val formatName: String? = null,
    val rawBytes: ByteArray? = null,
    val orientation: Int? = null,
    val errorCorrectionLevel: String? = null
) {
    
    /**
     * Creates an empty IntentResult with all properties set to null.
     * This constructor maintains compatibility with the original Java implementation.
     */
    constructor() : this(null, null, null, null, null)
    
    /**
     * Provides a detailed string representation of the barcode scan result.
     * 
     * The format matches the original Java implementation for backward compatibility,
     * displaying format, contents, raw bytes length, orientation, and error correction level.
     * 
     * @return A formatted string containing all barcode scan information
     */
    override fun toString(): String {
        val rawBytesLength = rawBytes?.size ?: 0
        return buildString {
            append("Format: ").append(formatName).append('\n')
            append("Contents: ").append(contents).append('\n')
            append("Raw bytes: (").append(rawBytesLength).append(" bytes)\n")
            append("Orientation: ").append(orientation).append('\n')
            append("EC level: ").append(errorCorrectionLevel).append('\n')
        }
    }
    
    /**
     * Custom equals implementation that properly handles ByteArray comparison.
     * 
     * The default data class equals() doesn't handle ByteArray properly (uses reference equality),
     * so we override to use contentEquals() for proper byte array comparison.
     * 
     * @param other The object to compare with this IntentResult
     * @return true if the objects are equal, false otherwise
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as IntentResult
        
        if (contents != other.contents) return false
        if (formatName != other.formatName) return false
        if (rawBytes != null) {
            if (other.rawBytes == null) return false
            if (!rawBytes.contentEquals(other.rawBytes)) return false
        } else if (other.rawBytes != null) {
            return false
        }
        if (orientation != other.orientation) return false
        if (errorCorrectionLevel != other.errorCorrectionLevel) return false
        
        return true
    }
    
    /**
     * Custom hashCode implementation that properly handles ByteArray.
     * 
     * Uses contentHashCode() for ByteArray to ensure consistent hashing behavior
     * that matches the custom equals() implementation.
     * 
     * @return The hash code for this IntentResult
     */
    override fun hashCode(): Int {
        var result = contents?.hashCode() ?: 0
        result = 31 * result + (formatName?.hashCode() ?: 0)
        result = 31 * result + (rawBytes?.contentHashCode() ?: 0)
        result = 31 * result + (orientation ?: 0)
        result = 31 * result + (errorCorrectionLevel?.hashCode() ?: 0)
        return result
    }
}