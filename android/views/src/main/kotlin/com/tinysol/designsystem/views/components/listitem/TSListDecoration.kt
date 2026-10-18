package com.tinysol.designsystem.views.components.listitem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tinysol.designsystem.core.style.TSListGroupPosition
import kotlin.math.roundToInt

public class TSListDecoration(
    context: Context,
    private val positionOf: (adapterPosition: Int) -> TSListGroupPosition?,
) : RecyclerView.ItemDecoration() {
    private val painter = TSListGroupPainter(context)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = groupPosition(parent, view)
        (view as? TSListItemView)?.groupPosition = position
        outRect.set(0, 0, 0, if (position?.roundsBottom == true) painter.sectionSpacing else 0)
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        painter.updateColors(parent.context)
        groupRuns(parent).forEach { painter.drawFill(canvas, it) }
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        painter.updateColors(parent.context)
        groupRuns(parent).forEach { painter.drawBorder(canvas, it) }
        dividerBounds(parent).forEach { painter.drawDivider(canvas, it) }
    }

    internal fun groupRuns(parent: RecyclerView): List<TSListGroupRun> {
        val runs = mutableListOf<TSListGroupRun>()
        var current: TSListGroupRun? = null
        visibleRows(parent).forEach { (child, position) ->
            val bounds = childBounds(child)
            val open = current
            current = if (open != null && !position.roundsTop && open.bounds.bottom == bounds.top) {
                open.copy(bounds = Rect(open.bounds).apply { union(bounds) }, roundsBottom = position.roundsBottom)
            } else {
                open?.let(runs::add)
                TSListGroupRun(bounds, position.roundsTop, position.roundsBottom)
            }
        }
        current?.let(runs::add)
        return runs
    }

    internal fun dividerBounds(parent: RecyclerView): List<Rect> = visibleRows(parent)
        .filter { (_, position) -> position.drawsTopDivider }
        .map { (child, _) -> childBounds(child).let { painter.dividerBounds(it.left, it.top, it.right) } }

    private fun visibleRows(parent: RecyclerView): List<Pair<View, TSListGroupPosition>> =
        (0 until parent.childCount).mapNotNull { index ->
            val child = parent.getChildAt(index)
            groupPosition(parent, child)?.let { child to it }
        }.sortedBy { (child, _) -> child.top }

    private fun groupPosition(parent: RecyclerView, child: View): TSListGroupPosition? {
        val adapterPosition = parent.getChildAdapterPosition(child)
        return if (adapterPosition == RecyclerView.NO_POSITION) null else positionOf(adapterPosition)
    }

    private fun childBounds(child: View): Rect {
        val translationX = child.translationX.roundToInt()
        val translationY = child.translationY.roundToInt()
        return Rect(child.left + translationX, child.top + translationY, child.right + translationX, child.bottom + translationY)
    }
}
