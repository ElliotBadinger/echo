package eu.mrogalski.saidit

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.siya.epistemophile.testing.AndroidTestUtils.awaitBoundService
import org.junit.*
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AutoSaveTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
    )

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        SaidItActivity.setTestSkipPermissionRequest(true)
    }

    @After
    fun tearDown() {
        SaidItActivity.setTestSkipPermissionRequest(false)
        val preferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
    }

    @Test
    fun testAutoSaveDoesNotCrashService() {
        val preferences: SharedPreferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
        preferences.edit()
            .putBoolean("auto_save_enabled", true)
            .putInt("auto_save_duration", 1)
            .putBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, false)
            .apply()

        val latch = CountDownLatch(1)
        val scenario = ActivityScenario.launch(SaidItActivity::class.java)
        val service = awaitBoundService(scenario)

        service.isTestEnvironment = true
        service.enableListening()
        service.triggerAutoSaveForTest(1, object : SaidItService.WavFileReceiver {
            override fun onSuccess(fileUri: android.net.Uri) { latch.countDown() }
            override fun onFailure(e: Exception) { latch.countDown() }
        })

        val completed = latch.await(15, TimeUnit.SECONDS)
        scenario.close()
        assertTrue("Auto-save callback should complete", completed)
    }
}

