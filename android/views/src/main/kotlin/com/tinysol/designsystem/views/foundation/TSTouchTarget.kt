package com.tinysol.designsystem.views.foundation

import android.graphics.Rect
import android.graphics.Region
import android.os.Build
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.Px
import androidx.annotation.RequiresApi
import com.tinysol.designsystem.views.R

public fun View.ensureMinTouchTarget(
    @Px minSize: Int = resources.getDimensionPixelSize(R.dimen.ts_min_touch_target),
) {
    val listener = TSTouchTargetListener(this, minSize)
    (getTag(R.id.ts_touch_target_listener) as? TSTouchTargetListener)?.let {
        removeOnLayoutChangeListener(it)
        removeOnAttachStateChangeListener(it)
    }
    setTag(R.id.ts_touch_target_listener, listener)
    addOnLayoutChangeListener(listener)
    addOnAttachStateChangeListener(listener)
    if (isLaidOut) {
        listener.update()
    }
}

public fun View.clearMinTouchTarget() {
    (getTag(R.id.ts_touch_target_listener) as? TSTouchTargetListener)?.let {
        removeOnLayoutChangeListener(it)
        removeOnAttachStateChangeListener(it)
    }
    setTag(R.id.ts_touch_target_listener, null)
    ((parent as? View)?.touchDelegate as? TSTouchDelegateGroup)?.remove(this)
}

internal fun expandedTouchBounds(hitRect: Rect, @Px minSize: Int): Rect {
    val horizontal = (minSize - hitRect.width()).coerceAtLeast(0)
    val vertical = (minSize - hitRect.height()).coerceAtLeast(0)
    return Rect(
        hitRect.left - horizontal / 2,
        hitRect.top - vertical / 2,
        hitRect.right + horizontal - horizontal / 2,
        hitRect.bottom + vertical - vertical / 2,
    )
}

private class TSTouchTargetListener(
    private val view: View,
    @Px private val minSize: Int,
) : View.OnLayoutChangeListener, View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(v: View) {
        if (view.isLaidOut) update()
    }

    override fun onViewDetachedFromWindow(v: View) {
        ((view.parent as? View)?.touchDelegate as? TSTouchDelegateGroup)?.remove(view)
    }

    override fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int,
    ) {
        update()
    }

    fun update() {
        val host = view.parent as? View ?: return
        val group = host.touchDelegate as? TSTouchDelegateGroup ?: TSTouchDelegateGroup(host).also { host.touchDelegate = it }
        if (view.visibility != View.VISIBLE) {
            group.remove(view)
            return
        }
        val hitRect = Rect().also(view::getHitRect)
        group.put(view, expandedTouchBounds(hitRect, minSize))
    }
}

internal class TSTouchDelegateGroup(host: View) : TouchDelegate(Rect(), host) {
    private val boundsByView = LinkedHashMap<View, Rect>()
    private val delegates = LinkedHashMap<View, TouchDelegate>()
    private var target: TouchDelegate? = null

    val bounds: Map<View, Rect>
        get() = boundsByView.toMap()

    fun put(view: View, bounds: Rect, delegate: TouchDelegate = TouchDelegate(Rect(bounds), view)) {
        boundsByView[view] = Rect(bounds)
        delegates[view] = delegate
    }

    fun remove(view: View) {
        boundsByView.remove(view)
        delegates.remove(view)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getTouchDelegateInfo(): AccessibilityNodeInfo.TouchDelegateInfo {
        if (boundsByView.isEmpty()) return super.getTouchDelegateInfo()
        return AccessibilityNodeInfo.TouchDelegateInfo(boundsByView.entries.associate { (view, bounds) -> Region(bounds) to view })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            target = delegates.values.reversed().firstOrNull { it.onTouchEventCopy(event) }
            return target != null
        }
        val handled = target?.onTouchEventCopy(event) ?: false
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            target = null
        }
        return handled
    }

    private fun TouchDelegate.onTouchEventCopy(event: MotionEvent): Boolean {
        val copy = MotionEvent.obtain(event)
        val handled = onTouchEvent(copy)
        copy.recycle()
        return handled
    }
}

internal class TSOffsetTouchDelegate(
    private val bounds: Rect,
    private val target: View,
    private val origin: () -> Pair<Int, Int>,
) : TouchDelegate(bounds, target) {
    private var tracking = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) tracking = bounds.contains(event.x.toInt(), event.y.toInt())
        if (!tracking) return false
        val (left, top) = origin()
        val forwarded = MotionEvent.obtain(event).apply {
            setLocation(
                (event.x - left).coerceIn(0f, (target.width - 1).coerceAtLeast(0).toFloat()),
                (event.y - top).coerceIn(0f, (target.height - 1).coerceAtLeast(0).toFloat()),
            )
        }
        val handled = target.dispatchTouchEvent(forwarded)
        forwarded.recycle()
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) tracking = false
        return handled
    }
}
