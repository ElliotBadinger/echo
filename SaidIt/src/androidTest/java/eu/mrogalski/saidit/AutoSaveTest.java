package eu.mrogalski.saidit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ServiceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AutoSaveTest {

    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();

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
                .putInt("auto_save_duration", 5) // 5 seconds
                .apply();

        // 2. Start the service
        Intent intent = new Intent(context, SaidItService.class);
        serviceRule.startService(intent);

        // 3. Bind to the service to ensure it's running
        try {
            serviceRule.bindService(intent);
        } catch (TimeoutException e) {
            // This is expected if the service is running
        }


        // 4. Wait for auto-save to trigger
        try {
            Thread.sleep(10000); // Wait for 10 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 5. Check if the service is still running
        // A simple way to do this is to try to bind again. If it succeeds, the service is running.
        boolean isRunning = false;
        try {
            serviceRule.bindService(intent);
            isRunning = true;
        } catch (TimeoutException e) {
            // Service crashed
        }
        assertTrue("Service should still be running after auto-save", isRunning);
    }
}
