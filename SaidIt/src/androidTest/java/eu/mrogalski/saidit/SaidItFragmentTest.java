package eu.mrogalski.saidit;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.Matcher;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

import com.siya.epistemophile.R;

@RunWith(AndroidJUnit4.class)
public class SaidItFragmentTest {

    private static final String PREF_FIRST_RUN = "is_first_run";
    private static final String PREF_TOUR_NEXT = "show_tour_on_next_launch";

    @Rule
    public final GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
    );

    @Before
    public void configurePreferences() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(PREF_FIRST_RUN, false)
                .putBoolean(PREF_TOUR_NEXT, false)
                .apply();
        SaidItActivity.setTestSkipPermissionRequest(true);
    }

    @After
    public void resetFlags() {
        SaidItActivity.setTestSkipPermissionRequest(false);
    }

    @Test
    public void testSaveClipFlow_showsProgressDialog() {
        ActivityScenario<SaidItActivity> scenario = ActivityScenario.launch(SaidItActivity.class);
        SaidItService service = awaitBoundService(scenario);
        service.setTestEnvironment(true);
        service.setTestAutoSaveDelayMs(400L);
        service.enableListening();

        onView(withId(R.id.save_clip_button)).perform(click());
        onView(withId(R.id.recording_name)).perform(replaceText("Test clip"), closeSoftKeyboard());
        onView(withId(R.id.save_button)).perform(click());
        onView(isRoot()).perform(waitFor(250));

        onView(withText("Saving Recording"))
                .inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));

        scenario.close();
    }

    private SaidItService awaitBoundService(ActivityScenario<SaidItActivity> scenario) {
        AtomicReference<SaidItService> reference = new AtomicReference<>();
        long start = SystemClock.uptimeMillis();
        long timeout = 15_000L;

        while (reference.get() == null && SystemClock.uptimeMillis() - start < timeout) {
            scenario.onActivity(activity -> {
                activity.ensureServiceBoundForTest();
                reference.set(activity.getEchoService());
            });
            if (reference.get() != null) {
                break;
            }
            SystemClock.sleep(50);
        }

        SaidItService service = reference.get();
        assertNotNull("Service should be bound before interacting with UI", service);
        return service;
    }

    private ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for " + millis + " milliseconds";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
