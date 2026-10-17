package com.ts.ds.views.components.dialog

import android.content.Context
import android.util.AttributeSet
import android.graphics.Rect
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.WindowInsetsCompat
import com.ts.ds.core.style.TSDialogStyleSpec
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.foundation.applySystemBarInsetsAsPadding
import com.ts.ds.views.theme.tsColor
import kotlin.math.abs

internal class TSDialogScrimLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    var panel: TSDialogPanelView? = null
        set(value) {
            field?.let(::removeView)
            field = value
            value?.let(::attachPanel)
        }

    var onScrimTap: () -> Unit = {}

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val panelBounds = Rect()
    private var downX = 0f
    private var downY = 0f
    private var tracking = false

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        applySystemBarInsetsAsPadding(
            start = true,
            top = true,
            end = true,
            bottom = true,
            insetTypes = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
        )
    }

    private fun attachPanel(panel: TSDialogPanelView) {
        val resolved: TSDialogStyleSpec.Resolved = panel.resolved
        setBackgroundColor(tsColor(resolved.scrim))
        val margin = dpToPx(resolved.outerMarginDp)
        addView(
            panel,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER).apply { setMargins(margin, margin, margin, margin) },
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                panel?.getHitRect(panelBounds) ?: panelBounds.setEmpty()
                tracking = !panelBounds.contains(event.x.toInt(), event.y.toInt())
                downX = event.x
                downY = event.y
            }
            MotionEvent.ACTION_MOVE -> if (abs(event.x - downX) > touchSlop || abs(event.y - downY) > touchSlop) tracking = false
            MotionEvent.ACTION_CANCEL -> tracking = false
            MotionEvent.ACTION_UP -> if (tracking) {
                tracking = false
                onScrimTap()
                return true
            }
        }
        return super.dispatchTouchEvent(event) || tracking
    }
}
