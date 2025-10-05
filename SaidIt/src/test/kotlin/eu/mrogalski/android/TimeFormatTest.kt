package eu.mrogalski.android

import android.content.res.Resources
import com.siya.epistemophile.R
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Comprehensive unit tests for TimeFormat utility class.
 * Tests all functionality including edge cases, error handling, and integration scenarios.
 */
class TimeFormatTest {

    @Mock
    private lateinit var mockResources: Resources

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Mock resource strings for testing
        `when`(mockResources.getQuantityString(eq(R.plurals.minute), eq(1), eq(1)))
            .thenReturn("1 minute")
        `when`(mockResources.getQuantityString(eq(R.plurals.minute), eq(2), eq(2)))
            .thenReturn("2 minutes")
        `when`(mockResources.getQuantityString(eq(R.plurals.minute), eq(5), eq(5)))
            .thenReturn("5 minutes")
        
        `when`(mockResources.getQuantityString(eq(R.plurals.second), eq(1), eq(1)))
            .thenReturn("1 second")
        `when`(mockResources.getQuantityString(eq(R.plurals.second), eq(30), eq(30)))
            .thenReturn("30 seconds")
        `when`(mockResources.getQuantityString(eq(R.plurals.second), eq(45), eq(45)))
            .thenReturn("45 seconds")
        
        `when`(mockResources.getString(R.string.minute_second_join))
            .thenReturn(" and ")
    }

    // ========== naturalLanguage(Resources, Float) Tests ==========

    @Test
    fun `naturalLanguage should format seconds only`() {
        val result = TimeFormat.naturalLanguage(mockResources, 30.0f)
        
        assertEquals("30 seconds.", result.text)
        assertEquals(30, result.count)
    }

    @Test
    fun `naturalLanguage should format minutes only`() {
        val result = TimeFormat.naturalLanguage(mockResources, 120.0f) // 2 minutes
        
        assertEquals("2 minutes.", result.text)
        assertEquals(2, result.count)
    }

    @Test
    fun `naturalLanguage should format minutes and seconds`() {
        val result = TimeFormat.naturalLanguage(mockResources, 150.0f) // 2 minutes 30 seconds
        
        assertEquals("2 minutes and 30 seconds.", result.text)
        assertEquals(2, result.count) // Count should be minutes when minutes > 0
    }

    @Test
    fun `naturalLanguage should handle zero seconds`() {
        `when`(mockResources.getQuantityString(eq(R.plurals.second), eq(0), eq(0)))
            .thenReturn("0 seconds")
            
        val result = TimeFormat.naturalLanguage(mockResources, 0.0f)
        
        assertEquals("0 seconds.", result.text)
        assertEquals(0, result.count)
    }

    @Test
    fun `naturalLanguage should floor float values`() {
        val result = TimeFormat.naturalLanguage(mockResources, 30.9f)
        
        assertEquals("30 seconds.", result.text)
        assertEquals(30, result.count)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `naturalLanguage should throw exception for negative seconds`() {
        TimeFormat.naturalLanguage(mockResources, -1.0f)
    }

    // ========== naturalLanguage(Resources, Float, Result) Tests ==========

    @Test
    fun `naturalLanguage with result parameter should populate result object`() {
        val result = TimeFormat.Result()
        
        TimeFormat.naturalLanguage(mockResources, 90.0f, result) // 1 minute 30 seconds
        
        assertEquals("1 minute and 30 seconds.", result.text)
        assertEquals(1, result.count)
    }

    @Test
    fun `naturalLanguage with result parameter should overwrite existing result`() {
        val result = TimeFormat.Result("old text", 999)
        
        TimeFormat.naturalLanguage(mockResources, 45.0f, result)
        
        assertEquals("45 seconds.", result.text)
        assertEquals(45, result.count)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `naturalLanguage with result parameter should throw exception for negative seconds`() {
        val result = TimeFormat.Result()
        TimeFormat.naturalLanguage(mockResources, -5.0f, result)
    }

    // ========== shortTimer Tests ==========

    @Test
    fun `shortTimer should format seconds only`() {
        val result = TimeFormat.shortTimer(45.0f)
        assertEquals("0:45", result)
    }

    @Test
    fun `shortTimer should format minutes and seconds`() {
        val result = TimeFormat.shortTimer(125.0f) // 2 minutes 5 seconds
        assertEquals("2:05", result)
    }

    @Test
    fun `shortTimer should pad seconds with zero`() {
        val result = TimeFormat.shortTimer(65.0f) // 1 minute 5 seconds
        assertEquals("1:05", result)
    }

    @Test
    fun `shortTimer should handle zero seconds`() {
        val result = TimeFormat.shortTimer(0.0f)
        assertEquals("0:00", result)
    }

    @Test
    fun `shortTimer should handle exact minutes`() {
        val result = TimeFormat.shortTimer(180.0f) // 3 minutes exactly
        assertEquals("3:00", result)
    }

    @Test
    fun `shortTimer should floor float values`() {
        val result = TimeFormat.shortTimer(65.9f) // 1 minute 5.9 seconds
        assertEquals("1:05", result)
    }

    @Test
    fun `shortTimer should handle large values`() {
        val result = TimeFormat.shortTimer(3665.0f) // 61 minutes 5 seconds
        assertEquals("61:05", result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `shortTimer should throw exception for negative seconds`() {
        TimeFormat.shortTimer(-1.0f)
    }

    // ========== Result Class Tests ==========

    @Test
    fun `Result default constructor should initialize with empty values`() {
        val result = TimeFormat.Result()
        
        assertEquals("", result.text)
        assertEquals(0, result.count)
    }

    @Test
    fun `Result parameterized constructor should set values correctly`() {
        val result = TimeFormat.Result("test text", 42)
        
        assertEquals("test text", result.text)
        assertEquals(42, result.count)
    }

    @Test
    fun `Result should provide text property access`() {
        val result = TimeFormat.Result("sample text", 10)
        assertEquals("sample text", result.text)
    }

    @Test
    fun `Result should provide count property access`() {
        val result = TimeFormat.Result("sample text", 15)
        assertEquals(15, result.count)
    }

    @Test
    fun `Result should support mutation`() {
        val result = TimeFormat.Result()
        
        result.text = "new text"
        result.count = 100
        
        assertEquals("new text", result.text)
        assertEquals(100, result.count)
    }

    @Test
    fun `Result data class should provide proper equals`() {
        val result1 = TimeFormat.Result("same", 42)
        val result2 = TimeFormat.Result("same", 42)
        val result3 = TimeFormat.Result("different", 42)
        
        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
    }

    @Test
    fun `Result data class should provide proper hashCode`() {
        val result1 = TimeFormat.Result("same", 42)
        val result2 = TimeFormat.Result("same", 42)
        
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun `Result data class should provide proper toString`() {
        val result = TimeFormat.Result("test", 123)
        val toString = result.toString()
        
        assertTrue(toString.contains("test"))
        assertTrue(toString.contains("123"))
    }

    // ========== Integration Tests ==========

    @Test
    fun `integration test with realistic time values`() {
        // Test common recording durations
        val thirtySeconds = TimeFormat.naturalLanguage(mockResources, 30.0f)
        val oneMinute = TimeFormat.naturalLanguage(mockResources, 60.0f)
        val fiveMinutes = TimeFormat.naturalLanguage(mockResources, 300.0f)
        
        assertEquals(30, thirtySeconds.count)
        assertEquals(1, oneMinute.count)
        assertEquals(5, fiveMinutes.count)
        
        // Verify short timer format for same values
        assertEquals("0:30", TimeFormat.shortTimer(30.0f))
        assertEquals("1:00", TimeFormat.shortTimer(60.0f))
        assertEquals("5:00", TimeFormat.shortTimer(300.0f))
    }

    @Test
    fun `edge case test with boundary values`() {
        // Test exactly 1 minute boundary
        val fiftyNineSeconds = TimeFormat.shortTimer(59.0f)
        val sixtySeconds = TimeFormat.shortTimer(60.0f)
        val sixtyOneSeconds = TimeFormat.shortTimer(61.0f)
        
        assertEquals("0:59", fiftyNineSeconds)
        assertEquals("1:00", sixtySeconds)
        assertEquals("1:01", sixtyOneSeconds)
    }

    // ========== Error Handling Tests ==========

    @Test
    fun `error handling should work consistently across methods`() {
        val negativeValue = -10.0f
        
        try {
            TimeFormat.naturalLanguage(mockResources, negativeValue)
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Total seconds cannot be negative", e.message)
        }
        
        try {
            TimeFormat.shortTimer(negativeValue)
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Total seconds cannot be negative", e.message)
        }
    }
}