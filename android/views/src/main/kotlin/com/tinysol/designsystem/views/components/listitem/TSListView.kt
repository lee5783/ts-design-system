package com.tinysol.designsystem.views.components.listitem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.tinysol.designsystem.core.style.TSListGroupPosition

public class TSListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private val painter = TSListGroupPainter(context)

    init {
        orientation = VERTICAL
        setWillNotDraw(false)
    }

    override fun setOrientation(orientation: Int) {
        super.setOrientation(VERTICAL)
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        updateGroupPositions()
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        updateGroupPositions()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        updateGroupPositions()
        super.onLayout(changed, l, t, r, b)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        painter.updateColors(context)
        painter.drawFill(canvas, run)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        painter.drawBorder(canvas, run)
        dividerBounds().forEach { painter.drawDivider(canvas, it) }
    }

    internal fun dividerBounds(): List<Rect> = visibleChildren()
        .drop(1)
        .map { painter.dividerBounds(0, it.top, width) }

    internal fun groupPositions(): List<TSListGroupPosition> {
        val children = visibleChildren()
        return children.indices.map { TSListGroupPosition.make(it, children.size) }
    }

    private val run: TSListGroupRun
        get() = TSListGroupRun(Rect(0, 0, width, height), roundsTop = true, roundsBottom = true)

    private fun visibleChildren(): List<View> = (0 until childCount).map(::getChildAt).filter { it.visibility != GONE }

    private fun updateGroupPositions() {
        val positions = groupPositions()
        visibleChildren().forEachIndexed { index, child ->
            when (child) {
                is TSListItemView -> child.groupPosition = positions[index]
                is TSDisclosureListItemView -> child.groupPosition = positions[index]
            }
        }
    }
}
