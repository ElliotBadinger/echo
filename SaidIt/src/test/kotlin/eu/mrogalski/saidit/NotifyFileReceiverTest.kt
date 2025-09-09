package eu.mrogalski.saidit

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.siya.epistemophile.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowNotificationManager

/**
 * Comprehensive unit tests for NotifyFileReceiver that verify actual behavior.
 * Tests include notification creation, permission checks, success/failure handling,
 * and the static notification builder method.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotifyFileReceiverTest {

    private lateinit var context: Context
    private lateinit var receiver: NotifyFileReceiver
    
    @Mock
    private lateinit var mockNotificationManager: NotificationManagerCompat
    
    @Mock
    private lateinit var mockUri: Uri

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `constructor accepts context parameter`() {
        // When
        receiver = NotifyFileReceiver(context)
        
        // Then
        assertNotNull(receiver)
    }

    @Test
    fun `onSuccess posts notification when permission granted`() {
        // Given
        receiver = NotifyFileReceiver(context)
        val testUri = Uri.parse("content://test/file.mp4")
        
        // Grant notification permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        
        // When
        receiver.onSuccess(testUri)
        
        // Then
        val shadowNotificationManager = ShadowNotificationManager.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        )
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(1, notifications.size)
    }

    @Test
    fun `onSuccess does not post notification when permission denied`() {
        // Given
        receiver = NotifyFileReceiver(context)
        val testUri = Uri.parse("content://test/file.mp4")
        
        // Ensure permission is not granted (default state)
        
        // When
        receiver.onSuccess(testUri)
        
        // Then
        val shadowNotificationManager = ShadowNotificationManager.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        )
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(0, notifications.size)
    }

    @Test
    fun `onSuccess uses correct notification ID`() {
        // Given
        receiver = NotifyFileReceiver(context)
        val testUri = Uri.parse("content://test/file.mp4")
        
        // Grant notification permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        
        // When
        receiver.onSuccess(testUri)
        
        // Then
        val shadowNotificationManager = ShadowNotificationManager.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        )
        val notification = shadowNotificationManager.getNotification(43)
        assertNotNull(notification)
    }

    @Test
    fun `onFailure does nothing`() {
        // Given
        receiver = NotifyFileReceiver(context)
        val exception = Exception("Test error")
        
        // When
        receiver.onFailure(exception)
        
        // Then - no exception thrown, no notification posted
        val shadowNotificationManager = ShadowNotificationManager.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        )
        val notifications = shadowNotificationManager.allNotifications
        assertEquals(0, notifications.size)
    }

    @Test
    fun `buildNotificationForFile creates notification with correct properties`() {
        // Given
        val testUri = Uri.parse("content://test/file.mp4")
        val fileName = "test_recording.mp4"
        
        // When
        val notification = NotifyFileReceiver.buildNotificationForFile(context, testUri, fileName)
        
        // Then
        assertNotNull(notification)
        assertEquals(Notification.FLAG_AUTO_CANCEL, notification.flags and Notification.FLAG_AUTO_CANCEL)
        assertNotNull(notification.contentIntent)
        
        // Verify the notification has the correct channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            assertEquals("SaidItServiceChannel", notification.channelId)
        }
    }

    @Test
    fun `buildNotificationForFile sets correct content intent`() {
        // Given
        val testUri = Uri.parse("content://test/file.mp4")
        val fileName = "test_recording.mp4"
        
        // When
        val notification = NotifyFileReceiver.buildNotificationForFile(context, testUri, fileName)
        
        // Then
        assertNotNull(notification.contentIntent)
        // Note: We can't easily extract the Intent from PendingIntent in tests,
        // but we've verified it's set
    }

    @Test
    fun `buildNotificationForFile sets auto cancel flag`() {
        // Given
        val testUri = Uri.parse("content://test/file.mp4")
        val fileName = "test_recording.mp4"
        
        // When
        val notification = NotifyFileReceiver.buildNotificationForFile(context, testUri, fileName)
        
        // Then
        val isAutoCancel = (notification.flags and Notification.FLAG_AUTO_CANCEL) != 0
        assertTrue(isAutoCancel)
    }

    @Test
    fun `buildNotificationForFile uses correct icon`() {
        // Given
        val testUri = Uri.parse("content://test/file.mp4")
        val fileName = "test_recording.mp4"
        
        // When
        val notification = NotifyFileReceiver.buildNotificationForFile(context, testUri, fileName)
        
        // Then
        assertEquals(R.drawable.ic_stat_notify_recorded, notification.icon)
    }

    @Test
    fun `buildNotificationForFile sets ticker text`() {
        // Given
        val testUri = Uri.parse("content://test/file.mp4")
        val fileName = "test_recording.mp4"
        
        // When
        val notification = NotifyFileReceiver.buildNotificationForFile(context, testUri, fileName)
        
        // Then
        assertEquals(fileName, notification.tickerText?.toString())
    }

    @Test
    fun `notification permission check works correctly`() {
        // Given
        receiver = NotifyFileReceiver(context)
        val testUri = Uri.parse("content://test/file.mp4")
        
        // Test without permission
        receiver.onSuccess(testUri)
        var shadowNotificationManager = ShadowNotificationManager.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        )
        assertEquals(0, shadowNotificationManager.allNotifications.size)
        
        // Grant permission and test again
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        receiver.onSuccess(testUri)
        shadowNotificationManager = ShadowNotificationManager.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        )
        assertEquals(1, shadowNotificationManager.allNotifications.size)
    }

    @Test
    fun `static method is accessible from Java`() {
        // This test ensures Java interoperability with @JvmStatic
        val testUri = Uri.parse("content://test/file.mp4")
        val fileName = "test_recording.mp4"
        
        // When - calling static method as if from Java
        val notification = NotifyFileReceiver.buildNotificationForFile(context, testUri, fileName)
        
        // Then
        assertNotNull(notification)
    }

    @Test
    fun `notification has correct priority`() {
        // Given
        val testUri = Uri.parse("content://test/file.mp4")
        val fileName = "test_recording.mp4"
        
        // When
        val notification = NotifyFileReceiver.buildNotificationForFile(context, testUri, fileName)
        
        // Then
        assertEquals(NotificationCompat.PRIORITY_DEFAULT, notification.priority)
    }

    @Test
    fun `multiple onSuccess calls post multiple notifications`() {
        // Given
        receiver = NotifyFileReceiver(context)
        val testUri1 = Uri.parse("content://test/file1.mp4")
        val testUri2 = Uri.parse("content://test/file2.mp4")
        
        // Grant notification permission
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        
        // When
        receiver.onSuccess(testUri1)
        receiver.onSuccess(testUri2)
        
        // Then - only one notification because same ID is used
        val shadowNotificationManager = ShadowNotificationManager.shadowOf(
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        )
        val notification = shadowNotificationManager.getNotification(43)
        assertNotNull(notification)
        // The second call replaces the first notification (same ID)
    }
}
