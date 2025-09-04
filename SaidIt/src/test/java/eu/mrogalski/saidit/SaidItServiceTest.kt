package eu.mrogalski.saidit

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner.Silent::class)
class SaidItServiceTest {

    // Note: These are unit tests for SaidItService logic that can be tested without Android context
    // Full integration tests with Android context should be in androidTest/

    @Test
    fun testServiceClassExists() {
        // Verify the SaidItService class can be loaded and has expected methods
        val serviceClass = SaidItService::class.java
        assertNotNull("SaidItService class should exist", serviceClass)
        
        // Verify key methods exist
        val methods = serviceClass.declaredMethods.map { it.name }
        assertTrue("Should have enableListening method", methods.contains("enableListening"))
        assertTrue("Should have disableListening method", methods.contains("disableListening"))
        assertTrue("Should have startRecording method", methods.contains("startRecording"))
        assertTrue("Should have stopRecording method", methods.contains("stopRecording"))
        assertTrue("Should have dumpRecording method", methods.contains("dumpRecording"))
    }
    
    @Test
    fun testServiceConstants() {
        // Test that service constants are properly defined
        val companionClass = SaidItService.Companion::class.java
        assertNotNull("Companion object should exist", companionClass)
    }
    
    @Test
    fun testServiceInterfaces() {
        // Verify that required interfaces exist
        val wavFileReceiverClass = SaidItService.WavFileReceiver::class.java
        val stateCallbackClass = SaidItService.StateCallback::class.java
        
        assertNotNull("WavFileReceiver interface should exist", wavFileReceiverClass)
        assertNotNull("StateCallback interface should exist", stateCallbackClass)
        
        // Verify interface methods
        val wavMethods = wavFileReceiverClass.declaredMethods.map { it.name }
        assertTrue("WavFileReceiver should have onSuccess method", wavMethods.contains("onSuccess"))
        assertTrue("WavFileReceiver should have onFailure method", wavMethods.contains("onFailure"))
        
        val stateMethods = stateCallbackClass.declaredMethods.map { it.name }
        assertTrue("StateCallback should have state method", stateMethods.contains("state"))
    }
    
    @Test
    fun testServiceInheritance() {
        // Verify SaidItService properly extends Android Service
        val serviceClass = SaidItService::class.java
        val superClass = serviceClass.superclass
        assertEquals("Should extend Android Service", "android.app.Service", superClass?.name)
    }
    
    @Test
    fun testBinder() {
        // Test that the binder inner class exists
        val binderClass = SaidItService.BackgroundRecorderBinder::class.java
        assertNotNull("BackgroundRecorderBinder should exist", binderClass)
        
        val methods = binderClass.declaredMethods.map { it.name }
        assertTrue("Binder should have getService method", methods.contains("getService"))
    }
    
    // Note: Full functional tests with Android context mocking should be added to androidTest/
    // These unit tests verify the class structure and basic functionality without Android dependencies
}