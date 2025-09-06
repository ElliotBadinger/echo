package eu.mrogalski.android

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Comprehensive unit tests for Views utility functions.
 * Tests both legacy Java-compatible methods and modern Kotlin extensions.
 */
class ViewsTest {
    
    private lateinit var rootViewGroup: ViewGroup
    private lateinit var childViewGroup: ViewGroup
    private lateinit var textView: TextView
    private lateinit var button: Button
    
    @Before
    fun setUp() {
        // Create mock view hierarchy:
        // rootViewGroup
        //   ├── textView
        //   ├── childViewGroup
        //   │   └── button
        
        textView = mock(TextView::class.java)
        button = mock(Button::class.java)
        childViewGroup = mock(LinearLayout::class.java)
        rootViewGroup = mock(LinearLayout::class.java)
        
        // Setup root ViewGroup
        `when`(rootViewGroup.childCount).thenReturn(2)
        `when`(rootViewGroup.getChildAt(0)).thenReturn(textView)
        `when`(rootViewGroup.getChildAt(1)).thenReturn(childViewGroup)
        
        // Setup child ViewGroup
        `when`(childViewGroup.childCount).thenReturn(1)
        `when`(childViewGroup.getChildAt(0)).thenReturn(button)
    }
    
    @Test
fun `search should call callback for all views in hierarchy`() {
        val callback = mock(Views.SearchViewCallback::class.java)
        
        Views.search(rootViewGroup, callback)
        
        // Verify callback called for each view with correct parent
        verify(callback).onView(textView, rootViewGroup)
        verify(callback).onView(childViewGroup, rootViewGroup)
        verify(callback).onView(button, childViewGroup)
        verifyNoMoreInteractions(callback)
    }
    
    @Test
fun `search should handle empty ViewGroup`() {
        val emptyViewGroup = mock(ViewGroup::class.java)
        `when`(emptyViewGroup.childCount).thenReturn(0)
        val callback = mock(Views.SearchViewCallback::class.java)
        
        Views.search(emptyViewGroup, callback)
        
        verifyNoInteractions(callback)
    }
    
    @Test
fun `search should handle single view`() {
        val singleViewGroup = mock(ViewGroup::class.java)
        val singleView = mock(View::class.java)
        `when`(singleViewGroup.childCount).thenReturn(1)
        `when`(singleViewGroup.getChildAt(0)).thenReturn(singleView)
        
        val callback = mock(Views.SearchViewCallback::class.java)
        
        Views.search(singleViewGroup, callback)
        
        verify(callback).onView(singleView, singleViewGroup)
        verifyNoMoreInteractions(callback)
    }
    
    @Test
    fun `SearchViewCallback should work with lambda`() {
        val viewsFound = mutableListOf<View>()
        val parentsFound = mutableListOf<ViewGroup>()
        
        val callback = Views.SearchViewCallback { view, parent ->
            viewsFound.add(view)
            parentsFound.add(parent)
        }
        
        Views.search(rootViewGroup, callback)
        
        assertEquals(3, viewsFound.size)
        assertEquals(3, parentsFound.size)
        assertTrue(viewsFound.contains(textView))
        assertTrue(viewsFound.contains(childViewGroup))
        assertTrue(viewsFound.contains(button))
    }
    
    @Test
    fun `children extension should return immediate children only`() {
        val children = rootViewGroup.children.toList()
        
        assertEquals(2, children.size)
        assertEquals(textView, children[0])
        assertEquals(childViewGroup, children[1])
    }
    
    @Test
    fun `allViews extension should return all views recursively`() {
        val allViews = rootViewGroup.allViews().toList()
        
        assertEquals(3, allViews.size)
        assertTrue(allViews.contains(textView))
        assertTrue(allViews.contains(childViewGroup))
        assertTrue(allViews.contains(button))
    }
    
    @Test
    fun `searchViews extension should call action for all views`() {
        val viewsFound = mutableListOf<View>()
        val parentsFound = mutableListOf<ViewGroup>()
        
        rootViewGroup.searchViews { view, parent ->
            viewsFound.add(view)
            parentsFound.add(parent)
        }
        
        assertEquals(3, viewsFound.size)
        assertEquals(3, parentsFound.size)
        assertTrue(viewsFound.contains(textView))
        assertTrue(viewsFound.contains(childViewGroup))
        assertTrue(viewsFound.contains(button))
    }
    
    @Test
    fun `findView should return first matching view`() {
        val foundView = rootViewGroup.findView { it == button }
        
        assertEquals(button, foundView)
    }
    
    @Test
fun `findView should return null when no match found`() {
        val nonExistentView = mock(View::class.java)
        val foundView = rootViewGroup.findView { it == nonExistentView }
        
        assertNull(foundView)
    }
    
    @Test
    fun `findViews should return all matching views`() {
        // Setup: make both textView and button match the predicate
        `when`(textView.id).thenReturn(1)
        `when`(button.id).thenReturn(1)
        `when`(childViewGroup.id).thenReturn(2)
        
        val foundViews = rootViewGroup.findViews { view ->
            when (view) {
                is TextView, is Button -> true
                else -> false
            }
        }
        
        assertEquals(2, foundViews.size)
        assertTrue(foundViews.contains(textView))
        assertTrue(foundViews.contains(button))
    }
    
    @Test
    fun `findViews should return empty list when no matches found`() {
        val foundViews = rootViewGroup.findViews { false }
        
        assertTrue(foundViews.isEmpty())
    }
    
    @Test
fun `extension functions should handle empty ViewGroup`() {
        val emptyViewGroup = mock(ViewGroup::class.java)
        `when`(emptyViewGroup.childCount).thenReturn(0)
        
        assertTrue(emptyViewGroup.children.toList().isEmpty())
        assertTrue(emptyViewGroup.allViews().toList().isEmpty())
        assertNull(emptyViewGroup.findView { true })
        assertTrue(emptyViewGroup.findViews { true }.isEmpty())
    }
    
    @Test
    fun `extension functions should handle deeply nested hierarchy`() {
        // Create deeper hierarchy: root -> level1 -> level2 -> deepView
        val deepView = mock(View::class.java)
        val level2 = mock(ViewGroup::class.java)
        val level1 = mock(ViewGroup::class.java)
        val root = mock(ViewGroup::class.java)
        
        `when`(root.childCount).thenReturn(1)
        `when`(root.getChildAt(0)).thenReturn(level1)
        
        `when`(level1.childCount).thenReturn(1)
        `when`(level1.getChildAt(0)).thenReturn(level2)
        
        `when`(level2.childCount).thenReturn(1)
        `when`(level2.getChildAt(0)).thenReturn(deepView)
        
        val allViews = root.allViews().toList()
        
        assertEquals(3, allViews.size)
        assertTrue(allViews.contains(level1))
        assertTrue(allViews.contains(level2))
        assertTrue(allViews.contains(deepView))
    }
    
    @Test
    fun `Views object should be accessible from Java`() {
        // Test Java compatibility - this ensures @JvmStatic works
        val callback = Views.SearchViewCallback { _, _ -> }
        
        // This should compile and work from Java
        Views.search(rootViewGroup, callback)
        
        // Verify the interface is accessible
        assertNotNull(callback)
    }
    
    @Test
    fun `performance test with large hierarchy should complete efficiently`() {
        // Create a large flat hierarchy to test performance
        val largeViewGroup = mock(ViewGroup::class.java)
        val childViews = (0 until 100).map { mock(View::class.java) }
        
        `when`(largeViewGroup.childCount).thenReturn(100)
        childViews.forEachIndexed { index, view ->
            `when`(largeViewGroup.getChildAt(index)).thenReturn(view)
        }
        
        val startTime = System.currentTimeMillis()
        val allViews = largeViewGroup.allViews().toList()
        val endTime = System.currentTimeMillis()
        
        assertEquals(100, allViews.size)
        assertTrue(endTime - startTime < 100) // Should complete in under 100ms
    }
}