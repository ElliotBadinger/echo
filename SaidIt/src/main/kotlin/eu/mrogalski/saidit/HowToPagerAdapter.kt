package eu.mrogalski.saidit

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class HowToPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = HowToStep.entries.size

    override fun createFragment(position: Int): Fragment = HowToPageFragment.newInstance(position)
}
