package eu.mrogalski.saidit;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import androidx.core.content.ContextCompat;
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
    private Intent serviceIntent;
    private ServiceConnection connection;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @After
    public void tearDown() {
        if (connection != null) {
            context.unbindService(connection);
            connection = null;
        }
        if (serviceIntent != null) {
            context.stopService(serviceIntent);
            serviceIntent = null;
        }
        SharedPreferences preferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    @Test
    public void testAutoSaveDoesNotCrashService() throws InterruptedException {
        // Configure preferences prior to launching the service
        SharedPreferences preferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean("auto_save_enabled", true)
                .putInt("auto_save_duration", 1)
                .putBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, false)
                .apply();

        CountDownLatch latch = new CountDownLatch(1);
        SaidItService service = bindService();
        assertNotNull("Service should bind", service);

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

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertTrue("Auto-save callback should complete", completed);
    }

    private SaidItService bindService() throws InterruptedException {
        AtomicReference<SaidItService> serviceRef = new AtomicReference<>();
        CountDownLatch connectedLatch = new CountDownLatch(1);

        serviceIntent = new Intent(context, SaidItService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                SaidItService.BackgroundRecorderBinder typedBinder = (SaidItService.BackgroundRecorderBinder) binder;
                serviceRef.set(typedBinder.getService());
                connectedLatch.countDown();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceRef.set(null);
            }
        };

        boolean bound = context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            return null;
        }

        boolean connected = connectedLatch.await(5, TimeUnit.SECONDS);
        return connected ? serviceRef.get() : null;
    }
}
