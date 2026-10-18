package com.ts.ds.views.components.tabs

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.core.style.TSTabsStyleSpec
import com.ts.ds.core.style.TSTabsVariant
import com.ts.ds.core.style.resolve
import com.ts.ds.views.R
import com.ts.ds.views.components.shared.TSViewSavedState
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.components.shared.restoreTSViewState
import com.ts.ds.views.foundation.ensureMinTouchTarget
import com.ts.ds.views.theme.tsColor
import kotlin.math.roundToInt

public class TSTabsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    public fun interface OnTabSelectedListener {
        public fun onTabSelected(view: TSTabsView, id: String)
    }

    private inner class MarkerLayout(context: Context) : FrameLayout(context) {
        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
            onContentLayout()
        }
    }

    internal class TabScrollView(context: Context) : HorizontalScrollView(context) {
        var scrollable: Boolean = true

        override fun measureChild(child: View, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
            if (scrollable) super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec) else measureFitting(child, parentWidthMeasureSpec, parentHeightMeasureSpec)
        }

        override fun measureChildWithMargins(child: View, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int) {
            if (scrollable) {
                super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed)
            } else {
                measureFitting(child, parentWidthMeasureSpec, parentHeightMeasureSpec)
            }
        }

        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = scrollable && super.onInterceptTouchEvent(ev)

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(ev: MotionEvent): Boolean = scrollable && super.onTouchEvent(ev)

        private fun measureFitting(child: View, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
            val params = child.layoutParams
            child.measure(
                getChildMeasureSpec(parentWidthMeasureSpec, paddingLeft + paddingRight, params.width),
                getChildMeasureSpec(parentHeightMeasureSpec, paddingTop + paddingBottom, params.height),
            )
        }
    }

    internal val scrollView = TabScrollView(context)
    private val content = MarkerLayout(context)
    internal val row = LinearLayout(context)
    internal val thumbView = View(context)
    internal val indicatorView = View(context)
    private val thumbFill = MaterialShapeDrawable()
    private val track = MaterialShapeDrawable()
    private val hairlinePaint = Paint()
    private val tabViews = mutableListOf<TSTabView>()
    private var markerLeft = 0f
    private var markerWidth = 0f
    private var markerPlaced = false
    private var animator: ValueAnimator? = null
    private var pendingScroll = true
    private var onTabSelectedListener: OnTabSelectedListener? = null
    private var selection = ""
    private var ready = false

    internal var reduceMotion: () -> Boolean = { false }

    public var items: List<TSTabItem> = emptyList()
        set(value) {
            field = value
            rebuildTabs()
        }

    public var selectedId: String
        get() = selection
        set(value) {
            selection = value
            applySelection(animated = false)
        }

    public var variant: TSTabsVariant = TSTabsVariant.Underline
        set(value) {
            field = value
            applyConfiguration()
        }

    public var isBlock: Boolean = false
        set(value) {
            field = value
            applyConfiguration()
        }

    init {
        setWillNotDraw(false)
        scrollView.isHorizontalScrollBarEnabled = false
        scrollView.overScrollMode = OVER_SCROLL_NEVER
        scrollView.clipToPadding = false
        row.orientation = LinearLayout.HORIZONTAL
        row.outlineProvider = null
        thumbView.background = thumbFill
        indicatorView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        thumbView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        content.addView(thumbView, LayoutParams(0, 0))
        content.addView(row, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START or Gravity.TOP))
        content.addView(indicatorView, LayoutParams(0, 0))
        scrollView.addView(content, LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(scrollView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.START or Gravity.TOP))
        context.obtainStyledAttributes(attrs, R.styleable.TSTabsView, defStyleAttr, 0).apply {
            variant = TSTabsVariant.entries[getInt(R.styleable.TSTabsView_tsTabsVariant, 0)]
            isBlock = getBoolean(R.styleable.TSTabsView_tsBlock, false)
            getTextArray(R.styleable.TSTabsView_android_entries)?.let { entries ->
                items = entries.map { TSTabItem(it.toString(), it.toString()) }
            }
            isEnabled = getBoolean(R.styleable.TSTabsView_android_enabled, true)
            recycle()
        }
        ready = true
        rebuildTabs()
    }

    public fun setOnTabSelectedListener(listener: OnTabSelectedListener?) {
        onTabSelectedListener = listener
    }

    override fun onSaveInstanceState(): Parcelable =
        TSViewSavedState(super.onSaveInstanceState(), bundleOf(SELECTED_KEY to selectedId))

    override fun onRestoreInstanceState(state: Parcelable?) {
        restoreTSViewState(state, { super.onRestoreInstanceState(it) }) { values ->
            values.getString(SELECTED_KEY)?.let { selectedId = it }
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!ready) return
        tabViews.forEach { it.isEnabled = enabled }
        applyContainer()
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        pendingScroll = true
    }

    override fun onDetachedFromWindow() {
        animator?.end()
        super.onDetachedFromWindow()
    }

    internal val container: TSTabsStyleSpec.Container
        get() = TSTabsStyleSpec.container(variant, isEnabled)

    internal val selectedIndex: Int
        get() = TSTabsStyleSpec.selectedIndex(items, selectedId)

    internal val tabs: List<TSTabView>
        get() = tabViews

    internal val runningAnimator: ValueAnimator?
        get() = animator?.takeIf { it.isRunning || it.isStarted }

    internal val trackBounds: Rect
        get() {
            val outset = dpToPx(container.touchOutsetDp)
            return Rect(scrollView.left, scrollView.top + outset, scrollView.right, scrollView.bottom - outset)
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val container = container
        val outset = dpToPx(container.touchOutsetDp)
        val stroke = dpToPx(container.borderWidthDp)
        when (variant) {
            TSTabsVariant.Underline -> {
                hairlinePaint.color = tsColor(container.border)
                val bottom = scrollView.bottom - outset
                canvas.drawRect(0f, (bottom - stroke).toFloat(), width.toFloat(), bottom.toFloat(), hairlinePaint)
            }
            TSTabsVariant.Segmented -> {
                track.bounds = trackBounds
                track.draw(canvas)
            }
        }
    }

    private fun rebuildTabs() {
        if (!ready) return
        animator?.cancel()
        row.removeAllViews()
        tabViews.clear()
        items.forEachIndexed { index, item ->
            val tab = TSTabView(context).apply {
                this.item = item
                isEnabled = this@TSTabsView.isEnabled
                setOnClickListener { userSelect(index) }
                ensureMinTouchTarget()
            }
            tabViews += tab
            row.addView(tab)
        }
        markerPlaced = false
        pendingScroll = true
        applyConfiguration()
    }

    private fun userSelect(index: Int) {
        val item = items.getOrNull(index) ?: return
        if (!isEnabled || index == selectedIndex) return
        selection = item.id
        if (variant == TSTabsVariant.Segmented) performHapticFeedback(DsHaptics.constantFor(DsHapticEvent.Selection))
        applySelection(animated = true)
        onTabSelectedListener?.onTabSelected(this, item.id)
    }

    private fun applyConfiguration() {
        if (!ready) return
        val container = container
        val segmented = variant == TSTabsVariant.Segmented
        val inset = dpToPx(container.contentInsetDp)
        val fillsWidth = !segmented || isBlock
        scrollView.isFillViewport = fillsWidth
        scrollView.scrollable = container.scrollable
        content.layoutParams = (content.layoutParams as LayoutParams).apply {
            width = if (segmented && isBlock) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        }
        scrollView.setPaddingRelative(inset, 0, inset, 0)
        scrollView.layoutParams = (scrollView.layoutParams as LayoutParams).apply {
            width = if (fillsWidth) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        }
        row.layoutParams = (row.layoutParams as LayoutParams).apply {
            width = if (segmented && isBlock) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val spacing = dpToPx(container.spacingDp)
        tabViews.forEachIndexed { index, tab ->
            tab.variant = variant
            tab.layoutParams = LinearLayout.LayoutParams(
                if (segmented && isBlock) 0 else ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                if (segmented && isBlock) 1f else 0f,
            ).apply { marginEnd = if (index < tabViews.lastIndex) spacing else 0 }
        }
        val selected = TSTabsStyleSpec.resolve(TSTabsStyleSpec.Configuration(variant, selected = true))
        thumbFill.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(dpToPx(selected.cornerRadiusDp).toFloat()).build()
        thumbFill.fillColor = ColorStateList.valueOf(selected.fill?.let(::tsColor) ?: 0)
        thumbView.elevation = selected.shadow?.let { dpToPx(it.values.elevation).toFloat() } ?: 0f
        row.translationZ = thumbView.elevation
        thumbView.visibility = if (segmented) VISIBLE else GONE
        indicatorView.setBackgroundColor(selected.indicatorColor?.let(::tsColor) ?: 0)
        indicatorView.visibility = if (segmented) GONE else VISIBLE
        track.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(dpToPx(container.cornerRadiusDp).toFloat()).build()
        track.fillColor = ColorStateList.valueOf(container.fill?.let(::tsColor) ?: 0)
        track.setStroke(dpToPx(container.borderWidthDp).toFloat(), tsColor(container.border))
        applySelection(animated = false)
        applyContainer()
        requestLayout()
        invalidate()
    }

    private fun applyContainer() {
        alpha = container.opacity
    }

    private fun applySelection(animated: Boolean) {
        if (!ready) return
        val selectedIndex = selectedIndex
        tabViews.forEachIndexed { index, tab ->
            tab.isTabSelected = index == selectedIndex
        }
        if (animated) {
            animateMarker()
            scrollSelectionIntoView(immediate = false)
        } else {
            animator?.cancel()
            pendingScroll = true
        }
        content.requestLayout()
    }

    private fun onContentLayout() {
        val target = markerTarget() ?: return
        if (!markerPlaced || runningAnimator == null) {
            markerLeft = target.first
            markerWidth = target.second
            markerPlaced = true
        }
        placeMarkers()
        if (pendingScroll) {
            pendingScroll = false
            scrollSelectionIntoView(immediate = true)
        }
    }

    private fun markerTarget(): Pair<Float, Float>? {
        val tab = tabViews.getOrNull(selectedIndex) ?: return null
        if (tab.width == 0) return null
        return (row.left + tab.left).toFloat() to tab.width.toFloat()
    }

    private fun placeMarkers() {
        val tab = tabViews.getOrNull(selectedIndex) ?: return
        val resolved = tab.resolved
        val outset = dpToPx(resolved.touchOutsetDp)
        val left = markerLeft.roundToInt()
        val right = (markerLeft + markerWidth).roundToInt()
        val top = row.top + tab.top
        val bottom = row.top + tab.bottom
        thumbView.layout(left, top + outset, right, bottom - outset)
        val indicatorHeight = dpToPx(TSTabsStyleSpec.resolve(TSTabsStyleSpec.Configuration(TSTabsVariant.Underline, selected = true)).indicatorHeightDp)
        indicatorView.layout(left, bottom - outset - indicatorHeight, right, bottom - outset)
    }

    private fun animateMarker() {
        animator?.cancel()
        animator = null
        val reference = TSTabsStyleSpec.resolve(TSTabsStyleSpec.Configuration(variant, selected = true))
        val duration = if (reduceMotion()) 0 else reference.animationDurationMillis
        if (duration == 0 || !markerPlaced) return
        val startLeft = markerLeft
        val startWidth = markerWidth
        val easing = reference.animationEasing
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration.toLong()
            interpolator = PathInterpolator(easing.x1, easing.y1, easing.x2, easing.y2)
            addUpdateListener {
                val target = markerTarget() ?: return@addUpdateListener
                val fraction = it.animatedValue as Float
                markerLeft = startLeft + (target.first - startLeft) * fraction
                markerWidth = startWidth + (target.second - startWidth) * fraction
                placeMarkers()
            }
            start()
        }
    }

    private fun scrollSelectionIntoView(immediate: Boolean) {
        val tab = tabViews.getOrNull(selectedIndex) ?: return
        if (variant != TSTabsVariant.Underline || tab.width == 0) return
        val rect = Rect(row.left + tab.left, 0, row.left + tab.right, tab.height)
        val reduce = immediate || reduceMotion()
        scrollView.post { scrollView.requestChildRectangleOnScreen(content, Rect(rect), reduce) }
    }

    private companion object {
        const val SELECTED_KEY = "ts.selectedId"
    }
}
