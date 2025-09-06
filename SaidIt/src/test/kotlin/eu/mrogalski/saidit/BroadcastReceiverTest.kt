package eu.mrogalski.saidit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

/**
 * Comprehensive unit tests for BroadcastReceiver.kt
 * 
 * Tests cover all scenarios including null safety, SharedPreferences handling,
 * service starting logic, and error conditions. Uses Mockito for mocking
 * and Robolectric for Android context testing.
 */
@RunWith(RobolectricTestRunner::class)
class BroadcastReceiverTest {
    
    private lateinit var broadcastReceiver: BroadcastReceiver
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockApplicationContext: Context
    
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var mockIntent: Intent
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        broadcastReceiver = BroadcastReceiver()
        
        // Setup default mock behavior
        `when`(mockContext.applicationContext).thenReturn(mockApplicationContext)
        `when`(mockApplicationContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
    }
    
    @Test
    fun `onReceive should start service when tutorial is completed`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("skip_tutorial", false)).thenReturn(true)
        
        // When
        broadcastReceiver.onReceive(mockContext, mockIntent)
        
        // Then
        verify(mockContext).startService(any(Intent::class.java))
        verify(mockApplicationContext).getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
        verify(mockSharedPreferences).getBoolean("skip_tutorial", false)
    }
    
    @Test
    fun `onReceive should not start service when tutorial is not completed`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("skip_tutorial", false)).thenReturn(false)
        
        // When
        broadcastReceiver.onReceive(mockContext, mockIntent)
        
        // Then
        verify(mockContext, never()).startService(any(Intent::class.java))
        verify(mockApplicationContext).getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
        verify(mockSharedPreferences).getBoolean("skip_tutorial", false)
    }
    
    @Test
    fun `onReceive should handle null context gracefully`() {
        // When - should not crash
        broadcastReceiver.onReceive(null, mockIntent)
        
        // Then - no interactions should occur
        verifyNoInteractions(mockContext)
    }
    
    @Test
    fun `onReceive should handle null intent gracefully`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("skip_tutorial", false)).thenReturn(true)
        
        // When
        broadcastReceiver.onReceive(mockContext, null)
        
        // Then - should still work normally (intent is not used in current implementation)
        verify(mockContext).startService(any(Intent::class.java))
        verify(mockApplicationContext).getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
    }
    
    @Test
    fun `onReceive should handle both null context and intent gracefully`() {
        // When - should not crash
        broadcastReceiver.onReceive(null, null)
        
        // Then - no interactions should occur
        verifyNoInteractions(mockContext)
    }
    
    @Test
    fun `onReceive should use application context for SharedPreferences`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("skip_tutorial", false)).thenReturn(true)
        
        // When
        broadcastReceiver.onReceive(mockContext, mockIntent)
        
        // Then
        verify(mockContext).applicationContext
        verify(mockApplicationContext).getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
        verify(mockContext, never()).getSharedPreferences(anyString(), anyInt())
    }
    
    @Test
    fun `onReceive should handle SharedPreferences exception gracefully`() {
        // Given
        `when`(mockApplicationContext.getSharedPreferences(anyString(), anyInt()))
            .thenThrow(RuntimeException("SharedPreferences error"))
        
        // When - should not crash
        broadcastReceiver.onReceive(mockContext, mockIntent)
        
        // Then - should not start service
        verify(mockContext, never()).startService(any(Intent::class.java))
    }
    
    @Test
    fun `onReceive should handle startService exception gracefully`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("skip_tutorial", false)).thenReturn(true)
        `when`(mockContext.startService(any(Intent::class.java)))
            .thenThrow(SecurityException("Service start error"))
        
        // When - should not crash
        broadcastReceiver.onReceive(mockContext, mockIntent)
        
        // Then - startService was attempted
        verify(mockContext).startService(any(Intent::class.java))
    }
    
    @Test
    fun `onReceive performance test - should complete quickly`() {
        // Given
        `when`(mockSharedPreferences.getBoolean("skip_tutorial", false)).thenReturn(true)
        
        // When
        val startTime = System.currentTimeMillis()
        broadcastReceiver.onReceive(mockContext, mockIntent)
        val endTime = System.currentTimeMillis()
        
        // Then - should complete in reasonable time (less than 100ms)
        val executionTime = endTime - startTime
        assert(executionTime < 100) { "onReceive took too long: ${executionTime}ms" }
        
        verify(mockContext).startService(any(Intent::class.java))
    }
}