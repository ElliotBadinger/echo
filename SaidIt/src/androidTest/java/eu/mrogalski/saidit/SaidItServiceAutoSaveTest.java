package eu.mrogalski.saidit;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.rule.ServiceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    private List<Uri> createdUris = new ArrayList<>();

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    @After
    public void tearDown() {
        // Clean up preferences and any created files after each test
        sharedPreferences.edit().clear().apply();
        ContentResolver contentResolver = context.getContentResolver();
        for (Uri uri : createdUris) {
            try {
                contentResolver.delete(uri, null, null);
            } catch (Exception e) {
                // Log or handle error if cleanup fails
            }
        }
        createdUris.clear();
    }

    @Test
    public void testAutoSave_createsAudioFile() throws Exception {
        // 1. Configure auto-save to be enabled with a 2-second interval.
        sharedPreferences.edit()
                .putBoolean("auto_save_enabled", true)
                .putInt("auto_save_duration", 2) // 2 seconds
                .apply();

        // 2. Start the service.
        Intent serviceIntent = new Intent(context, SaidItService.class);
        serviceRule.startService(serviceIntent);

        // 3. Record the current time to query for files created after this point.
        long startTimeMillis = System.currentTimeMillis();

        // 4. Wait for a period longer than the auto-save interval to ensure it triggers.
        Thread.sleep(3000); // Wait 3 seconds

        // 5. Query MediaStore for the new file.
        ContentResolver contentResolver = context.getContentResolver();
        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATE_ADDED};
        String selection = MediaStore.Audio.Media.DISPLAY_NAME + " LIKE ? AND " + MediaStore.Audio.Media.DATE_ADDED + " >= ?";
        String[] selectionArgs = new String[]{"Auto-save_%", String.valueOf(startTimeMillis / 1000)};
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        Cursor cursor = contentResolver.query(collection, projection, selection, selectionArgs, sortOrder);

        assertNotNull("Cursor should not be null", cursor);
        assertTrue("A new auto-saved file should be found in MediaStore.", cursor.moveToFirst());

        // 6. Get the URI and add it to the list for cleanup.
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
        long id = cursor.getLong(idColumn);
        Uri contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
        createdUris.add(contentUri);

        cursor.close();
    }
}
