package eu.mrogalski.saidit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EchoAppTest {

    @Test
    fun `EchoApp should extend Application`() {
        val app = EchoApp()
        assertTrue("EchoApp should be an instance of Application", app is Application)
    }

    @Test
    fun `EchoApp should have HiltAndroidApp annotation`() {
        val annotation = EchoApp::class.java.getAnnotation(HiltAndroidApp::class.java)
        assertNotNull("EchoApp should be annotated with @HiltAndroidApp", annotation)
    }

    @Test
    fun `EchoApp should be instantiable`() {
        assertNotNull("EchoApp should be instantiable", EchoApp())
    }

    @Test
    fun `EchoApp class should be final`() {
        // In Kotlin, classes are final by default unless marked open
        val clazz = EchoApp::class.java
        assertTrue("EchoApp class should be final", 
            java.lang.reflect.Modifier.isFinal(clazz.modifiers))
    }
}
