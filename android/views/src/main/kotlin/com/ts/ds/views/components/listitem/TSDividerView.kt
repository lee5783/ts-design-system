package com.ts.ds.views.components.listitem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ts.ds.core.style.TSListItemStyleSpec
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.theme.tsColor

public class TSDividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint()

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        paint.color = tsColor(TSListItemStyleSpec.group.divider)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val thickness = dpToPx(TSListItemStyleSpec.group.dividerThicknessDp)
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), resolveSize(thickness, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val inset = dpToPx(TSListItemStyleSpec.group.dividerInsetDp).toFloat()
        val rtl = layoutDirection == LAYOUT_DIRECTION_RTL
        val left = if (rtl) 0f else inset
        val right = if (rtl) width - inset else width.toFloat()
        canvas.drawRect(left, 0f, right, height.toFloat(), paint)
    }
}
