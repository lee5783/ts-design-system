package com.ts.ds.views.components.listitem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.withClip
import com.ts.ds.core.style.TSListItemStyleSpec
import com.ts.ds.views.theme.tsColor

internal data class TSListGroupRun(
    val bounds: Rect,
    val roundsTop: Boolean,
    val roundsBottom: Boolean,
)

internal class TSListGroupPainter(context: Context) {
    private val group = TSListItemStyleSpec.group
    private val density = context.resources.displayMetrics.density
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dividerPaint = Paint().apply { style = Paint.Style.FILL }
    private val shape = RectF()

    val cornerRadius: Float = group.cornerRadiusDp * density
    val borderWidth: Float = group.borderWidthDp * density
    val dividerThickness: Int = maxOf(1, (group.dividerThicknessDp * density).toInt())
    val sectionSpacing: Int = (group.sectionSpacingDp * density).toInt()

    fun updateColors(context: Context) {
        fillPaint.color = context.tsColor(group.fill)
        borderPaint.color = context.tsColor(group.border)
        borderPaint.strokeWidth = borderWidth
        dividerPaint.color = context.tsColor(group.divider)
    }

    fun drawFill(canvas: Canvas, run: TSListGroupRun) {
        canvas.withClip(run.bounds) {
            outline(run, inset = 0f)
            drawRoundRect(shape, cornerRadius, cornerRadius, fillPaint)
        }
    }

    fun drawBorder(canvas: Canvas, run: TSListGroupRun) {
        canvas.withClip(run.bounds) {
            outline(run, inset = borderWidth / 2f)
            val radius = (cornerRadius - borderWidth / 2f).coerceAtLeast(0f)
            drawRoundRect(shape, radius, radius, borderPaint)
        }
    }

    fun drawDivider(canvas: Canvas, bounds: Rect) {
        canvas.drawRect(bounds, dividerPaint)
    }

    fun dividerBounds(left: Int, top: Int, right: Int): Rect =
        Rect(left, top, right, top + dividerThickness)

    private fun outline(run: TSListGroupRun, inset: Float) {
        val overflow = cornerRadius * 2f + borderWidth
        shape.set(
            run.bounds.left + inset,
            if (run.roundsTop) run.bounds.top + inset else run.bounds.top - overflow,
            run.bounds.right - inset,
            if (run.roundsBottom) run.bounds.bottom - inset else run.bounds.bottom + overflow,
        )
    }
}
