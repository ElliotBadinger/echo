package eu.mrogalski.saidit;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class SaidItServiceTest {

    private ServiceController<SaidItService> controller;
    private SaidItService saidItService;

    @Before
    public void setUp() {
        // Use Robolectric to create and manage the service lifecycle
        controller = Robolectric.buildService(SaidItService.class);
        saidItService = controller.create().get();
        saidItService.mIsTestEnvironment = true;
    }

    @After
    public void tearDown() {
        controller.destroy(); // Calls onDestroy and queues up thread termination
        if (saidItService.audioHandler != null && saidItService.audioHandler.getLooper() != null) {
            Shadows.shadowOf(saidItService.audioHandler.getLooper()).runToEndOfTasks(); // Process termination message
        }
        if (saidItService.audioThread != null) {
            try {
                // Wait for the thread to fully terminate
                saidItService.audioThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void runAllLoopers() {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        if (saidItService.audioHandler != null && saidItService.audioHandler.getLooper() != null) {
            Shadows.shadowOf(saidItService.audioHandler.getLooper()).runToEndOfTasks();
        }
    }

    @Test
    public void testInitialState_startsListeningByDefault() {
        // By default, audio memory is enabled, so the service should start listening
        runAllLoopers();
        assertEquals(SaidItService.ServiceState.LISTENING, saidItService.state);
    }

    @Test
    public void testInitialState_isReadyWhenDisabled() {
        // Disable audio memory before the service is created
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SaidIt.AUDIO_MEMORY_ENABLED_KEY, false).apply();

        // Re-create service to apply the preference
        controller.destroy(); // Destroy the old service instance
        controller = Robolectric.buildService(SaidItService.class);
        saidItService = controller.create().get();

        runAllLoopers();
        assertEquals(SaidItService.ServiceState.READY, saidItService.state);
    }

    @Test
    public void testEnableListening_changesState() {
        // Given the service is in the READY state
        saidItService.state = SaidItService.ServiceState.READY;

        // When listening is enabled
        saidItService.enableListening();
        runAllLoopers();

        // Then the state transitions to LISTENING
        assertEquals(SaidItService.ServiceState.LISTENING, saidItService.state);
    }

    @Test
    public void testDisableListening_changesState() {
        // Given the service is listening
        saidItService.enableListening();
        runAllLoopers();
        assertEquals(SaidItService.ServiceState.LISTENING, saidItService.state);

        // When listening is disabled
        saidItService.disableListening();
        runAllLoopers();

        // Then the state transitions back to READY
        assertEquals(SaidItService.ServiceState.READY, saidItService.state);
    }

    @Test
    public void testStartRecording_changesState() {
        // Given the service is listening
        saidItService.enableListening();
        runAllLoopers();
        assertEquals(SaidItService.ServiceState.LISTENING, saidItService.state);

        // When recording is started
        saidItService.startRecording(5.0f);
        runAllLoopers();

        // Then the state transitions to RECORDING
        assertEquals(SaidItService.ServiceState.RECORDING, saidItService.state);
    }

    @Test
    public void testStopRecording_changesState() {
        // Given the service is recording
        saidItService.enableListening();
        runAllLoopers();
        saidItService.startRecording(5.0f);
        runAllLoopers();
        assertEquals(SaidItService.ServiceState.RECORDING, saidItService.state);

        // When recording is stopped
        saidItService.stopRecording(null);
        runAllLoopers();

        // Then the state transitions back to LISTENING
        assertEquals(SaidItService.ServiceState.LISTENING, saidItService.state);
    }
}
