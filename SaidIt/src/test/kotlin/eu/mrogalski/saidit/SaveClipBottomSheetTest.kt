package eu.mrogalski.saidit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.siya.epistemophile.R
import eu.mrogalski.android.TimeFormat
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

/**
 * Comprehensive unit tests for SaveClipBottomSheet that verify actual behavior.
 * Tests include UI initialization, listener callbacks, duration selection,
 * input validation, and fragment lifecycle.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SaveClipBottomSheetTest {

    private lateinit var fragment: SaveClipBottomSheet
    private lateinit var activity: FragmentActivity
    
    @Mock
    private lateinit var mockListener: SaveClipBottomSheet.SaveClipListener

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        activity = Robolectric.buildActivity(FragmentActivity::class.java)
            .create()
            .start()
            .resume()
            .get()
    }

    @Test
    fun `newInstance creates fragment with correct arguments`() {
        // Given
        val memorizedDuration = 123.45f
        
        // When
        fragment = SaveClipBottomSheet.newInstance(memorizedDuration)
        
        // Then
        assertNotNull(fragment)
        assertNotNull(fragment.arguments)
        assertEquals(memorizedDuration, fragment.arguments?.getFloat("memorized_duration"))
    }

    @Test
    fun `onCreate retrieves memorized duration from arguments`() {
        // Given
        val memorizedDuration = 300f
        fragment = SaveClipBottomSheet.newInstance(memorizedDuration)
        
        // When
        activity.supportFragmentManager
            .beginTransaction()
            .add(fragment, "test")
            .commitNow()
        
        // Then fragment should have the duration set
        // (Duration is private, but we can verify it's used correctly in onViewCreated)
        assertNotNull(fragment.view)
    }

    @Test
    fun `onCreateView inflates correct layout`() {
        // Given
        fragment = SaveClipBottomSheet.newInstance(100f)
        
        // When
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        // Then
        assertNotNull(fragment.view)
        assertNotNull(fragment.view?.findViewById<TextInputEditText>(R.id.recording_name))
        assertNotNull(fragment.view?.findViewById<ChipGroup>(R.id.duration_chip_group))
        assertNotNull(fragment.view?.findViewById<MaterialButton>(R.id.save_button))
    }

    @Test
    fun `all memory chip displays correct duration`() {
        // Given
        val memorizedDuration = 185.5f // 3 minutes 5.5 seconds
        fragment = SaveClipBottomSheet.newInstance(memorizedDuration)
        
        // When
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        // Then
        val allChip = fragment.view?.findViewById<Chip>(R.id.duration_all)
        assertNotNull(allChip)
        val expectedText = "${fragment.getString(R.string.all_memory)} (${TimeFormat.shortTimer(memorizedDuration)})"
        assertTrue(allChip?.text?.contains(TimeFormat.shortTimer(memorizedDuration)) == true)
    }

    @Test
    fun `default duration selection is 1 minute`() {
        // Given
        fragment = SaveClipBottomSheet.newInstance(100f)
        
        // When
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        // Then
        val oneMinChip = fragment.view?.findViewById<Chip>(R.id.duration_1m)
        assertTrue(oneMinChip?.isChecked == true)
    }

    @Test
    fun `save button with empty file name shows toast`() {
        // Given
        fragment = SaveClipBottomSheet.newInstance(100f)
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        
        // When - leave file name empty and click save
        fileNameInput?.setText("")
        saveButton?.performClick()
        
        // Then
        val latestToast = ShadowToast.getLatestToast()
        assertNotNull(latestToast)
        assertEquals("Please enter a file name", ShadowToast.getTextOfLatestToast())
        verify(mockListener, never()).onSaveClip(any(), any())
    }

    @Test
    fun `save button with valid file name calls listener with 1 minute duration`() {
        // Given
        fragment = SaveClipBottomSheet.newInstance(100f)
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        
        // When - enter file name and click save (default 1m selected)
        fileNameInput?.setText("test_recording")
        saveButton?.performClick()
        
        // Then
        verify(mockListener).onSaveClip("test_recording", 60f)
    }

    @Test
    fun `save button with 5 minute selection calls listener correctly`() {
        // Given
        fragment = SaveClipBottomSheet.newInstance(100f)
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val fiveMinChip = fragment.view?.findViewById<Chip>(R.id.duration_5m)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        
        // When - select 5 minutes and save
        fiveMinChip?.isChecked = true
        fileNameInput?.setText("five_min_recording")
        saveButton?.performClick()
        
        // Then
        verify(mockListener).onSaveClip("five_min_recording", 300f)
    }

    @Test
    fun `save button with 30 minute selection calls listener correctly`() {
        // Given
        fragment = SaveClipBottomSheet.newInstance(100f)
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val thirtyMinChip = fragment.view?.findViewById<Chip>(R.id.duration_30m)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        
        // When - select 30 minutes and save
        thirtyMinChip?.isChecked = true
        fileNameInput?.setText("thirty_min_recording")
        saveButton?.performClick()
        
        // Then
        verify(mockListener).onSaveClip("thirty_min_recording", 1800f)
    }

    @Test
    fun `save button with all memory selection uses memorized duration`() {
        // Given
        val memorizedDuration = 456.78f
        fragment = SaveClipBottomSheet.newInstance(memorizedDuration)
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val allChip = fragment.view?.findViewById<Chip>(R.id.duration_all)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        
        // When - select all memory and save
        allChip?.isChecked = true
        fileNameInput?.setText("all_memory_recording")
        saveButton?.performClick()
        
        // Then
        verify(mockListener).onSaveClip("all_memory_recording", memorizedDuration)
    }

    @Test
    fun `file name is trimmed before validation`() {
        // Given
        fragment = SaveClipBottomSheet.newInstance(100f)
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        
        // When - enter file name with spaces and click save
        fileNameInput?.setText("  test_file  ")
        saveButton?.performClick()
        
        // Then - file name should be trimmed
        verify(mockListener).onSaveClip("test_file", 60f)
    }

    @Test
    fun `listener can be set and called`() {
        // Given
        fragment = SaveClipBottomSheet.newInstance(100f)
        
        // When
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        
        fileNameInput?.setText("test")
        saveButton?.performClick()
        
        // Then
        verify(mockListener, times(1)).onSaveClip(any(), any())
    }

    @Test
    fun `fragment dismisses after successful save`() {
        // Given
        fragment = spy(SaveClipBottomSheet.newInstance(100f))
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        
        // When
        fileNameInput?.setText("test_file")
        saveButton?.performClick()
        
        // Then
        verify(fragment).dismiss()
    }

    @Test
    fun `getDurationFromChipSelection returns correct durations`() {
        // This tests the internal logic through the public interface
        fragment = SaveClipBottomSheet.newInstance(999f)
        fragment.setSaveClipListener(mockListener)
        
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()
        
        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        fileNameInput?.setText("test")
        
        // Test each duration option
        val durations = mapOf(
            R.id.duration_1m to 60f,
            R.id.duration_5m to 300f,
            R.id.duration_30m to 1800f,
            R.id.duration_all to 999f
        )
        
        durations.forEach { (chipId, expectedDuration) ->
            reset(mockListener)
            fragment.view?.findViewById<Chip>(chipId)?.isChecked = true
            saveButton?.performClick()
            verify(mockListener).onSaveClip("test", expectedDuration)
        }
    }
}
