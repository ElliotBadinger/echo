package eu.mrogalski.saidit

import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive unit tests for IntentResult Kotlin data class.
 * 
 * Tests cover data class functionality, null safety, ByteArray handling,
 * Java compatibility, and all edge cases for barcode scanning results.
 */
class IntentResultTest {

    @Test
    fun testDefaultConstructor() {
        // Test empty constructor creates object with all null values
        val result = IntentResult()
        
        assertNull("Contents should be null", result.contents)
        assertNull("Format name should be null", result.formatName)
        assertNull("Raw bytes should be null", result.rawBytes)
        assertNull("Orientation should be null", result.orientation)
        assertNull("Error correction level should be null", result.errorCorrectionLevel)
    }

    @Test
    fun testParameterizedConstructor() {
        // Test constructor with all parameters
        val rawBytes = byteArrayOf(1, 2, 3, 4, 5)
        val result = IntentResult(
            contents = "https://example.com",
            formatName = "QR_CODE",
            rawBytes = rawBytes,
            orientation = 90,
            errorCorrectionLevel = "M"
        )
        
        assertEquals("Contents should match", "https://example.com", result.contents)
        assertEquals("Format name should match", "QR_CODE", result.formatName)
        assertArrayEquals("Raw bytes should match", rawBytes, result.rawBytes)
        assertEquals("Orientation should match", 90, result.orientation)
        assertEquals("Error correction level should match", "M", result.errorCorrectionLevel)
    }

    @Test
    fun testPartialConstructor() {
        // Test constructor with some null values
        val result = IntentResult(
            contents = "12345",
            formatName = "UPC_A",
            rawBytes = null,
            orientation = null,
            errorCorrectionLevel = null
        )
        
        assertEquals("Contents should match", "12345", result.contents)
        assertEquals("Format name should match", "UPC_A", result.formatName)
        assertNull("Raw bytes should be null", result.rawBytes)
        assertNull("Orientation should be null", result.orientation)
        assertNull("Error correction level should be null", result.errorCorrectionLevel)
    }

    @Test
    fun testToStringWithAllValues() {
        // Test toString with all values present
        val rawBytes = byteArrayOf(1, 2, 3)
        val result = IntentResult(
            contents = "Test Content",
            formatName = "QR_CODE",
            rawBytes = rawBytes,
            orientation = 180,
            errorCorrectionLevel = "H"
        )
        
        val expected = "Format: QR_CODE\n" +
                      "Contents: Test Content\n" +
                      "Raw bytes: (3 bytes)\n" +
                      "Orientation: 180\n" +
                      "EC level: H\n"
        
        assertEquals("ToString should match expected format", expected, result.toString())
    }

    @Test
    fun testToStringWithNullValues() {
        // Test toString with null values
        val result = IntentResult()
        
        val expected = "Format: null\n" +
                      "Contents: null\n" +
                      "Raw bytes: (0 bytes)\n" +
                      "Orientation: null\n" +
                      "EC level: null\n"
        
        assertEquals("ToString should handle nulls correctly", expected, result.toString())
    }

    @Test
    fun testToStringWithEmptyByteArray() {
        // Test toString with empty byte array
        val result = IntentResult(
            contents = "Empty bytes",
            formatName = "CODE_128",
            rawBytes = byteArrayOf(),
            orientation = 0,
            errorCorrectionLevel = "L"
        )
        
        val expected = "Format: CODE_128\n" +
                      "Contents: Empty bytes\n" +
                      "Raw bytes: (0 bytes)\n" +
                      "Orientation: 0\n" +
                      "EC level: L\n"
        
        assertEquals("ToString should handle empty byte array", expected, result.toString())
    }

    @Test
    fun testEqualsWithSameValues() {
        // Test equals with identical objects
        val rawBytes1 = byteArrayOf(1, 2, 3)
        val rawBytes2 = byteArrayOf(1, 2, 3)
        
        val result1 = IntentResult("content", "QR_CODE", rawBytes1, 90, "M")
        val result2 = IntentResult("content", "QR_CODE", rawBytes2, 90, "M")
        
        assertEquals("Objects with same values should be equal", result1, result2)
        assertTrue("Objects with same values should be equal", result1 == result2)
    }

    @Test
    fun testEqualsWithDifferentByteArrays() {
        // Test equals with different byte arrays
        val rawBytes1 = byteArrayOf(1, 2, 3)
        val rawBytes2 = byteArrayOf(1, 2, 4)
        
        val result1 = IntentResult("content", "QR_CODE", rawBytes1, 90, "M")
        val result2 = IntentResult("content", "QR_CODE", rawBytes2, 90, "M")
        
        assertNotEquals("Objects with different byte arrays should not be equal", result1, result2)
    }

    @Test
    fun testEqualsWithNullByteArrays() {
        // Test equals with null byte arrays
        val result1 = IntentResult("content", "QR_CODE", null, 90, "M")
        val result2 = IntentResult("content", "QR_CODE", null, 90, "M")
        
        assertEquals("Objects with null byte arrays should be equal", result1, result2)
    }

    @Test
    fun testEqualsWithOneNullByteArray() {
        // Test equals with one null and one non-null byte array
        val rawBytes = byteArrayOf(1, 2, 3)
        
        val result1 = IntentResult("content", "QR_CODE", rawBytes, 90, "M")
        val result2 = IntentResult("content", "QR_CODE", null, 90, "M")
        
        assertNotEquals("Objects with different null states should not be equal", result1, result2)
    }

    @Test
    fun testEqualsWithSameReference() {
        // Test equals with same reference
        val result = IntentResult("content", "QR_CODE", byteArrayOf(1, 2, 3), 90, "M")
        
        assertTrue("Object should equal itself", result == result)
        assertEquals("Object should equal itself", result, result)
    }

    @Test
    fun testEqualsWithNull() {
        // Test equals with null
        val result = IntentResult("content", "QR_CODE", byteArrayOf(1, 2, 3), 90, "M")
        
        assertNotEquals("Object should not equal null", result, null)
        assertFalse("Object should not equal null", result.equals(null))
    }

    @Test
    fun testEqualsWithDifferentClass() {
        // Test equals with different class
        val result = IntentResult("content", "QR_CODE", byteArrayOf(1, 2, 3), 90, "M")
        val other = "Not an IntentResult"
        
        assertNotEquals("Object should not equal different class", result, other)
    }

    @Test
    fun testHashCodeConsistency() {
        // Test hashCode consistency
        val rawBytes = byteArrayOf(1, 2, 3)
        val result = IntentResult("content", "QR_CODE", rawBytes, 90, "M")
        
        val hash1 = result.hashCode()
        val hash2 = result.hashCode()
        
        assertEquals("HashCode should be consistent", hash1, hash2)
    }

    @Test
    fun testHashCodeWithEqualObjects() {
        // Test hashCode with equal objects
        val rawBytes1 = byteArrayOf(1, 2, 3)
        val rawBytes2 = byteArrayOf(1, 2, 3)
        
        val result1 = IntentResult("content", "QR_CODE", rawBytes1, 90, "M")
        val result2 = IntentResult("content", "QR_CODE", rawBytes2, 90, "M")
        
        assertEquals("Equal objects should have same hashCode", result1.hashCode(), result2.hashCode())
    }

    @Test
    fun testHashCodeWithNullValues() {
        // Test hashCode with null values
        val result1 = IntentResult()
        val result2 = IntentResult()
        
        assertEquals("Objects with all nulls should have same hashCode", result1.hashCode(), result2.hashCode())
    }

    @Test
    fun testDataClassCopyFunction() {
        // Test data class copy functionality
        val original = IntentResult(
            contents = "original",
            formatName = "QR_CODE",
            rawBytes = byteArrayOf(1, 2, 3),
            orientation = 90,
            errorCorrectionLevel = "M"
        )
        
        val copied = original.copy(contents = "modified")
        
        assertEquals("Copied object should have modified contents", "modified", copied.contents)
        assertEquals("Copied object should retain original format", "QR_CODE", copied.formatName)
        assertArrayEquals("Copied object should retain original raw bytes", original.rawBytes, copied.rawBytes)
        assertEquals("Copied object should retain original orientation", 90, copied.orientation)
        assertEquals("Copied object should retain original error correction", "M", copied.errorCorrectionLevel)
    }

    @Test
    fun testDataClassDestructuring() {
        // Test data class destructuring
        val result = IntentResult(
            contents = "test",
            formatName = "CODE_128",
            rawBytes = byteArrayOf(4, 5, 6),
            orientation = 270,
            errorCorrectionLevel = "L"
        )
        
        val (contents, formatName, rawBytes, orientation, errorCorrectionLevel) = result
        
        assertEquals("Destructured contents should match", "test", contents)
        assertEquals("Destructured format should match", "CODE_128", formatName)
        assertArrayEquals("Destructured raw bytes should match", byteArrayOf(4, 5, 6), rawBytes)
        assertEquals("Destructured orientation should match", 270, orientation)
        assertEquals("Destructured error correction should match", "L", errorCorrectionLevel)
    }

    @Test
    fun testNullSafetyWithKotlinTypes() {
        // Test null safety with Kotlin nullable types
        val result = IntentResult()
        
        // These should compile without null checks due to nullable types
        val contentsLength = result.contents?.length
        val formatUpper = result.formatName?.uppercase()
        val bytesSize = result.rawBytes?.size
        val orientationAbs = result.orientation?.let { kotlin.math.abs(it) }
        val errorLower = result.errorCorrectionLevel?.lowercase()
        
        assertNull("Null-safe operations should return null", contentsLength)
        assertNull("Null-safe operations should return null", formatUpper)
        assertNull("Null-safe operations should return null", bytesSize)
        assertNull("Null-safe operations should return null", orientationAbs)
        assertNull("Null-safe operations should return null", errorLower)
    }

    @Test
    fun testJavaCompatibility() {
        // Test Java compatibility by verifying constructor accessibility
        // This test ensures the class can be instantiated from Java code
        
        // Test default constructor (Java compatible)
        val result1 = IntentResult()
        assertNotNull("Default constructor should work", result1)
        
        // Test parameterized constructor (Java compatible)
        val result2 = IntentResult("content", "QR_CODE", byteArrayOf(1, 2), 90, "M")
        assertNotNull("Parameterized constructor should work", result2)
        assertEquals("Java constructor should set values", "content", result2.contents)
    }

    @Test
    fun testImmutability() {
        // Test that the data class properties are immutable references
        val originalBytes = byteArrayOf(1, 2, 3)
        val result = IntentResult(
            contents = "immutable",
            formatName = "QR_CODE",
            rawBytes = originalBytes,
            orientation = 90,
            errorCorrectionLevel = "M"
        )
        
        // Note: Kotlin data classes store references, not defensive copies
        // This is expected behavior - the reference is immutable, but the array content can change
        originalBytes[0] = 99
        
        // The data class holds the same reference, so changes are reflected
        assertEquals("Raw bytes reference is immutable but content can change", 99.toByte(), result.rawBytes!![0])
        
        // However, we cannot reassign the property itself (this would be a compile error)
        // result.rawBytes = byteArrayOf(4, 5, 6) // This would not compile
        
        // The immutability is at the property level, not the content level
        assertSame("Should be the same reference", originalBytes, result.rawBytes)
    }
}