package com.ts.ds.views.components.toast

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.ts.ds.core.logic.TSToastEntry
import com.ts.ds.core.style.TSToastStyleSpec
import com.ts.ds.core.style.resolve
import com.ts.ds.views.components.shared.dpToPx

internal class TSToastLayer(context: Context) : FrameLayout(context) {
    var anchorView: View? = null
        set(value) {
            if (field === value) return
            field?.removeOnLayoutChangeListener(anchorListener)
            field = value
            value?.addOnLayoutChangeListener(anchorListener)
            requestLayout()
        }

    var bottomInset: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    var reduceMotion: () -> Boolean = { false }

    var toastView: TSToastView? = null
        private set

    var shownEntry: TSToastEntry? = null
        private set

    private val resolved = TSToastStyleSpec.resolve(TSToastStyleSpec.Configuration())
    private val margin = dpToPx(resolved.outerMarginDp)
    private var systemInsets = Insets.NONE
    private val anchorListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> requestLayout() }

    init {
        clipChildren = false
        clipToPadding = false
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            systemInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime(),
            )
            requestLayout()
            insets
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewCompat.requestApplyInsets(this)
    }

    fun show(entry: TSToastEntry?, perform: (TSToastEntry) -> Unit) {
        if (entry?.id == shownEntry?.id) return
        toastView?.let(::animateOut)
        toastView = null
        shownEntry = entry
        if (entry == null) return
        val toast = TSToastView(context).apply {
            message = entry.message.text
            tone = entry.message.tone
            actionLabel = entry.message.actionLabel
            onAction = entry.message.actionLabel?.let { { perform(entry) } }
            visibility = INVISIBLE
        }
        addView(toast, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL))
        toastView = toast
        animateIn(toast)
    }

    fun clear() {
        toastView?.let(::removeView)
        toastView = null
        shownEntry = null
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val bottom = anchorOffset(MeasureSpec.getSize(heightMeasureSpec)) ?: systemInsets.bottom
        for (index in 0 until childCount) {
            (getChildAt(index).layoutParams as LayoutParams).setMargins(
                margin + systemInsets.left,
                margin + systemInsets.top,
                margin + systemInsets.right,
                margin + bottom + bottomInset,
            )
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun anchorOffset(height: Int): Int? {
        val anchor = anchorView?.takeIf { it.isAttachedToWindow && it.isVisible } ?: return null
        val anchorLocation = IntArray(2).also(anchor::getLocationInWindow)
        val layerLocation = IntArray(2).also(::getLocationInWindow)
        return (layerLocation[1] + height - anchorLocation[1]).coerceAtLeast(0)
    }

    private fun animateIn(toast: TSToastView) {
        val reduce = reduceMotion()
        toast.alpha = 0f
        toast.post {
            toast.visibility = VISIBLE
            toast.translationY = if (reduce) 0f else toast.height / 2f
            toast.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(resolved.enterDurationMillis.toLong())
                .start()
        }
    }

    private fun animateOut(toast: TSToastView) {
        toast.actionView.isEnabled = false
        toast.animate().cancel()
        toast.animate()
            .alpha(0f)
            .setDuration(resolved.exitDurationMillis.toLong())
            .withEndAction { removeView(toast) }
            .start()
    }

    companion object {
        fun attach(container: ViewGroup): TSToastLayer = TSToastLayer(container.context).also {
            container.addView(it, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }
}
