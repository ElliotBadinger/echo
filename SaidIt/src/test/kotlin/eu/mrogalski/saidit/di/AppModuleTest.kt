package eu.mrogalski.saidit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.*
import org.junit.Test
import javax.inject.Singleton

class AppModuleTest {

    @Test
    fun `AppModule should be annotated with Module`() {
        val annotation = AppModule::class.java.getAnnotation(Module::class.java)
        assertNotNull("AppModule should be annotated with @Module", annotation)
    }

    @Test
    fun `AppModule should be annotated with InstallIn SingletonComponent`() {
        val annotation = AppModule::class.java.getAnnotation(InstallIn::class.java)
        assertNotNull("AppModule should be annotated with @InstallIn", annotation)
        assertEquals("AppModule should be installed in SingletonComponent", 
            SingletonComponent::class.java, annotation?.value?.get(0))
    }

    @Test
    fun `provideAudioConfig should be annotated with Provides`() {
        val method = AppModule::class.java.getDeclaredMethod("provideAudioConfig")
        val annotation = method.getAnnotation(Provides::class.java)
        assertNotNull("provideAudioConfig should be annotated with @Provides", annotation)
    }

    @Test
    fun `provideAudioConfig should be annotated with Singleton`() {
        val method = AppModule::class.java.getDeclaredMethod("provideAudioConfig")
        val annotation = method.getAnnotation(Singleton::class.java)
        assertNotNull("provideAudioConfig should be annotated with @Singleton", annotation)
    }

    @Test
    fun `provideAudioConfig should return correct AudioConfig`() {
        val audioConfig = AppModule.provideAudioConfig()
        assertEquals("Sample rate should be 48000", 48000, audioConfig.sampleRate)
        assertEquals("Channels should be 1", 1, audioConfig.channels)
    }

    @Test
    fun `AudioConfig should be a data class`() {
        val audioConfig1 = AppModule.AudioConfig(48000, 1)
        val audioConfig2 = AppModule.AudioConfig(48000, 1)
        val audioConfig3 = AppModule.AudioConfig(44100, 2)

        // Test equality (data class behavior)
        assertEquals("AudioConfig with same values should be equal", audioConfig1, audioConfig2)
        assertNotEquals("AudioConfig with different values should not be equal", audioConfig1, audioConfig3)

        // Test toString (data class behavior)
        val toString = audioConfig1.toString()
        assertTrue("AudioConfig toString should contain sampleRate", toString.contains("48000"))
        assertTrue("AudioConfig toString should contain channels", toString.contains("1"))
    }

    @Test
    fun `AudioConfig should have correct properties`() {
        val audioConfig = AppModule.AudioConfig(sampleRate = 44100, channels = 2)
        assertEquals("Sample rate should match constructor parameter", 44100, audioConfig.sampleRate)
        assertEquals("Channels should match constructor parameter", 2, audioConfig.channels)
    }

    @Test
    fun `AudioConfig should support component destructuring`() {
        val audioConfig = AppModule.AudioConfig(sampleRate = 48000, channels = 1)
        val (sampleRate, channels) = audioConfig
        assertEquals("Destructured sample rate should match", 48000, sampleRate)
        assertEquals("Destructured channels should match", 1, channels)
    }
}
