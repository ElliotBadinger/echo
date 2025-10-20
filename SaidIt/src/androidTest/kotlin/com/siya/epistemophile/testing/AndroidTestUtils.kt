package com.siya.epistemophile.testing

import android.os.SystemClock
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import eu.mrogalski.saidit.SaidItActivity
import eu.mrogalski.saidit.SaidItService
import org.hamcrest.Matcher
import org.junit.Assert.assertNotNull
import java.util.concurrent.atomic.AtomicReference

/**
 * Shared helpers for Android instrumentation tests.
 */
object AndroidTestUtils {

    /**
     * Espresso-compatible delay that advances the main looper.
     */
    @JvmStatic
    fun waitFor(millis: Long): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription(): String = "wait for $millis milliseconds"
        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }

    /**
     * Repeatedly pings the activity to ensure the service is bound, returning the instance when available.
     */
    @JvmStatic
    fun awaitBoundService(
        scenario: ActivityScenario<SaidItActivity>,
        timeoutMs: Long = 15_000L
    ): SaidItService {
        val reference = AtomicReference<SaidItService?>()
        val start = SystemClock.uptimeMillis()
        while (reference.get() == null && SystemClock.uptimeMillis() - start < timeoutMs) {
            scenario.onActivity { activity ->
                activity.ensureServiceBoundForTest()
                reference.set(activity.getEchoService())
            }
            if (reference.get() != null) break
            SystemClock.sleep(50)
        }
        val service = reference.get()
        assertNotNull("Service should bind", service)
        return service!!
    }

    /**
     * Polls the activity until [condition] returns true or times out.
     */
    @JvmStatic
    fun waitUntil(
        scenario: ActivityScenario<SaidItActivity>,
        timeoutMs: Long = 5_000L,
        condition: (SaidItActivity) -> Boolean
    ): Boolean {
        val start = SystemClock.uptimeMillis()
        var met = false
        while (!met && SystemClock.uptimeMillis() - start < timeoutMs) {
            scenario.onActivity { activity ->
                met = condition(activity)
            }
            if (met) break
            SystemClock.sleep(50)
        }
        return met
    }
}
