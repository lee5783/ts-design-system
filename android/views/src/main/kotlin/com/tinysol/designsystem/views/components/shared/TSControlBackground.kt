package com.tinysol.designsystem.views.components.shared

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

internal class TSControlBackground : Drawable() {
    private val fill = MaterialShapeDrawable()
    private val ring = MaterialShapeDrawable()
    private val ringGap = MaterialShapeDrawable()
    private var ringWidth = 0f
    private var cornerRadius = 0f
    private var ringOutside = false
    private var visualWidth = 0
    private var visualHeight = 0
    private var showsRing = false

    val visualBounds: Rect
        get() = Rect(fill.bounds)

    val ringBounds: Rect
        get() = Rect(ring.bounds)

    val drawsRingOutside: Boolean
        get() = ringOutside

    fun update(
        @ColorInt fillColor: Int,
        @ColorInt borderColor: Int,
        @Px borderWidth: Float,
        @Px cornerRadius: Float,
        @ColorInt ringColor: Int,
        @ColorInt ringGapColor: Int,
        @Px ringWidth: Float,
        showsRing: Boolean,
    ) {
        val shape = ShapeAppearanceModel.builder().setAllCornerSizes(cornerRadius).build()
        fill.shapeAppearanceModel = shape
        fill.fillColor = ColorStateList.valueOf(fillColor)
        fill.setStroke(borderWidth, borderColor)
        ring.fillColor = ColorStateList.valueOf(0)
        ring.setStroke(ringWidth, ringColor)
        ringGap.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes((cornerRadius - ringWidth).coerceAtLeast(0f)).build()
        ringGap.fillColor = ColorStateList.valueOf(0)
        ringGap.setStroke(ringWidth, ringGapColor)
        this.ringWidth = ringWidth
        this.cornerRadius = cornerRadius
        this.showsRing = showsRing
        layoutLayers(bounds)
    }

    fun setVisualSize(@Px width: Int, @Px height: Int) {
        if (width == visualWidth && height == visualHeight) return
        visualWidth = width
        visualHeight = height
        layoutLayers(bounds)
    }

    override fun onBoundsChange(bounds: Rect) {
        layoutLayers(bounds)
    }

    private fun layoutLayers(bounds: Rect) {
        val width = visualWidth.takeIf { it in 1..bounds.width() } ?: bounds.width()
        val height = visualHeight.takeIf { it in 1..bounds.height() } ?: bounds.height()
        val left = bounds.left + (bounds.width() - width) / 2
        val top = bounds.top + (bounds.height() - height) / 2
        val inner = Rect(left, top, left + width, top + height)
        fill.bounds = inner
        val gap = ringWidth.toInt()
        val outer = Rect(inner).apply { inset(-gap * 2, -gap * 2) }
        ringOutside = gap > 0 && bounds.contains(outer)
        if (ringOutside) {
            ring.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(cornerRadius + gap * 2).build()
            ring.bounds = outer
        } else {
            ring.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(cornerRadius).build()
            ring.bounds = inner
        }
        ringGap.setBounds(inner.left + gap, inner.top + gap, inner.right - gap, inner.bottom - gap)
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        fill.draw(canvas)
        if (showsRing) {
            ring.draw(canvas)
            if (!ringOutside) ringGap.draw(canvas)
        }
    }

    override fun getOutline(outline: Outline) {
        fill.getOutline(outline)
    }

    override fun setAlpha(alpha: Int) {
        fill.alpha = alpha
        ring.alpha = alpha
        ringGap.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fill.colorFilter = colorFilter
        ring.colorFilter = colorFilter
        ringGap.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
