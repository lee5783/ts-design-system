package com.tinysol.designsystem.views.components.listitem

import android.content.pm.ApplicationInfo
import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.views.components.hostInActivity
import com.tinysol.designsystem.views.components.tapCenter
import com.tinysol.designsystem.views.theme.tsColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "w360dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSDisclosureListItemViewTest {
    private val changes = mutableListOf<Boolean>()
    private lateinit var body: MaterialTextView

    private fun disclosure(configure: TSDisclosureListItemView.() -> Unit = {}): TSDisclosureListItemView = hostInActivity { context ->
        LinearLayout(context).apply {
            body = MaterialTextView(context).apply { text = "Licence text" }
            addView(
                TSDisclosureListItemView(context).apply {
                    title = "Kotlin"
                    subtitle = "Apache License 2.0"
                    reduceMotion = { true }
                    addView(body)
                    setOnExpandedChangeListener { _, expanded -> changes += expanded }
                    configure()
                },
                LinearLayout.LayoutParams(360, LinearLayout.LayoutParams.WRAP_CONTENT),
            )
        }
    }.getChildAt(0) as TSDisclosureListItemView

    @Test
    fun addedChildrenBecomeTheBody() {
        val view = disclosure()
        assertEquals(view.bodyContainer, body.parent)
        assertEquals(2, view.childCount)
        assertEquals(View.GONE, view.bodyContainer.visibility)
    }

    @Test
    fun tappingTheHeaderToggles() {
        val view = disclosure()
        view.headerView.tapCenter()
        assertTrue(view.isExpanded)
        assertEquals(View.VISIBLE, view.bodyContainer.visibility)
        view.headerView.tapCenter()
        assertFalse(view.isExpanded)
        assertEquals(listOf(true, false), changes)
    }

    @Test
    fun settingIsExpandedInCodeDoesNotNotify() {
        val view = disclosure()
        view.isExpanded = true
        assertEquals(View.VISIBLE, view.bodyContainer.visibility)
        assertTrue(changes.isEmpty())
    }

    @Test
    fun disabledRowDoesNotToggle() {
        val view = disclosure { isEnabled = false }
        view.headerView.tapCenter()
        assertFalse(view.isExpanded)
        assertEquals(0.45f, view.bodyContainer.alpha, 0.001f)
    }

    @Test
    fun chevronTurnsDownMirroredInRtl() {
        val application = RuntimeEnvironment.getApplication()
        application.applicationInfo.flags = application.applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
        val view = disclosure()
        assertEquals(0f, view.headerView.chevronView.rotation, 0f)
        view.isExpanded = true
        assertEquals(90f, view.headerView.chevronView.rotation, 0f)
        view.layoutDirection = View.LAYOUT_DIRECTION_RTL
        assertEquals(-90f, view.headerView.chevronView.rotation, 0f)
    }

    @Test
    fun removeAllViewsClearsOnlyTheBody() {
        val view = disclosure { isExpanded = true }
        view.removeAllViews()
        assertEquals(2, view.childCount)
        assertEquals(view.headerView, view.getChildAt(0))
        assertEquals(0, view.bodyContainer.childCount)
        view.addView(MaterialTextView(view.context).apply { text = "Replacement" })
        assertEquals(1, view.bodyContainer.childCount)
        view.removeView(view.bodyContainer.getChildAt(0))
        assertEquals(0, view.bodyContainer.childCount)
        view.removeView(view.headerView)
        assertEquals(view.headerView, view.getChildAt(0))
    }

    @Test
    fun restoringStateSetsTheChevronWithoutAnimating() {
        val view = disclosure { reduceMotion = { false } }
        val saved = SparseArray<Parcelable>()
        view.id = View.generateViewId()
        view.isExpanded = true
        view.saveHierarchyState(saved)
        view.isExpanded = false
        view.headerView.chevronView.animate().cancel()
        view.restoreHierarchyState(saved)
        assertTrue(view.isExpanded)
        assertEquals(90f, view.headerView.chevronView.rotation, 0f)
        assertEquals(View.VISIBLE, view.bodyContainer.visibility)
        assertTrue(changes.isEmpty())
    }

    @Test
    fun headerHeightAndBodyPadding() {
        val view = disclosure { isExpanded = true }
        assertEquals(68, view.headerView.height)
        assertEquals(16, view.bodyContainer.paddingStart)
        assertEquals(16, view.bodyContainer.paddingEnd)
        assertEquals(12, view.bodyContainer.paddingBottom)
        assertEquals(view.headerView.bottom, view.bodyContainer.top)
    }

    @Test
    fun dividerViewMatchesTheListDivider() {
        val divider = hostInActivity { context ->
            LinearLayout(context).apply {
                addView(TSDividerView(context), LinearLayout.LayoutParams(360, LinearLayout.LayoutParams.WRAP_CONTENT))
            }
        }.getChildAt(0)
        assertEquals(1, divider.height)
        assertEquals(360, divider.width)
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, divider.importantForAccessibility)
        val bitmap = android.graphics.Bitmap.createBitmap(divider.width, divider.height, android.graphics.Bitmap.Config.ARGB_8888)
        divider.draw(android.graphics.Canvas(bitmap))
        val expected = divider.tsColor(TSColorRole.BorderSubtle)
        assertEquals(expected, bitmap.getPixel(0, 0))
        assertEquals(expected, bitmap.getPixel(359, 0))
    }
}
