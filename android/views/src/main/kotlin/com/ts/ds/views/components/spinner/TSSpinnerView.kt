package com.ts.ds.views.components.spinner

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.ts.ds.core.style.TSSpinnerSize
import com.ts.ds.core.style.TSSpinnerStyleSpec
import com.ts.ds.core.style.TSSpinnerTone
import com.ts.ds.core.style.resolve
import com.ts.ds.views.R
import com.ts.ds.views.components.shared.TSSpinnerDrawable
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.theme.tsColor

public class TSSpinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var spinner: TSSpinnerDrawable = makeDrawable(TSSpinnerSize.Md)
    private var ready = false

    public var size: TSSpinnerSize = TSSpinnerSize.Md
        set(value) {
            if (field == value) return
            field = value
            rebuild()
        }

    public var tone: TSSpinnerTone = TSSpinnerTone.Primary
        set(value) {
            field = value
            applyColor()
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TSSpinnerView, defStyleAttr, 0).apply {
            size = TSSpinnerSize.entries[getInt(R.styleable.TSSpinnerView_tsSpinnerSize, TSSpinnerSize.Md.ordinal)]
            tone = TSSpinnerTone.entries[getInt(R.styleable.TSSpinnerView_tsSpinnerTone, TSSpinnerTone.Primary.ordinal)]
            recycle()
        }
        ready = true
        rebuild()
    }

    internal val resolved: TSSpinnerStyleSpec.Resolved
        get() = TSSpinnerStyleSpec.resolve(TSSpinnerStyleSpec.Configuration(size, tone))

    internal var isSpinning: Boolean = false
        private set

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val side = dpToPx(resolved.sizeDp)
        setMeasuredDimension(resolveSize(side, widthMeasureSpec), resolveSize(side, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        spinner.setBounds(0, 0, width, height)
        spinner.draw(canvas)
    }

    override fun verifyDrawable(who: Drawable): Boolean = who === spinner || super.verifyDrawable(who)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateAnimation()
    }

    override fun onDetachedFromWindow() {
        isSpinning = false
        spinner.stop()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (ready) updateAnimation()
    }

    private fun makeDrawable(size: TSSpinnerSize): TSSpinnerDrawable {
        val resolved = TSSpinnerStyleSpec.resolve(TSSpinnerStyleSpec.Configuration(size))
        val side = dpToPx(resolved.sizeDp)
        return TSSpinnerDrawable(
            size = side,
            slotSize = side,
            strokeWidth = resolved.strokeWidthDp * resources.displayMetrics.density,
            rotationMillis = resolved.rotationMillis.toLong(),
        )
    }

    private fun rebuild() {
        if (!ready) return
        spinner.stop()
        spinner.callback = null
        spinner = makeDrawable(size)
        spinner.callback = this
        applyColor()
        updateAnimation()
        requestLayout()
    }

    private fun applyColor() {
        if (!ready) return
        spinner.color = tsColor(resolved.color)
        invalidate()
    }

    private fun updateAnimation() {
        isSpinning = isAttachedToWindow && isShown
        if (isSpinning) spinner.start() else spinner.stop()
    }
}
