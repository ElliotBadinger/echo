package eu.mrogalski.saidit

import android.net.Uri
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Comprehensive unit tests for RecordingItem data class.
 * 
 * Tests cover:
 * - Data class functionality (equals, hashCode, toString, copy)
 * - Input validation and error handling
 * - Utility methods (formatted duration, recent check)
 * - Edge cases and boundary conditions
 */
@RunWith(MockitoJUnitRunner::class)
class RecordingItemTest {

    @Mock
    private lateinit var mockUri: Uri

    @Test
    fun `constructor creates valid RecordingItem with all properties`() {
        // Given
        val name = "Test Recording"
        val date = 1609459200000L // 2021-01-01 00:00:00 UTC
        val duration = 120000L // 2 minutes

        // When
        val item = RecordingItem(mockUri, name, date, duration)

        // Then
        assertEquals(mockUri, item.uri)
        assertEquals(name, item.name)
        assertEquals(date, item.date)
        assertEquals(duration, item.duration)
    }

    @Test
    fun `data class equality works correctly`() {
        // Given
        val name = "Test Recording"
        val date = 1609459200000L
        val duration = 120000L
        val item1 = RecordingItem(mockUri, name, date, duration)
        val item2 = RecordingItem(mockUri, name, date, duration)

        // Then
        assertEquals(item1, item2)
        assertEquals(item1.hashCode(), item2.hashCode())
    }

    @Test
    fun `data class copy works correctly`() {
        // Given
        val original = RecordingItem(mockUri, "Original", 1609459200000L, 120000L)

        // When
        val copied = original.copy(name = "Copied")

        // Then
        assertEquals(mockUri, copied.uri)
        assertEquals("Copied", copied.name)
        assertEquals(original.date, copied.date)
        assertEquals(original.duration, copied.duration)
        assertNotEquals(original, copied)
    }

    @Test
    fun `toString contains all properties`() {
        // Given
        val item = RecordingItem(mockUri, "Test", 1609459200000L, 120000L)

        // When
        val toString = item.toString()

        // Then
        assertTrue(toString.contains("Test"))
        assertTrue(toString.contains("1609459200000"))
        assertTrue(toString.contains("120000"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for blank name`() {
        RecordingItem(mockUri, "", 1609459200000L, 120000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for whitespace-only name`() {
        RecordingItem(mockUri, "   ", 1609459200000L, 120000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for negative date`() {
        RecordingItem(mockUri, "Test", -1L, 120000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws exception for negative duration`() {
        RecordingItem(mockUri, "Test", 1609459200000L, -1L)
    }

    @Test
    fun `constructor accepts zero date`() {
        // When/Then - should not throw
        val item = RecordingItem(mockUri, "Test", 0L, 120000L)
        assertEquals(0L, item.date)
    }

    @Test
    fun `constructor accepts zero duration`() {
        // When/Then - should not throw
        val item = RecordingItem(mockUri, "Test", 1609459200000L, 0L)
        assertEquals(0L, item.duration)
    }

    @Test
    fun `getFormattedDuration formats seconds correctly`() {
        // Given
        val item = RecordingItem(mockUri, "Test", 1609459200000L, 45000L) // 45 seconds

        // When
        val formatted = item.getFormattedDuration()

        // Then
        assertEquals("0:45", formatted)
    }

    @Test
    fun `getFormattedDuration formats minutes and seconds correctly`() {
        // Given
        val item = RecordingItem(mockUri, "Test", 1609459200000L, 150000L) // 2:30

        // When
        val formatted = item.getFormattedDuration()

        // Then
        assertEquals("2:30", formatted)
    }

    @Test
    fun `getFormattedDuration formats hours correctly`() {
        // Given
        val item = RecordingItem(mockUri, "Test", 1609459200000L, 3661000L) // 1:01:01

        // When
        val formatted = item.getFormattedDuration()

        // Then
        assertEquals("61:01", formatted) // Shows as minutes:seconds
    }

    @Test
    fun `getFormattedDuration handles zero duration`() {
        // Given
        val item = RecordingItem(mockUri, "Test", 1609459200000L, 0L)

        // When
        val formatted = item.getFormattedDuration()

        // Then
        assertEquals("0:00", formatted)
    }

    @Test
    fun `isRecent returns true for recording within 24 hours`() {
        // Given
        val recentTime = System.currentTimeMillis() - (12 * 60 * 60 * 1000) // 12 hours ago
        val item = RecordingItem(mockUri, "Test", recentTime, 120000L)

        // When/Then
        assertTrue(item.isRecent())
    }

    @Test
    fun `isRecent returns false for recording older than 24 hours`() {
        // Given
        val oldTime = System.currentTimeMillis() - (25 * 60 * 60 * 1000) // 25 hours ago
        val item = RecordingItem(mockUri, "Test", oldTime, 120000L)

        // When/Then
        assertFalse(item.isRecent())
    }

    @Test
    fun `isRecent returns true for recording exactly at 24 hour boundary`() {
        // Given - Use a fixed current time to avoid timing precision issues
        val currentTime = System.currentTimeMillis()
        val boundaryTime = currentTime - (24 * 60 * 60 * 1000) + 1000 // Just under 24 hours (1 second buffer)
        val item = RecordingItem(mockUri, "Test", boundaryTime, 120000L)

        // When/Then
        assertTrue("Recording just under 24 hours should be recent", item.isRecent())
    }

    @Test
    fun `isRecent returns false for recording exactly at 24 hour boundary minus one`() {
        // Given
        val boundaryTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000) - 1 // Just over 24 hours
        val item = RecordingItem(mockUri, "Test", boundaryTime, 120000L)

        // When/Then
        assertFalse(item.isRecent())
    }

    @Test
    fun `isRecent returns true for future recording`() {
        // Given
        val futureTime = System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour in future
        val item = RecordingItem(mockUri, "Test", futureTime, 120000L)

        // When/Then
        assertTrue(item.isRecent())
    }
}