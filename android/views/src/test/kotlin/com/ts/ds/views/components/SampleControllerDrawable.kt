package com.ts.ds.views.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

internal class SampleControllerDrawable(private val sizePx: Int) : Drawable() {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeCap = Paint.Cap.ROUND
    }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun getIntrinsicWidth(): Int = sizePx

    override fun getIntrinsicHeight(): Int = sizePx

    override fun draw(canvas: Canvas) {
        val unit = bounds.width() / 24f
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        stroke.strokeWidth = 1.75f * unit
        canvas.drawRoundRect(RectF(2 * unit, 7 * unit, 22 * unit, 17 * unit), 5 * unit, 5 * unit, stroke)
        canvas.drawLine(7 * unit, 10 * unit, 7 * unit, 14 * unit, stroke)
        canvas.drawLine(5 * unit, 12 * unit, 9 * unit, 12 * unit, stroke)
        dot.color = Color.RED
        canvas.drawCircle(16 * unit, 11 * unit, unit, dot)
        dot.color = Color.BLUE
        canvas.drawCircle(18 * unit, 13 * unit, unit, dot)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        stroke.alpha = alpha
        dot.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        stroke.colorFilter = colorFilter
        dot.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

internal fun Context.sampleController(sizeDp: Float = 24f): Drawable = SampleControllerDrawable(dp(sizeDp))

private fun Context.dp(value: Float): Int = (value * resources.displayMetrics.density).toInt()
