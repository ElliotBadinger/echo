package eu.mrogalski.saidit.di

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dagger.Module
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration tests for AppModule.
 *
 * Note: AudioConfig was removed from AppModule because audio configuration
 * is user-configurable via SettingsActivity and stored in SharedPreferences.
 * See docs/architecture/audio-config-decision.md for details.
 */
@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class AppModuleTest {

    @dagger.hilt.EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppEntryPoint {
        fun application(): Application
        @ApplicationContext fun appContext(): Context
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `AppModule should be annotated and installed correctly`() {
        val moduleAnnotation = AppModule::class.java.getAnnotation(Module::class.java)
        val installInAnnotation = AppModule::class.java.getAnnotation(InstallIn::class.java)
        assertNotNull("AppModule should be annotated with @Module", moduleAnnotation)
        assertNotNull("AppModule should be annotated with @InstallIn", installInAnnotation)
        assertEquals(
            "AppModule should be installed in SingletonComponent",
            SingletonComponent::class.java, installInAnnotation?.value?.get(0)
        )
    }

    @Test
    fun `Hilt graph provides Application and @ApplicationContext`() {
        // Real DI verification via EntryPoint
        val application = ApplicationProvider.getApplicationContext<Application>()
        val entry = EntryPoints.get(application, AppEntryPoint::class.java)
        assertSame("Application entry should be the test application", application, entry.application())
        assertSame("@ApplicationContext entry should equal provider context", application, entry.appContext())
    }

    @Test
    fun `AppModule should not provide AudioConfig because it uses SharedPreferences`() {
        val methods = AppModule::class.java.declaredMethods
        val audioConfigMethods = methods.filter { it.name.contains("AudioConfig") }
        assertTrue(
            "AppModule should not have AudioConfig provider methods (uses SharedPreferences instead)",
            audioConfigMethods.isEmpty()
        )
    }

    @Test
    fun `AppModule should be an object (singleton)`() {
        val clazz = AppModule::class.java
        assertTrue(
            "AppModule should be final (Kotlin object)",
            java.lang.reflect.Modifier.isFinal(clazz.modifiers)
        )
        val instanceField = clazz.declaredFields.find { it.name == "INSTANCE" }
        assertNotNull("AppModule should have INSTANCE field (Kotlin object)", instanceField)
    }
}
