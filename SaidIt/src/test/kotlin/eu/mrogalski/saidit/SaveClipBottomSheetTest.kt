package eu.mrogalski.saidit

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.siya.epistemophile.R
import eu.mrogalski.android.TimeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SaveClipBottomSheetTest {

    private lateinit var activity: FragmentActivity

    private data class SaveEvent(val fileName: String, val duration: Float)

    private class RecordingListener : SaveClipBottomSheet.SaveClipListener {
        private val recorded = mutableListOf<SaveEvent>()
        val events: List<SaveEvent> get() = recorded

        override fun onSaveClip(fileName: String, durationInSeconds: Float) {
            recorded += SaveEvent(fileName, durationInSeconds)
        }

        fun reset() {
            recorded.clear()
        }
    }

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java)
            .create()
            .start()
            .resume()
            .get()
    }

    private fun launchFragment(
        memorizedDuration: Float = 100f,
        listener: RecordingListener? = null
    ): Pair<SaveClipBottomSheet, RecordingListener?> {
        val fragment = SaveClipBottomSheet.newInstance(memorizedDuration)
        listener?.let { fragment.setSaveClipListener(it) }

        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test")
            .commitNow()

        return fragment to listener
    }

    @Test
    fun `newInstance creates fragment with correct arguments`() {
        val memorizedDuration = 123.45f
        val fragment = SaveClipBottomSheet.newInstance(memorizedDuration)

        assertNotNull(fragment)
        assertNotNull(fragment.arguments)
        assertEquals(memorizedDuration, fragment.arguments?.getFloat("memorized_duration"))
    }

    @Test
    fun `onCreate retrieves memorized duration from arguments`() {
        val fragment = SaveClipBottomSheet.newInstance(300f)

        activity.supportFragmentManager
            .beginTransaction()
            .add(fragment, "test")
            .commitNow()

        assertNotNull(fragment.view)
    }

    @Test
    fun `onCreateView inflates correct layout`() {
        val (fragment, _) = launchFragment()

        assertNotNull(fragment.view)
        assertNotNull(fragment.view?.findViewById<TextInputEditText>(R.id.recording_name))
        assertNotNull(fragment.view?.findViewById<ChipGroup>(R.id.duration_chip_group))
        assertNotNull(fragment.view?.findViewById<MaterialButton>(R.id.save_button))
    }

    @Test
    fun `all memory chip displays correct duration`() {
        val memorizedDuration = 185.5f
        val (fragment, _) = launchFragment(memorizedDuration)

        val allChip = fragment.view?.findViewById<Chip>(R.id.duration_all)
        assertNotNull(allChip)
        val expectedText = "${fragment.getString(R.string.all_memory)} (${TimeFormat.shortTimer(memorizedDuration)})"
        assertEquals(expectedText, allChip?.text)
    }

    @Test
    fun `default duration selection is 1 minute`() {
        val (fragment, _) = launchFragment()
        val oneMinuteChip = fragment.view?.findViewById<Chip>(R.id.duration_1m)

        assertTrue(oneMinuteChip?.isChecked == true)
    }

    @Test
    fun `save button with empty file name shows toast`() {
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(listener = listener)

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)

        fileNameInput?.setText("")
        saveButton?.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val latestToast = ShadowToast.getLatestToast()
        assertNotNull(latestToast)
        assertEquals("Please enter a file name", ShadowToast.getTextOfLatestToast())
        assertTrue(listener.events.isEmpty())
    }

    @Test
    fun `save button with valid file name calls listener with 1 minute duration`() {
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(listener = listener)

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)

        fileNameInput?.setText("test_recording")
        saveButton?.performClick()

        val event = listener.events.single()
        assertEquals("test_recording", event.fileName)
        assertEquals(60f, event.duration)
    }

    @Test
    fun `save button with 5 minute selection calls listener correctly`() {
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(listener = listener)

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val fiveMinuteChip = fragment.view?.findViewById<Chip>(R.id.duration_5m)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)

        fiveMinuteChip?.performClick()
        fileNameInput?.setText("five_min_recording")
        saveButton?.performClick()

        val event = listener.events.single()
        assertEquals("five_min_recording", event.fileName)
        assertEquals(300f, event.duration)
    }

    @Test
    fun `save button with 30 minute selection calls listener correctly`() {
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(listener = listener)

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val thirtyMinuteChip = fragment.view?.findViewById<Chip>(R.id.duration_30m)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)

        thirtyMinuteChip?.performClick()
        fileNameInput?.setText("thirty_min_recording")
        saveButton?.performClick()

        val event = listener.events.single()
        assertEquals("thirty_min_recording", event.fileName)
        assertEquals(1800f, event.duration)
    }

    @Test
    fun `save button with all memory selection uses memorized duration`() {
        val memorizedDuration = 456.78f
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(memorizedDuration, listener)

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val allChip = fragment.view?.findViewById<Chip>(R.id.duration_all)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)

        allChip?.performClick()
        fileNameInput?.setText("all_memory_recording")
        saveButton?.performClick()

        val event = listener.events.single()
        assertEquals("all_memory_recording", event.fileName)
        assertEquals(memorizedDuration, event.duration)
    }

    @Test
    fun `file name is trimmed before validation`() {
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(listener = listener)

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)

        fileNameInput?.setText("  test_file  ")
        saveButton?.performClick()

        val event = listener.events.single()
        assertEquals("test_file", event.fileName)
        assertEquals(60f, event.duration)
    }

    @Test
    fun `listener can be set and called`() {
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(listener = listener)

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)

        fileNameInput?.setText("test")
        saveButton?.performClick()

        assertEquals(1, listener.events.size)
    }

    @Test
    fun `fragment dismisses after successful save`() {
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(listener = listener)

        var wasDetached = false
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                    if (f === fragment) {
                        wasDetached = true
                    }
                }
            },
            false
        )

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)

        fileNameInput?.setText("test_file")
        saveButton?.performClick()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue(wasDetached)
    }

    @Test
    fun `getDurationFromChipSelection returns correct durations`() {
        val listener = RecordingListener()
        val (fragment, _) = launchFragment(999f, listener)

        val fileNameInput = fragment.view?.findViewById<TextInputEditText>(R.id.recording_name)
        val saveButton = fragment.view?.findViewById<MaterialButton>(R.id.save_button)
        fileNameInput?.setText("test")

        val durations = mapOf(
            R.id.duration_1m to 60f,
            R.id.duration_5m to 300f,
            R.id.duration_30m to 1800f,
            R.id.duration_all to 999f
        )

        durations.forEach { (chipId, expectedDuration) ->
            listener.reset()
            fragment.view?.findViewById<Chip>(chipId)?.performClick()
            saveButton?.performClick()
            val event = listener.events.single()
            assertEquals("test", event.fileName)
            assertEquals(expectedDuration, event.duration)
        }
    }
}
