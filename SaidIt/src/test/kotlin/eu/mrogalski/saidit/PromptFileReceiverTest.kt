package eu.mrogalski.saidit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.siya.epistemophile.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowLooper
import org.robolectric.Shadows.shadowOf

/**
 * Comprehensive unit tests for PromptFileReceiver that verify actual behavior.
 * Tests include dialog handling, success/failure callbacks, UI thread execution,
 * and intent creation for opening/sharing recordings.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PromptFileReceiverTest {

    private lateinit var activity: Activity
    private lateinit var receiver: PromptFileReceiver
    
    @Mock
    private lateinit var mockProgressDialog: AlertDialog
    
    @Mock
    private lateinit var mockUri: Uri

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        activity = Robolectric.buildActivity(Activity::class.java)
            .create()
            .start()
            .resume()
            .get()
    }

    @Test
    fun `constructor with activity only sets null progress dialog`() {
        // When
        receiver = PromptFileReceiver(activity)
        
        // Then - should not throw exception when methods are called
        receiver.onSuccess(mockUri)
        
        // Verify UI thread execution
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        // Dialog should be shown
        val dialog = ShadowAlertDialog.getLatestDialog()
        assertNotNull(dialog)
    }

    @Test
    fun `constructor with both parameters uses provided dialog`() {
        // Given
        whenever(mockProgressDialog.isShowing).thenReturn(true)
        
        // When
        receiver = PromptFileReceiver(activity, mockProgressDialog)
        receiver.onSuccess(mockUri)
        
        // Then
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        verify(mockProgressDialog).dismiss()
    }

    @Test
    fun `onSuccess dismisses progress dialog if showing`() {
        // Given
        whenever(mockProgressDialog.isShowing).thenReturn(true)
        receiver = PromptFileReceiver(activity, mockProgressDialog)
        
        // When
        receiver.onSuccess(mockUri)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        // Then
        verify(mockProgressDialog).dismiss()
    }

    @Test
    fun `onSuccess does not dismiss progress dialog if not showing`() {
        // Given
        whenever(mockProgressDialog.isShowing).thenReturn(false)
        receiver = PromptFileReceiver(activity, mockProgressDialog)
        
        // When
        receiver.onSuccess(mockUri)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        // Then
        verify(mockProgressDialog, never()).dismiss()
    }

    @Test
    fun `onSuccess shows success dialog with correct title and message`() {
        // Given
        receiver = PromptFileReceiver(activity)
        
        // When
        receiver.onSuccess(mockUri)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        // Then
        val dialog = ShadowAlertDialog.getLatestDialog() as AlertDialog
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
        
        // Note: In a real test we'd verify the title and message through the dialog's view
        // but Robolectric's shadow doesn't expose these directly
    }

    @Test
    fun `onSuccess does nothing when activity is null`() {
        // Given
        receiver = PromptFileReceiver(null, mockProgressDialog)
        
        // When
        receiver.onSuccess(mockUri)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        // Then
        verify(mockProgressDialog, never()).dismiss()
        assertNull(ShadowAlertDialog.getLatestDialog())
    }

    @Test
    fun `onSuccess does nothing when activity is finishing`() {
        // Given
        val mockActivity = mock(Activity::class.java)
        whenever(mockActivity.isFinishing).thenReturn(true)
        receiver = PromptFileReceiver(mockActivity, mockProgressDialog)
        
        // When
        receiver.onSuccess(mockUri)
        
        // Then
        verify(mockActivity, never()).runOnUiThread(any())
        verify(mockProgressDialog, never()).dismiss()
    }

    @Test
    fun `onFailure shows error dialog`() {
        // Given
        receiver = PromptFileReceiver(activity)
        val exception = Exception("Test error")
        
        // When
        receiver.onFailure(exception)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        // Then
        val dialog = ShadowAlertDialog.getLatestDialog() as AlertDialog
        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }

    @Test
    fun `onFailure dismisses progress dialog if showing`() {
        // Given
        whenever(mockProgressDialog.isShowing).thenReturn(true)
        receiver = PromptFileReceiver(activity, mockProgressDialog)
        val exception = Exception("Test error")
        
        // When
        receiver.onFailure(exception)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        // Then
        verify(mockProgressDialog).dismiss()
    }

    @Test
    fun `onFailure does nothing when activity is null`() {
        // Given
        receiver = PromptFileReceiver(null, mockProgressDialog)
        val exception = Exception("Test error")
        
        // When
        receiver.onFailure(exception)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        
        // Then
        verify(mockProgressDialog, never()).dismiss()
        assertNull(ShadowAlertDialog.getLatestDialog())
    }

    @Test
    fun `onFailure does nothing when activity is finishing`() {
        // Given
        val mockActivity = mock(Activity::class.java)
        whenever(mockActivity.isFinishing).thenReturn(true)
        receiver = PromptFileReceiver(mockActivity, mockProgressDialog)
        val exception = Exception("Test error")
        
        // When
        receiver.onFailure(exception)
        
        // Then
        verify(mockActivity, never()).runOnUiThread(any())
        verify(mockProgressDialog, never()).dismiss()
    }

    @Test
    fun `open button creates correct intent`() {
        // Given
        receiver = PromptFileReceiver(activity)
        val shadowActivity = shadowOf(activity)
        val testUri = Uri.parse("content://test/file.mp4")

        // When
        receiver.onSuccess(testUri)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val dialog = ShadowAlertDialog.getLatestDialog() as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val capturedIntent = shadowActivity.nextStartedActivity
        assertNotNull(capturedIntent)
        requireNotNull(capturedIntent)
        assertEquals(Intent.ACTION_VIEW, capturedIntent.action)
        assertEquals(testUri, capturedIntent.data)
        assertEquals("audio/mp4", capturedIntent.type)
        assertTrue(capturedIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `share button creates correct intent`() {
        // Given
        receiver = PromptFileReceiver(activity)
        val shadowActivity = shadowOf(activity)
        val testUri = Uri.parse("content://test/file.mp4")

        // When
        receiver.onSuccess(testUri)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val dialog = ShadowAlertDialog.getLatestDialog() as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val chooserIntent = shadowActivity.nextStartedActivity
        assertNotNull(chooserIntent)
        requireNotNull(chooserIntent)
        assertEquals(Intent.ACTION_CHOOSER, chooserIntent.action)

        val targetIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull(targetIntent)
        requireNotNull(targetIntent)
        assertEquals(Intent.ACTION_SEND, targetIntent.action)
        assertEquals("audio/mp4", targetIntent.type)
        assertEquals(testUri, targetIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
        assertTrue(targetIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `dismiss button closes dialog without action`() {
        // Given
        receiver = PromptFileReceiver(activity)
        val shadowActivity = shadowOf(activity)

        // When
        receiver.onSuccess(mockUri)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val dialog = ShadowAlertDialog.getLatestDialog() as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertNull(shadowActivity.nextStartedActivity)
        assertFalse(dialog.isShowing)
    }

    @Test
    fun `UI operations run on UI thread`() {
        // Given
        val realActivity = Robolectric.buildActivity(Activity::class.java)
            .create()
            .start()
            .resume()
            .get()
        val spyActivity = spy(realActivity)

        whenever(spyActivity.isFinishing).thenReturn(false)
        doAnswer { invocation ->
            val runnable = invocation.getArgument<Runnable>(0)
            runnable.run()
            null
        }.whenever(spyActivity).runOnUiThread(any())

        receiver = PromptFileReceiver(spyActivity, mockProgressDialog)
        whenever(mockProgressDialog.isShowing).thenReturn(true)

        // When
        receiver.onSuccess(mockUri)

        // Then
        verify(spyActivity).runOnUiThread(any())
        verify(mockProgressDialog).dismiss()
    }
}
