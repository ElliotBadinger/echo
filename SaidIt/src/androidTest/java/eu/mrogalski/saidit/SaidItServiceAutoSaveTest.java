package eu.mrogalski.saidit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.rule.ServiceTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SaidItServiceAutoSaveTest {

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
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    @Test
    public void testAutoSave_createsAudioFile() throws Exception {
        // 1. Configure auto-save to be enabled with a 2-second interval.
        sharedPreferences.edit()
                .putBoolean("auto_save_enabled", true)
                .putInt("auto_save_duration", 1)
                .apply();

        // 2. Start the service.
        Intent serviceIntent = new Intent(context, SaidItService.class);
        serviceRule.startService(serviceIntent);

        SaidItService.BackgroundRecorderBinder binder = (SaidItService.BackgroundRecorderBinder) serviceRule.bindService(serviceIntent);
        SaidItService service = binder.getService();
        service.setTestEnvironment(true);

        CountDownLatch latch = new CountDownLatch(1);
        final Uri[] resultUri = new Uri[1];

        SaidItService.WavFileReceiver receiver = new SaidItService.WavFileReceiver() {
            @Override
            public void onSuccess(Uri fileUri) {
                resultUri[0] = fileUri;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                fail("Auto-save failed: " + e.getMessage());
            }
        };

        service.triggerAutoSaveForTest(1, receiver);

        boolean completed = false;
        try {
            completed = latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        assertTrue("Auto-save should signal completion", completed);
        assertNotNull("A URI should be returned for the auto-saved file", resultUri[0]);
    }
}
