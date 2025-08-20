package eu.mrogalski.saidit;

import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ServiceController;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class SaidItServiceTest {

    private ServiceController<SaidItService> controller;
    private SaidItService service;

    @Before
    public void setUp() {
        controller = Robolectric.buildService(SaidItService.class, new Intent(ApplicationProvider.getApplicationContext(), SaidItService.class));
        service = controller.create().get();
        service.mIsTestEnvironment = true; // Mark as test environment
    }

    @After
    public void tearDown() {
        // Properly destroy the service to ensure threads are cleaned up
        controller.destroy();
    }

    @Test
    public void testInitialState() {
        assertEquals(SaidItService.STATE_READY, service.state);
    }

    @Test
    public void testEnableListening_changesState() {
        service.state = SaidItService.STATE_READY;
        service.enableListening();
        assertEquals(SaidItService.STATE_LISTENING, service.state);
    }

    @Test
    public void testDisableListening_changesState() {
        service.state = SaidItService.STATE_LISTENING;
        service.disableListening();
        assertEquals(SaidItService.STATE_READY, service.state);
    }

    @Test
    public void testStartRecording_changesState() {
        service.state = SaidItService.STATE_LISTENING;
        service.startRecording(5.0f);
        assertEquals(SaidItService.STATE_RECORDING, service.state);
    }

    @Test
    public void testStopRecording_changesState() {
        service.state = SaidItService.STATE_RECORDING;
        service.stopRecording(null);
        assertEquals(SaidItService.STATE_LISTENING, service.state);
    }
}
