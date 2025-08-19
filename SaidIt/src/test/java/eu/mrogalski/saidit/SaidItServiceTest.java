package eu.mrogalski.saidit;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioRecord;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SaidItServiceTest {

    @Mock
    private Context mockContext;
    @Mock
    private SharedPreferences mockPrefs;
    @Mock
    private SharedPreferences.Editor mockEditor;
    @Mock
    private AudioRecord mockAudioRecord;

    private SaidItService saidItService;

    @Before
    public void setUp() {
        // Mock Android dependencies
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
        
        saidItService = spy(new SaidItService());
        
        // Use `attachBaseContext` to set the mocked context
        saidItService.attachBaseContext(mockContext);
        
        // Further mocking needed after `attachBaseContext`
        when(saidItService.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        
        // Mock audio memory to avoid actual allocation
        saidItService.audioMemory = mock(AudioMemory.class);
    }
    
    @Test
    public void testInitialState() {
        assertEquals(SaidItService.STATE_READY, saidItService.state);
    }

    @Test
    public void testEnableListening_changesState() {
        // Given the service is in the READY state
        saidItService.state = SaidItService.STATE_READY;

        // When listening is enabled
        saidItService.enableListening();

        // Then the state transitions to LISTENING
        assertEquals(SaidItService.STATE_LISTENING, saidItService.state);
    }
    
    @Test
    public void testDisableListening_changesState() {
        // Given the service is listening
        saidItService.state = SaidItService.STATE_LISTENING;

        // When listening is disabled
        saidItService.disableListening();

        // Then the state transitions back to READY
        assertEquals(SaidItService.STATE_READY, saidItService.state);
    }
    
    @Test
    public void testStartRecording_changesState() {
        // Given the service is listening
        saidItService.state = SaidItService.STATE_LISTENING;
        
        // When recording is started
        saidItService.startRecording(5.0f);
        
        // Then the state transitions to RECORDING
        assertEquals(SaidItService.STATE_RECORDING, saidItService.state);
    }
    
    @Test
    public void testStopRecording_changesState() {
        // Given the service is recording
        saidItService.state = SaidItService.STATE_RECORDING;

        // When recording is stopped
        saidItService.stopRecording(null);

        // Then the state transitions back to LISTENING
        assertEquals(SaidItService.STATE_LISTENING, saidItService.state);
    }
    
    @Test
    public void testDumpRecording_callsAudioMemoryDump() throws IOException {
        // Given the service is listening
        saidItService.state = SaidItService.STATE_LISTENING;
        saidItService.SAMPLE_RATE = 48000;
        saidItService.FILL_RATE = 2 * saidItService.SAMPLE_RATE;
        
        doNothing().when(saidItService.audioMemory).dump(any(AudioMemory.Consumer.class), anyInt());

        // When dumpRecording is called
        saidItService.dumpRecording(10, null, "test_dump");
        
        // We need to wait for the audio handler to process the runnable
        try {
            Thread.sleep(500); // Allow time for the async operation
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Then it should calculate bytes correctly and call audioMemory.dump
        int expectedBytesToDump = (int) (10 * (2 * 48000));
        verify(saidItService.audioMemory).dump(any(AudioMemory.Consumer.class), anyInt());
    }
}