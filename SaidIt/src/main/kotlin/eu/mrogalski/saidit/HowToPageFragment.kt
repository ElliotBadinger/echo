package eu.mrogalski.saidit

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.siya.epistemophile.R

class HowToPageFragment : Fragment(R.layout.fragment_how_to_page) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val stepIndex = requireArguments().getInt(ARG_STEP_INDEX)
        val step = HowToStep.fromIndex(stepIndex)

        view.findViewById<TextView>(R.id.how_to_title_text).setText(step.titleRes)
        view.findViewById<TextView>(R.id.how_to_description_text).setText(step.descriptionRes)
    }

    companion object {
        private const val ARG_STEP_INDEX = "step_index"

        fun newInstance(index: Int): HowToPageFragment = HowToPageFragment().apply {
            arguments = bundleOf(ARG_STEP_INDEX to index)
        }
    }
}
