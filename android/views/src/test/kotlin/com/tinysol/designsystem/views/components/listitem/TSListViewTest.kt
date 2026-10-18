package com.tinysol.designsystem.views.components.listitem

import android.graphics.Rect
import android.view.View
import android.widget.LinearLayout
import com.tinysol.designsystem.core.style.TSListGroupPosition
import com.tinysol.designsystem.views.components.hostInActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "w360dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSListViewTest {
    private fun list(count: Int): TSListView = hostInActivity { context ->
        TSListView(context).apply {
            repeat(count) { index ->
                addView(TSListItemView(context).apply { title = "Row $index" }, LinearLayout.LayoutParams(360, LinearLayout.LayoutParams.WRAP_CONTENT))
            }
        }
    }

    private fun TSListView.positions(): List<TSListGroupPosition?> = (0 until childCount).map { (getChildAt(it) as TSListItemView).groupPosition }

    @Test
    fun rowsLearnTheirPositionAndDividersSitAboveAllButTheFirst() {
        val view = list(3)

        assertEquals(listOf(TSListGroupPosition.First, TSListGroupPosition.Middle, TSListGroupPosition.Last), view.positions())
        assertEquals(listOf(Rect(0, 48, 360, 49), Rect(0, 96, 360, 97)), view.dividerBounds())
        assertEquals(144, view.height)
    }

    @Test
    fun singleRowHasNoDivider() {
        val view = list(1)

        assertEquals(listOf(TSListGroupPosition.Single), view.positions())
        assertEquals(emptyList<Rect>(), view.dividerBounds())
    }

    @Test
    fun hiddenRowsAreSkipped() {
        val view = list(3)

        view.getChildAt(2).visibility = View.GONE
        view.requestLayout()
        view.measure(View.MeasureSpec.makeMeasureSpec(360, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, 360, view.measuredHeight)

        assertEquals(listOf(TSListGroupPosition.First, TSListGroupPosition.Last, TSListGroupPosition.Last), view.positions())
        assertEquals(1, view.dividerBounds().size)
    }
}
