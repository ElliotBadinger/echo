package eu.mrogalski.saidit

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.siya.epistemophile.R
import com.siya.epistemophile.testing.AndroidTestUtils.awaitBoundService
import com.siya.epistemophile.testing.AndroidTestUtils.waitFor
import com.siya.epistemophile.testing.AndroidTestUtils.waitUntil
import org.junit.*
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaidItFragmentTest {

    companion object {
        private const val PREF_FIRST_RUN = "is_first_run"
        private const val PREF_TOUR_NEXT = "show_tour_on_next_launch"
    }

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
    )

    @Before
    fun configurePreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences: SharedPreferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
        preferences.edit()
            .putBoolean(PREF_FIRST_RUN, false)
            .putBoolean(PREF_TOUR_NEXT, false)
            .apply()
        SaidItActivity.setTestSkipPermissionRequest(true)
    }

    @After
    fun resetFlags() {
        SaidItActivity.setTestSkipPermissionRequest(false)
    }

    @Test
    fun testSaveClipFlow_showsProgressDialog() {
        val scenario = ActivityScenario.launch(SaidItActivity::class.java)
        val service = awaitBoundService(scenario)
        service.isTestEnvironment = true
        service.testAutoSaveDelayMs = 400L
        service.enableListening()

        onView(withId(R.id.save_clip_button)).perform(click())
        onView(withId(R.id.recording_name)).perform(replaceText("Test clip"), closeSoftKeyboard())
        onView(withId(R.id.save_button)).perform(click())

        val appeared = waitUntil(scenario, timeoutMs = 5_000) { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.container) as? SaidItFragment
            val dialog: AlertDialog? = fragment?.let { findProgressDialog(it) }
            dialog?.isShowing == true
        }
        assertTrue("Progress dialog should appear and be showing", appeared)

        scenario.close()
    }
}

private fun findProgressDialog(fragment: SaidItFragment): AlertDialog? {
    val candidates = arrayOf(
        "getProgressDialogForTest",
        "getProgressDialogForTest\$SaidIt_debug",
        "getProgressDialogForTest\$SaidIt_release"
    )
    for (name in candidates) {
        try {
            val method = fragment::class.java.getMethod(name)
            val result = method.invoke(fragment)
            if (result is AlertDialog) return result
        } catch (_: Exception) {
            // try next
        }
    }
    return null
}
