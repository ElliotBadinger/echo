package eu.mrogalski.saidit

import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.siya.epistemophile.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HowToActivityTest {

    @Test
    fun `tab layout shows sequential steps`() {
        val controller = Robolectric.buildActivity(HowToActivity::class.java).setup()
        val activity = controller.get()

        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        assertNotNull(tabLayout)

        HowToStep.entries.forEachIndexed { index, _ ->
            val expectedTitle = activity.getString(R.string.how_to_step_tab_title, index + 1)
            assertEquals(expectedTitle, tabLayout.getTabAt(index)?.text)
        }
    }

    @Test
    fun `fragments bind title and description`() {
        val controller = Robolectric.buildActivity(HowToActivity::class.java).setup()
        val activity = controller.get()

        HowToStep.entries.forEachIndexed { index, step ->
            val fragment = HowToPageFragment.newInstance(index)
            fragment.onCreate(null)
            val parent = FrameLayout(activity)
            val view = fragment.onCreateView(activity.layoutInflater, parent, null)!!
            fragment.onViewCreated(view, null)

            val titleView = view.findViewById<TextView>(R.id.how_to_title_text)
            val descriptionView = view.findViewById<TextView>(R.id.how_to_description_text)

            assertEquals(activity.getString(step.titleRes), titleView.text.toString())
            assertEquals(activity.getString(step.descriptionRes), descriptionView.text.toString())
        }
    }
}
