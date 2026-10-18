package com.ts.ds.views.components.shared

import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.Px
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat
import kotlin.math.roundToInt

internal class TSIconSlotDrawable(
    drawable: Drawable,
    @Px private val sizePx: Int,
) : DrawableWrapperCompat(drawable) {
    override fun getIntrinsicWidth(): Int = sizePx

    override fun getIntrinsicHeight(): Int = sizePx

    override fun onBoundsChange(bounds: Rect) {
        val inner = drawable ?: return
        val width = inner.intrinsicWidth.takeIf { it > 0 } ?: bounds.width()
        val height = inner.intrinsicHeight.takeIf { it > 0 } ?: bounds.height()
        val scale = minOf(bounds.width().toFloat() / width, bounds.height().toFloat() / height)
        val fittedWidth = (width * scale).roundToInt()
        val fittedHeight = (height * scale).roundToInt()
        val left = bounds.left + (bounds.width() - fittedWidth) / 2
        val top = bounds.top + (bounds.height() - fittedHeight) / 2
        inner.setBounds(left, top, left + fittedWidth, top + fittedHeight)
    }
}
