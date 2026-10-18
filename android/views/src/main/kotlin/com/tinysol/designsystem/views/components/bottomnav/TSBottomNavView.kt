package com.tinysol.designsystem.views.components.bottomnav

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import com.tinysol.designsystem.core.style.TSBottomNavItem
import com.tinysol.designsystem.core.style.TSBottomNavStyleSpec
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.shared.TSViewSavedState
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.components.shared.restoreTSViewState
import com.tinysol.designsystem.views.foundation.applySystemBarInsetsAsPadding
import com.tinysol.designsystem.views.theme.tsColor

public class TSBottomNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    public fun interface OnItemSelectedListener {
        public fun onItemSelected(view: TSBottomNavView, id: String)
    }

    private val hairlinePaint = Paint()
    private val itemViews = mutableListOf<TSBottomNavItemView>()
    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var selection = ""
    private var ready = false

    internal var reduceMotion: () -> Boolean = { false }
        set(value) {
            field = value
            itemViews.forEach { it.reduceMotion = value }
        }

    public var items: List<TSBottomNavItem> = emptyList()
        set(value) {
            val sameIds = value.map { it.id } == field.map { it.id }
            field = value
            if (sameIds && itemViews.size == value.size) updateItems() else rebuildItems()
        }

    public var selectedId: String
        get() = items.getOrNull(selectedIndex)?.id ?: selection
        set(value) {
            selection = value
            applySelection(animated = false)
        }

    init {
        orientation = HORIZONTAL
        setWillNotDraw(false)
        val container = TSBottomNavStyleSpec.container
        setBackgroundColor(tsColor(container.fill))
        hairlinePaint.color = tsColor(container.border)
        applySystemBarInsetsAsPadding(
            start = true,
            end = true,
            bottom = true,
            insetTypes = WindowInsetsCompat.Type.navigationBars(),
        )
        context.obtainStyledAttributes(attrs, R.styleable.TSBottomNavView, defStyleAttr, 0).apply {
            isEnabled = getBoolean(R.styleable.TSBottomNavView_android_enabled, true)
            recycle()
        }
        ready = true
        rebuildItems()
    }

    public fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        onItemSelectedListener = listener
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
        itemViews.forEach { it.isEnabled = enabled }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = dpToPx(TSBottomNavStyleSpec.container.heightDp) + paddingTop + paddingBottom
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), dpToPx(TSBottomNavStyleSpec.container.borderWidthDp).toFloat(), hairlinePaint)
    }

    internal val selectedIndex: Int
        get() = TSBottomNavStyleSpec.selectedIndex(items, selection)

    internal val itemViewList: List<TSBottomNavItemView>
        get() = itemViews

    private fun rebuildItems() {
        if (!ready) return
        removeAllViews()
        itemViews.clear()
        items.forEachIndexed { index, item ->
            val view = TSBottomNavItemView(context).apply {
                this.item = item
                reduceMotion = this@TSBottomNavView.reduceMotion
                isEnabled = this@TSBottomNavView.isEnabled
                setOnClickListener { userSelect(index) }
            }
            itemViews += view
            addView(view, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
        }
        applySelection(animated = false)
    }

    private fun updateItems() {
        items.forEachIndexed { index, item -> itemViews[index].item = item }
        applySelection(animated = false)
    }

    private fun userSelect(index: Int) {
        val item = items.getOrNull(index) ?: return
        if (!isEnabled || index == selectedIndex) return
        selection = item.id
        applySelection(animated = true)
        onItemSelectedListener?.onItemSelected(this, item.id)
    }

    private fun applySelection(animated: Boolean) {
        if (!ready) return
        val selectedIndex = selectedIndex
        itemViews.forEachIndexed { index, view ->
            view.setItemSelected(index == selectedIndex, animated)
        }
    }

    private companion object {
        const val SELECTED_KEY = "ts.selectedId"
    }
}
