package com.tinysol.designsystem.views.sample.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.tinysol.designsystem.tokens.R as TokensR

class DemoFlowRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {
    private val spacing = resources.getDimensionPixelSize(TokensR.dimen.ts_space_2)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var lineWidth = 0
        var lineHeight = 0
        var height = 0
        var width = 0
        visibleChildren().forEach { child ->
            measureChild(child, widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            if (lineWidth > 0 && lineWidth + spacing + child.measuredWidth > maxWidth) {
                height += lineHeight + spacing
                lineWidth = 0
                lineHeight = 0
            }
            lineWidth += (if (lineWidth > 0) spacing else 0) + child.measuredWidth
            lineHeight = maxOf(lineHeight, child.measuredHeight)
            width = maxOf(width, lineWidth)
        }
        height += lineHeight
        setMeasuredDimension(
            resolveSize(width + paddingLeft + paddingRight, widthMeasureSpec),
            resolveSize(height + paddingTop + paddingBottom, heightMeasureSpec),
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val maxWidth = r - l - paddingLeft - paddingRight
        val rtl = layoutDirection == LAYOUT_DIRECTION_RTL
        val lines = mutableListOf(mutableListOf<View>())
        var lineWidth = 0
        visibleChildren().forEach { child ->
            if (lineWidth > 0 && lineWidth + child.measuredWidth > maxWidth) {
                lines += mutableListOf<View>()
                lineWidth = 0
            }
            lines.last() += child
            lineWidth += child.measuredWidth + spacing
        }
        var y = paddingTop
        lines.forEach { line ->
            val lineHeight = line.maxOfOrNull { it.measuredHeight } ?: 0
            var x = 0
            line.forEach { child ->
                val left = if (rtl) r - l - paddingRight - x - child.measuredWidth else paddingLeft + x
                val top = y + (lineHeight - child.measuredHeight) / 2
                child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
                x += child.measuredWidth + spacing
            }
            y += lineHeight + spacing
        }
    }

    private fun visibleChildren(): List<View> = (0 until childCount).map(::getChildAt).filter { it.visibility != GONE }
}
