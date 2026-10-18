package com.ts.ds.views.components.listitem

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import com.ts.ds.core.icon.TSIcons
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSListGroupPosition
import com.ts.ds.core.style.TSListItemStyleSpec
import com.ts.ds.views.R
import com.ts.ds.views.components.shared.TSViewSavedState
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.components.shared.restoreTSViewState

public class TSDisclosureListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    public fun interface OnExpandedChangeListener {
        public fun onExpandedChanged(view: TSDisclosureListItemView, isExpanded: Boolean)
    }

    internal val headerView = TSListItemView(context)
    internal val bodyContainer = LinearLayout(context)
    private var onExpandedChangeListener: OnExpandedChangeListener? = null
    private var ready = false
    internal var reduceMotion: () -> Boolean = { false }

    public var title: CharSequence
        get() = headerView.title
        set(value) {
            headerView.title = value
        }

    public var subtitle: CharSequence?
        get() = headerView.subtitle
        set(value) {
            headerView.subtitle = value
        }

    public var leading: TSListItemLeading?
        get() = headerView.leading
        set(value) {
            headerView.leading = value
        }

    private var expanded = false

    public var isExpanded: Boolean
        get() = expanded
        set(value) {
            updateExpanded(value, animated = isAttachedToWindow)
        }

    internal var groupPosition: TSListGroupPosition? = null
        set(value) {
            field = value
            applyGroupPosition()
        }

    init {
        orientation = VERTICAL
        bodyContainer.orientation = VERTICAL
        val disclosure = TSListItemStyleSpec.disclosure
        val horizontal = dpToPx(disclosure.bodyHorizontalPaddingDp)
        bodyContainer.setPaddingRelative(horizontal, 0, horizontal, dpToPx(disclosure.bodyBottomPaddingDp))
        super.addView(headerView, -1, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        super.addView(bodyContainer, -1, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        headerView.showsChevron = true
        headerView.setOnClickListener { userDidToggle() }
        context.obtainStyledAttributes(attrs, R.styleable.TSDisclosureListItemView, defStyleAttr, 0).apply {
            title = getText(R.styleable.TSDisclosureListItemView_tsTitle) ?: ""
            subtitle = getText(R.styleable.TSDisclosureListItemView_tsSubtitle)
            leading = getString(R.styleable.TSDisclosureListItemView_tsLeadingIcon)?.let(TSIcons::fromDsName)?.let(TSListItemLeading::Icon)
                ?: getString(R.styleable.TSDisclosureListItemView_tsName)?.let { TSListItemLeading.Avatar(it) }
            isExpanded = getBoolean(R.styleable.TSDisclosureListItemView_tsExpanded, false)
            recycle()
        }
        ready = true
        applyExpansion(animated = false)
    }

    public fun setOnExpandedChangeListener(listener: OnExpandedChangeListener?) {
        onExpandedChangeListener = listener
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child === headerView || child === bodyContainer) {
            super.addView(child, index, params)
        } else {
            bodyContainer.addView(child, index, params)
        }
    }

    override fun removeAllViews() {
        bodyContainer.removeAllViews()
    }

    override fun removeView(view: View) {
        if (view.parent === bodyContainer) bodyContainer.removeView(view)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        headerView.isEnabled = enabled
        bodyContainer.alpha = if (enabled) 1f else TSListItemStyleSpec.resolve(TSListItemStyleSpec.Configuration(), TSControlState.Disabled).opacity
    }

    override fun onSaveInstanceState(): Parcelable =
        TSViewSavedState(super.onSaveInstanceState(), bundleOf(EXPANDED_KEY to isExpanded))

    override fun onRestoreInstanceState(state: Parcelable?) {
        restoreTSViewState(state, { super.onRestoreInstanceState(it) }) { updateExpanded(it.getBoolean(EXPANDED_KEY), animated = false) }
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        if (ready) headerView.chevronView.rotation = chevronRotation
    }

    internal val chevronRotation: Float
        get() = TSListItemStyleSpec.disclosure.rotationDegrees(isExpanded, layoutDirection == LAYOUT_DIRECTION_RTL)

    private fun updateExpanded(value: Boolean, animated: Boolean) {
        if (expanded == value) return
        expanded = value
        applyExpansion(animated)
    }

    private fun userDidToggle() {
        if (!isEnabled) return
        isExpanded = !isExpanded
        onExpandedChangeListener?.onExpandedChanged(this, isExpanded)
    }

    private fun applyExpansion(animated: Boolean) {
        if (!ready) return
        bodyContainer.visibility = if (isExpanded) VISIBLE else GONE
        headerView.expansionState = isExpanded
        applyGroupPosition()
        val disclosure = TSListItemStyleSpec.disclosure
        val duration = disclosure.animationDurationMillis(!animated || reduceMotion())
        val chevron = headerView.chevronView
        chevron.animate().cancel()
        if (duration == 0) {
            chevron.rotation = chevronRotation
        } else {
            val easing = disclosure.animationEasing
            chevron.animate()
                .rotation(chevronRotation)
                .setDuration(duration.toLong())
                .setInterpolator(PathInterpolator(easing.x1, easing.y1, easing.x2, easing.y2))
                .start()
        }
    }

    private fun applyGroupPosition() {
        val position = groupPosition
        headerView.groupPosition = when {
            position == null || !isExpanded -> position
            position.roundsTop -> TSListGroupPosition.First
            else -> TSListGroupPosition.Middle
        }
    }

    private companion object {
        private const val EXPANDED_KEY = "ts.expanded"
    }
}
