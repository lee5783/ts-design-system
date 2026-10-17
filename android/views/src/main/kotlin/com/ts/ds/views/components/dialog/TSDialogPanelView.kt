package com.ts.ds.views.components.dialog

import android.content.Context
import android.util.AttributeSet
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.R as CoreR
import com.ts.ds.core.logic.TSDialogAction
import com.ts.ds.core.logic.TSDialogLayout
import com.ts.ds.core.style.TSDialogActionsAxis
import com.ts.ds.core.style.TSDialogStyleSpec
import com.ts.ds.core.style.TSDialogVariant
import com.ts.ds.core.style.resolve
import com.ts.ds.views.components.button.TSButtonView
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor

internal class TSDialogPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    internal val grabberView = View(context)
    internal val titleView = MaterialTextView(context)
    internal val messageView = MaterialTextView(context)
    internal val actionsView = LinearLayout(context)
    private val scrollView = NestedScrollView(context)
    private val contentView = LinearLayout(context)
    private val bodyContainer = FrameLayout(context)
    private var closeActionId = View.NO_ID

    var variant: TSDialogVariant = TSDialogVariant.Center
        set(value) {
            field = value
            applyConfiguration()
        }

    var onAction: (TSDialogAction) -> Unit = {}
    var onClose: () -> Unit = {}

    var title: CharSequence = ""
        set(value) {
            field = value
            applyConfiguration()
        }

    var message: CharSequence? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    var bodyView: View? = null
        set(value) {
            if (field === value) return
            field?.let(bodyContainer::removeView)
            field = value
            value?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                bodyContainer.addView(it, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
            applyConfiguration()
        }

    var actions: List<TSDialogAction> = emptyList()
        set(value) {
            field = value
            applyConfiguration()
        }

    internal val resolved: TSDialogStyleSpec.Resolved
        get() = TSDialogStyleSpec.resolve(TSDialogStyleSpec.configuration(variant, resources.configuration.fontScale))

    internal val actionButtons: List<TSButtonView>
        get() = (0 until actionsView.childCount).map { actionsView.getChildAt(it) as TSButtonView }

    init {
        orientation = VERTICAL
        contentView.orientation = VERTICAL
        listOf(titleView, messageView).forEach {
            it.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            it.gravity = Gravity.START
        }
        ViewCompat.setAccessibilityHeading(titleView, true)
        addView(grabberView, LayoutParams(0, 0).apply { gravity = Gravity.CENTER_HORIZONTAL })
        contentView.addView(titleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        bodyContainer.addView(messageView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        contentView.addView(bodyContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        contentView.addView(actionsView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        scrollView.isFillViewport = false
        scrollView.addView(contentView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        addView(scrollView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        applyConfiguration()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = resolved.maxWidthDp?.let { dpToPx(it) }
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val spec = if (maxWidth != null && mode != MeasureSpec.UNSPECIFIED && MeasureSpec.getSize(widthMeasureSpec) > maxWidth) {
            MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY)
        } else {
            widthMeasureSpec
        }
        super.onMeasure(spec, heightMeasureSpec)
    }

    private fun applyConfiguration() {
        val resolved = resolved
        background = GradientDrawable().apply {
            setColor(tsColor(resolved.fill))
            val radius = dpToPx(resolved.cornerRadiusDp).toFloat()
            val bottom = if (resolved.roundsBottomCorners) radius else 0f
            cornerRadii = floatArrayOf(radius, radius, radius, radius, bottom, bottom, bottom, bottom)
        }
        elevation = resolved.shadow?.let { dpToPx(it.values.elevation).toFloat() } ?: 0f
        val horizontal = dpToPx(resolved.paddingHorizontalDp)
        setPaddingRelative(horizontal, dpToPx(resolved.paddingTopDp), horizontal, dpToPx(resolved.paddingBottomDp))
        grabberView.visibility = if (resolved.showsGrabber) VISIBLE else GONE
        grabberView.background = GradientDrawable().apply {
            setColor(tsColor(resolved.grabberColor))
            cornerRadius = dpToPx(resolved.grabberHeightDp).toFloat()
        }
        grabberView.layoutParams = (grabberView.layoutParams as LayoutParams).apply {
            width = dpToPx(resolved.grabberWidthDp)
            height = dpToPx(resolved.grabberHeightDp)
            bottomMargin = dpToPx(resolved.grabberSpacingDp)
        }
        grabberView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        titleView.text = title
        titleView.setTextAppearance(resolved.titleTypeRole.textAppearance)
        titleView.setTextColor(tsColor(resolved.titleColor))
        bodyContainer.layoutParams = (bodyContainer.layoutParams as LayoutParams).apply {
            topMargin = dpToPx(resolved.titleSpacingDp)
        }
        messageView.text = message
        messageView.visibility = if (message.isNullOrEmpty() || bodyView != null) GONE else VISIBLE
        messageView.setTextAppearance(resolved.bodyTypeRole.textAppearance)
        messageView.setTextColor(tsColor(resolved.bodyColor))
        bodyContainer.isVisible = messageView.isVisible || bodyView != null
        applyActions(resolved)
        applyAccessibility()
    }

    private fun applyActions(resolved: TSDialogStyleSpec.Resolved) {
        actionsView.removeAllViews()
        actionsView.visibility = if (actions.isEmpty()) GONE else VISIBLE
        actionsView.layoutParams = (actionsView.layoutParams as LayoutParams).apply {
            topMargin = dpToPx(resolved.actionsTopSpacingDp)
        }
        val vertical = resolved.actionsAxis == TSDialogActionsAxis.Vertical
        actionsView.orientation = if (vertical) VERTICAL else HORIZONTAL
        actionsView.gravity = if (vertical) Gravity.CENTER_HORIZONTAL else Gravity.END or Gravity.CENTER_VERTICAL
        val spacing = dpToPx(resolved.actionsSpacingDp)
        TSDialogLayout.orderedActions(actions, resolved.primaryActionFirst).forEachIndexed { index, action ->
            val button = TSButtonView(context).apply {
                text = action.label
                variant = action.variant
                setOnClickListener { onAction(action) }
            }
            val width = if (resolved.blockActions) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT
            actionsView.addView(
                button,
                LayoutParams(width, LayoutParams.WRAP_CONTENT).apply {
                    if (vertical) topMargin = if (index > 0) spacing else 0 else marginStart = if (index > 0) spacing else 0
                },
            )
        }
    }

    private fun applyAccessibility() {
        ViewCompat.setAccessibilityPaneTitle(this, title)
        if (closeActionId != View.NO_ID) ViewCompat.removeAccessibilityAction(this, closeActionId)
        closeActionId = ViewCompat.addAccessibilityAction(this, context.getString(CoreR.string.ts_close)) { _, _ ->
            onClose()
            true
        }
        ViewCompat.replaceAccessibilityAction(this, AccessibilityActionCompat.ACTION_DISMISS, context.getString(CoreR.string.ts_close)) { _, _ ->
            onClose()
            true
        }
    }
}
