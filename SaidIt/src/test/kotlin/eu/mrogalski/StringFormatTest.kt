package eu.mrogalski

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for StringFormat utility class.
 * Tests the file size formatting functionality.
 */
class StringFormatTest {

    @Test
    fun testShortFileSize_zeroAndNegative() {
        assertEquals("0", StringFormat.shortFileSize(0))
        assertEquals("0", StringFormat.shortFileSize(-1))
        assertEquals("0", StringFormat.shortFileSize(-100))
    }

    @Test
    fun testShortFileSize_bytes() {
        assertEquals("1 B", StringFormat.shortFileSize(1))
        assertEquals("512 B", StringFormat.shortFileSize(512))
        assertEquals("1,023 B", StringFormat.shortFileSize(1023))
    }

    @Test
    fun testShortFileSize_kilobytes() {
        assertEquals("1 KB", StringFormat.shortFileSize(1024))
        assertEquals("1.5 KB", StringFormat.shortFileSize(1536)) // 1.5 * 1024
        assertEquals("10 KB", StringFormat.shortFileSize(10240))
    }

    @Test
    fun testShortFileSize_megabytes() {
        assertEquals("1 MB", StringFormat.shortFileSize(1048576)) // 1024 * 1024
        assertEquals("2.5 MB", StringFormat.shortFileSize(2621440)) // 2.5 * 1024 * 1024
        assertEquals("100 MB", StringFormat.shortFileSize(104857600))
    }

    @Test
    fun testShortFileSize_gigabytes() {
        assertEquals("1 GB", StringFormat.shortFileSize(1073741824)) // 1024^3
        assertEquals("4 GB", StringFormat.shortFileSize(4294967296L)) // 4 * 1024^3
    }

    @Test
    fun testShortFileSize_terabytes() {
        assertEquals("1 TB", StringFormat.shortFileSize(1099511627776L)) // 1024^4
    }
}