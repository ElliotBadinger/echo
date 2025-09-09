package eu.mrogalski.saidit

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class EchoAppTest {

    @dagger.hilt.EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppEntryPoint {
        fun application(): Application
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject lateinit var injectedApp: Application
    @Inject @ApplicationContext lateinit var injectedContext: Context

    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

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
    fun `Hilt should inject Application and Context`() {
        // Real DI verification: Application and @ApplicationContext should be injectable
        assertNotNull("Injected Application should not be null", injectedApp)
        assertNotNull("Injected @ApplicationContext should not be null", injectedContext)
        val providerAppContext = ApplicationProvider.getApplicationContext<Context>()
        assertSame("Injected @ApplicationContext should equal provider context", providerAppContext, injectedContext)
        assertTrue(
            "Injected Application should be HiltTestApplication under Robolectric",
            injectedApp is HiltTestApplication
        )
    }

    @Test
    fun `EchoApp should handle Application lifecycle correctly`() {
        val app = EchoApp()
        try {
            app.onCreate()
        } catch (e: Exception) {
            fail("EchoApp.onCreate() should not throw exceptions: ${e.message}")
        }
    }

    @Test
    fun `EntryPoint can retrieve Application from Hilt graph`() {
        val application = RuntimeEnvironment.getApplication() as Application
        val entry = EntryPoints.get(application, AppEntryPoint::class.java)
        assertSame("EntryPoint application should be the test application", application, entry.application())
    }
}
