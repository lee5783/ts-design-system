package com.ts.ds.views.components.shared

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.withRotation

internal class TSSpinnerDrawable(
    @param:Px private val size: Int,
    @param:Px private val slotSize: Int,
    @Px strokeWidth: Float,
    rotationMillis: Long,
) : Drawable(), Animatable {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        this.strokeWidth = strokeWidth
    }
    private val arc = RectF()
    private var rotation = 0f
    private val animator = ValueAnimator.ofFloat(0f, FULL_TURN).apply {
        duration = rotationMillis
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            rotation = it.animatedValue as Float
            invalidateSelf()
        }
    }

    @get:ColorInt
    var color: Int
        get() = paint.color
        set(value) {
            paint.color = value
            invalidateSelf()
        }

    override fun getIntrinsicWidth(): Int = slotSize

    override fun getIntrinsicHeight(): Int = slotSize

    override fun draw(canvas: Canvas) {
        val inset = paint.strokeWidth / 2f
        val left = bounds.exactCenterX() - size / 2f
        val top = bounds.exactCenterY() - size / 2f
        arc.set(left + inset, top + inset, left + size - inset, top + size - inset)
        canvas.withRotation(rotation, bounds.exactCenterX(), bounds.exactCenterY()) {
            drawArc(arc, START_ANGLE, SWEEP_ANGLE, false, paint)
        }
    }

    override fun start() {
        if (!animator.isStarted) animator.start()
    }

    override fun stop() {
        animator.cancel()
        rotation = 0f
        invalidateSelf()
    }

    override fun isRunning(): Boolean = animator.isRunning

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        if (!visible) stop()
        return super.setVisible(visible, restart)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private companion object {
        const val FULL_TURN = 360f
        const val START_ANGLE = 45f
        const val SWEEP_ANGLE = 270f
    }
}
