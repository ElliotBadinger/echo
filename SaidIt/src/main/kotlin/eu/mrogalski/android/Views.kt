package eu.mrogalski.android

import android.view.View
import android.view.ViewGroup

/**
 * Modern Kotlin utility functions for Android View operations.
 * Provides functional programming approach to ViewGroup traversal and manipulation.
 */
object Views {
    
    /**
     * Recursively searches through ViewGroup hierarchy and calls callback for each view.
     * 
     * @param viewGroup The root ViewGroup to search
     * @param callback Function called for each view found (view, parent)
     */
    @JvmStatic
    fun search(viewGroup: ViewGroup, callback: SearchViewCallback) {
        val childCount = viewGroup.childCount
        for (i in 0 until childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                search(child, callback)
            }
            callback.onView(child, viewGroup)
        }
    }
    
    /**
     * Kotlin functional interface for view search callbacks.
     * Compatible with Java SAM (Single Abstract Method) conversion.
     */
    fun interface SearchViewCallback {
        fun onView(view: View, parent: ViewGroup)
    }
}

/**
 * Extension functions for ViewGroup - Modern Kotlin approach
 */

/**
 * Creates a sequence of all child views in this ViewGroup (non-recursive).
 */
val ViewGroup.children: Sequence<View>
    get() = (0 until childCount).asSequence().map { getChildAt(it) }

/**
 * Recursively traverses all views in the ViewGroup hierarchy.
 * Returns a sequence of all views including nested ViewGroups.
 */
fun ViewGroup.allViews(): Sequence<View> = sequence {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        yield(child)
        if (child is ViewGroup) {
            yieldAll(child.allViews())
        }
    }
}

/**
 * Recursively searches through ViewGroup hierarchy using functional approach.
 * 
 * @param action Function to execute for each view (view, parent)
 */
fun ViewGroup.searchViews(action: (view: View, parent: ViewGroup) -> Unit) {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child is ViewGroup) {
            child.searchViews(action)
        }
        action(child, this)
    }
}

/**
 * Finds the first view matching the given predicate.
 * 
 * @param predicate Function to test each view
 * @return First matching view or null if none found
 */
fun ViewGroup.findView(predicate: (View) -> Boolean): View? {
    return allViews().firstOrNull(predicate)
}

/**
 * Finds all views matching the given predicate.
 * 
 * @param predicate Function to test each view
 * @return List of all matching views
 */
fun ViewGroup.findViews(predicate: (View) -> Boolean): List<View> {
    return allViews().filter(predicate).toList()
}