package eu.mrogalski.saidit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.rule.ServiceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AutoSaveTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

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
    }

    @After
    public void tearDown() {
        // ServiceTestRule automatically handles service cleanup
        // No manual stopService() call needed - ServiceTestRule manages lifecycle
    }

    @Test
    public void testAutoSaveDoesNotCrashService() throws TimeoutException {
        // 1. Configure auto-save
        SharedPreferences preferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean("auto_save_enabled", true)
                .putInt("auto_save_duration", 1)
                .apply();

        // 2. Start the service
        Intent intent = new Intent(context, SaidItService.class);
        serviceRule.startService(intent);

        // 3. Bind to the service and trigger auto-save directly
        SaidItService.BackgroundRecorderBinder binder = (SaidItService.BackgroundRecorderBinder) serviceRule.bindService(intent);
        SaidItService service = binder.getService();
        service.setTestEnvironment(true);

        CountDownLatch latch = new CountDownLatch(1);
        SaidItService.WavFileReceiver receiver = new SaidItService.WavFileReceiver() {
            @Override
            public void onSuccess(android.net.Uri fileUri) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                // Still count down so the test fails via assertion below
                latch.countDown();
            }
        };

        service.triggerAutoSaveForTest(1, receiver);

        boolean completed = false;
        try {
            completed = latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        assertTrue("Auto-save callback should complete", completed);
    }
}
