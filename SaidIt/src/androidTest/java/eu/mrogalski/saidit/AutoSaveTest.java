package eu.mrogalski.saidit;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AutoSaveTest {

    @Rule
    public final GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
    );

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SaidItActivity.setTestSkipPermissionRequest(true);
    }

    @After
    public void tearDown() {
        SaidItActivity.setTestSkipPermissionRequest(false);
        SharedPreferences preferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    @Test
    public void testAutoSaveDoesNotCrashService() throws InterruptedException {
        SharedPreferences preferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean("auto_save_enabled", true)
                .putInt("auto_save_duration", 1)
                .putBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, false)
                .apply();

        CountDownLatch latch = new CountDownLatch(1);
        ActivityScenario<SaidItActivity> scenario = ActivityScenario.launch(SaidItActivity.class);
        SaidItService service = awaitBoundService(scenario);

        service.setTestEnvironment(true);
        service.enableListening();
        service.triggerAutoSaveForTest(1, new SaidItService.WavFileReceiver() {
            @Override
            public void onSuccess(android.net.Uri fileUri) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        });

        boolean completed = latch.await(15, TimeUnit.SECONDS);
        scenario.close();
        assertTrue("Auto-save callback should complete", completed);
    }

    private SaidItService awaitBoundService(ActivityScenario<SaidItActivity> scenario) {
        AtomicReference<SaidItService> reference = new AtomicReference<>();
        long start = SystemClock.uptimeMillis();
        long timeoutMillis = 15_000L;

        while (reference.get() == null && SystemClock.uptimeMillis() - start < timeoutMillis) {
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
        assertNotNull("Service should bind", service);
        return service;
    }
}
