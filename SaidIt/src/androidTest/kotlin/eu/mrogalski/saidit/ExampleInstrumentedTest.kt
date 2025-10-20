package eu.mrogalski.saidit

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.siya.epistemophile.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        // Assert against BuildConfig.APPLICATION_ID so this test survives the future appId flip.
        assertEquals(BuildConfig.APPLICATION_ID, appContext.packageName)
    }
}

