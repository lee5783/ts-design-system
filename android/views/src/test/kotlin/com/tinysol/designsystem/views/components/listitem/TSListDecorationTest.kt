package com.tinysol.designsystem.views.components.listitem

import android.app.Activity
import android.graphics.Rect
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tinysol.designsystem.core.style.TSListGroupPosition
import com.tinysol.designsystem.views.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private sealed interface SampleRow {
    data class Header(val title: String) : SampleRow

    data class Item(
        val title: String,
        val position: TSListGroupPosition,
        val switch: Boolean = false,
        var checked: Boolean = false,
        val chevron: Boolean = false,
    ) : SampleRow
}

private class SampleAdapter(val rows: List<SampleRow>, private val onOpen: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var created = 0

    override fun getItemCount(): Int = rows.size

    override fun getItemViewType(position: Int): Int = if (rows[position] is SampleRow.Header) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        created++
        val view = if (viewType == 0) TextView(parent.context) else TSListItemView(parent.context)
        view.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is SampleRow.Header -> (holder.itemView as TextView).text = row.title
            is SampleRow.Item -> (holder.itemView as TSListItemView).apply {
                title = row.title
                trailing = if (row.switch) TSListItemTrailing.Switch else null
                isChecked = row.checked
                showsChevron = row.chevron
                setOnCheckedChangeListener { _, checked -> row.checked = checked }
                setOnClickListener(if (row.chevron) View.OnClickListener { onOpen(row.title) } else null)
            }
        }
    }
}

private fun groupedRows(sections: List<Int>, switchEvery: Int = 0): List<SampleRow> = sections.flatMapIndexed { section, count ->
    listOf<SampleRow>(SampleRow.Header("Section $section")) + (0 until count).map { index ->
        val switch = switchEvery > 0 && index % switchEvery == 0
        SampleRow.Item("Row $section.$index", TSListGroupPosition.make(index, count), switch = switch, chevron = !switch)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "w360dp-h640dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSListDecorationTest {
    private val opened = mutableListOf<String>()

    private fun host(rows: List<SampleRow>, height: Int = 600): Pair<RecyclerView, TSListDecoration> {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val context = ContextThemeWrapper(activity, R.style.Theme_TS)
        val list = RecyclerView(context)
        val adapter = SampleAdapter(rows) { opened += it }
        val decoration = TSListDecoration(context) { (rows[it] as? SampleRow.Item)?.position }
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = adapter
        list.addItemDecoration(decoration)
        activity.setContentView(FrameLayout(context).apply { addView(list, FrameLayout.LayoutParams(360, height)) })
        idle()
        return list to decoration
    }

    private fun idle() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun RecyclerView.rows(): List<Pair<Int, TSListItemView>> = (0 until childCount)
        .map(::getChildAt)
        .filterIsInstance<TSListItemView>()
        .map { getChildAdapterPosition(it) to it }

    @Test
    fun offsetsSeparateSectionsBelowTheirLastRow() {
        val (list, _) = host(groupedRows(listOf(3, 1)))
        val rows = (list.adapter as SampleAdapter).rows

        list.rows().forEach { (position, view) ->
            val offsets = Rect().also { list.getDecoratedBoundsWithMargins(view, it) }
            val expectedBottom = if ((rows[position] as SampleRow.Item).position.roundsBottom) 24 else 0
            assertEquals(expectedBottom, offsets.bottom - view.bottom)
            assertEquals(0, view.top - offsets.top)
        }
    }

    @Test
    fun rowsLearnTheirGroupPosition() {
        val (list, _) = host(groupedRows(listOf(3, 1)))
        val positions = list.rows().map { (_, view) -> view.groupPosition }

        assertEquals(
            listOf(TSListGroupPosition.First, TSListGroupPosition.Middle, TSListGroupPosition.Last, TSListGroupPosition.Single),
            positions,
        )
    }

    @Test
    fun groupRunsCoverEachSectionsRowsOnly() {
        val (list, decoration) = host(groupedRows(listOf(3, 1)))
        val rows = list.rows().map { it.second }

        val runs = decoration.groupRuns(list)

        assertEquals(2, runs.size)
        assertEquals(Rect(rows[0].left, rows[0].top, rows[2].right, rows[2].bottom), runs[0].bounds)
        assertTrue(runs[0].roundsTop && runs[0].roundsBottom)
        assertEquals(Rect(rows[3].left, rows[3].top, rows[3].right, rows[3].bottom), runs[1].bounds)
    }

    @Test
    fun dividersAreFullWidthHairlinesAboveEveryRowButTheFirst() {
        val (list, decoration) = host(groupedRows(listOf(3, 1)))
        val rows = list.rows().map { it.second }

        val dividers = decoration.dividerBounds(list)

        assertEquals(listOf(rows[1], rows[2]).map { Rect(0, it.top, 360, it.top + 1) }, dividers)
    }

    @Test
    fun runsCutOffAtTheViewportKeepTheirOpenEdgeSquare() {
        val (list, decoration) = host(groupedRows(listOf(40)), height = 300)
        list.scrollBy(0, 400)
        idle()

        val run = decoration.groupRuns(list).single()

        assertFalse(run.roundsTop)
        assertFalse(run.roundsBottom)
    }

    @Test
    fun recycledRowsRebindTheirOwnState() {
        val (list, _) = host(groupedRows(listOf(60), switchEvery = 3), height = 400)
        val adapter = list.adapter as SampleAdapter
        val first = list.rows().first { (position, _) -> (adapter.rows[position] as SampleRow.Item).switch }
        first.second.switchView.performClick()
        idle()
        assertTrue((adapter.rows[first.first] as SampleRow.Item).checked)

        list.scrollBy(0, 2000)
        idle()
        assertCellsMatchTheirRows(list, adapter)

        list.scrollToPosition(0)
        idle()
        assertCellsMatchTheirRows(list, adapter)
        assertTrue(adapter.created < adapter.rows.size)
        assertTrue(list.findViewHolderForAdapterPosition(first.first)!!.itemView.let { it as TSListItemView }.isChecked)
    }

    @Test
    fun rebindingToAPlainRowClearsPressAndClickability() {
        val (list, _) = host(groupedRows(listOf(2)))
        val view = list.rows().first().second
        view.setOnClickListener { opened += "x" }
        view.isPressed = true
        assertTrue(view.isPressed)

        view.setOnClickListener(null)

        assertFalse(view.isPressed)
        assertFalse(view.isClickable)
        assertNull(view.resolved.background)
    }

    private fun assertCellsMatchTheirRows(list: RecyclerView, adapter: SampleAdapter) {
        list.rows().forEach { (position, view) ->
            val row = adapter.rows[position] as SampleRow.Item
            assertEquals(row.title, view.title.toString())
            assertEquals(row.position, view.groupPosition)
            assertEquals(row.switch, view.trailing == TSListItemTrailing.Switch)
            assertEquals(if (row.switch) View.VISIBLE else View.GONE, view.switchView.visibility)
            assertEquals(row.checked, view.isChecked)
            assertEquals(row.chevron, view.isClickable)
            assertEquals(if (row.chevron) View.VISIBLE else View.GONE, view.chevronView.visibility)
            assertFalse(view.isPressed)
        }
    }
}
