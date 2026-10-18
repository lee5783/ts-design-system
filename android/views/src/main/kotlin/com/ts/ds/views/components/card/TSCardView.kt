package com.ts.ds.views.components.card

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.logic.TSCardAccessory
import com.ts.ds.core.logic.TSCardAction
import com.ts.ds.core.style.TSCardStyleSpec
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.views.R
import com.ts.ds.views.components.button.TSButtonView
import com.ts.ds.views.components.iconbutton.TSIconButtonView
import com.ts.ds.views.components.shared.TSControlBackground
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor
import kotlin.math.roundToInt

public class TSCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private val contentView = LinearLayout(context)
    private val headerView = LinearLayout(context)
    private val headerTextColumn = LinearLayout(context)
    private val titleView = MaterialTextView(context)
    private val subtitleView = MaterialTextView(context)
    private val accessoryContainer = FrameLayout(context)
    private val bodyContainer = FrameLayout(context)
    private val bodyTextView = MaterialTextView(context)
    internal val dividerView = View(context)
    internal val footerView = LinearLayout(context)
    private val chrome = TSControlBackground()
    private val focusRingWidth = resources.getDimension(R.dimen.ts_focus_ring_width)
    private var ready = false

    public var title: CharSequence? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var subtitle: CharSequence? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var bodyText: CharSequence? = null
        set(value) {
            field = value
            if (value != null) bodyView = null
            applyConfiguration()
        }

    public var bodyView: View? = null
        set(value) {
            if (field === value) return
            field?.let(bodyContainer::removeView)
            field = value
            value?.let { bodyContainer.addView(it, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
            applyConfiguration()
        }

    private var typedAccessory: TSCardAccessory? = null

    public var accessoryView: View? = null
        set(value) {
            typedAccessory = null
            if (field !== value) {
                accessoryContainer.removeAllViews()
                field?.let { previous ->
                    previous.isEnabled = true
                    when (previous) {
                        is TSButtonView -> previous.isDimmedByContainer = false
                        is TSIconButtonView -> previous.isDimmedByContainer = false
                    }
                }
                field = value
                value?.let { accessoryContainer.addView(it, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
            }
            applyConfiguration()
        }

    public var accessory: TSCardAccessory?
        get() = typedAccessory
        set(value) {
            accessoryView = value?.let(::makeAccessoryView)
            typedAccessory = value
        }

    public var footerActions: List<TSCardAction> = emptyList()
        set(value) {
            field = value
            rebuildFooter()
            applyConfiguration()
        }

    public var isRaised: Boolean = false
        set(value) {
            field = value
            applyState()
        }

    public val isInteractive: Boolean
        get() = hasOnClickListeners()

    init {
        orientation = VERTICAL
        background = chrome
        clipToOutline = true
        contentView.orientation = VERTICAL
        headerView.orientation = HORIZONTAL
        headerView.gravity = Gravity.TOP
        headerTextColumn.orientation = VERTICAL
        listOf(titleView, subtitleView, bodyTextView).forEach {
            it.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            it.gravity = Gravity.START
        }
        headerTextColumn.addView(titleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        headerTextColumn.addView(subtitleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        headerView.addView(headerTextColumn, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        headerView.addView(accessoryContainer, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        bodyContainer.addView(bodyTextView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        contentView.addView(headerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        contentView.addView(bodyContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        contentView.addView(dividerView, LayoutParams(LayoutParams.MATCH_PARENT, 0))
        contentView.addView(footerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(contentView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        context.obtainStyledAttributes(attrs, R.styleable.TSCardView, defStyleAttr, 0).apply {
            title = getText(R.styleable.TSCardView_tsTitle)
            subtitle = getText(R.styleable.TSCardView_tsSubtitle)
            bodyText = getText(R.styleable.TSCardView_tsBodyText)
            isRaised = getBoolean(R.styleable.TSCardView_tsRaised, false)
            isEnabled = getBoolean(R.styleable.TSCardView_android_enabled, true)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
        if (listener == null) isClickable = false
        applyConfiguration()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (ready) applyConfiguration()
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed && isInteractive)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (ready) applyState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        applyConfiguration()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        chrome.setBounds(0, 0, w, h)
    }

    internal val currentState: TSControlState
        get() = TSControlState.make(pressed = isPressed, enabled = isEnabled, focused = isFocused)

    internal val resolved: TSCardStyleSpec.Resolved
        get() = TSCardStyleSpec.resolve(TSCardStyleSpec.Configuration(isRaised, isInteractive), currentState)

    internal val footerButtons: List<TSButtonView>
        get() = (0 until footerView.childCount).map { footerView.getChildAt(it) as TSButtonView }

    private fun rebuildFooter() {
        footerView.removeAllViews()
        footerActions.forEach { action ->
            footerView.addView(
                TSButtonView(context).apply {
                    text = action.label
                    variant = action.variant
                    setOnClickListener { action.onClick() }
                },
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT),
            )
        }
    }

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        val interactive = isInteractive
        isClickable = interactive
        isFocusable = interactive
        if (!interactive) isPressed = false
        val padding = dpToPx(resolved.paddingDp)
        val hasHeader = !title.isNullOrEmpty() || !subtitle.isNullOrEmpty() || accessoryView != null
        val hasBody = !bodyText.isNullOrEmpty() || bodyView != null
        val hasFooter = footerActions.isNotEmpty()
        headerView.visibility = if (hasHeader) VISIBLE else GONE
        headerView.setPaddingRelative(padding, padding, padding, if (hasBody) 0 else padding)
        titleView.text = title
        titleView.visibility = if (title.isNullOrEmpty()) GONE else VISIBLE
        titleView.setTextAppearance(resolved.titleTypeRole.textAppearance)
        titleView.setTextColor(tsColor(resolved.titleColor))
        titleView.includeFontPadding = false
        titleView.minHeight = lineHeightPx(resolved.titleTypeRole)
        subtitleView.text = subtitle
        subtitleView.visibility = if (subtitle.isNullOrEmpty()) GONE else VISIBLE
        subtitleView.setTextAppearance(resolved.subtitleTypeRole.textAppearance)
        subtitleView.setTextColor(tsColor(resolved.subtitleColor))
        subtitleView.minHeight = lineHeightPx(resolved.subtitleTypeRole)
        subtitleView.layoutParams = (subtitleView.layoutParams as LayoutParams).apply {
            topMargin = if (title.isNullOrEmpty()) 0 else dpToPx(resolved.subtitleSpacingDp)
        }
        accessoryContainer.visibility = if (accessoryView == null) GONE else VISIBLE
        accessoryContainer.layoutParams = (accessoryContainer.layoutParams as LayoutParams).apply {
            marginStart = dpToPx(resolved.headerSpacingDp)
        }
        bodyContainer.visibility = if (hasBody) VISIBLE else GONE
        bodyContainer.setPaddingRelative(padding, padding, padding, padding)
        bodyTextView.text = bodyText
        bodyTextView.visibility = if (bodyText.isNullOrEmpty()) GONE else VISIBLE
        bodyTextView.setTextAppearance(resolved.bodyTypeRole.textAppearance)
        bodyTextView.setTextColor(tsColor(resolved.bodyColor))
        bodyTextView.minHeight = lineHeightPx(resolved.bodyTypeRole)
        applyFooter(resolved, hasFooter)
        applyState()
    }

    private fun applyFooter(resolved: TSCardStyleSpec.Resolved, hasFooter: Boolean) {
        val stacked = TSCardStyleSpec.stacksFooter(resources.configuration.fontScale)
        dividerView.visibility = if (hasFooter) VISIBLE else GONE
        dividerView.layoutParams = (dividerView.layoutParams as LayoutParams).apply { height = dpToPx(resolved.dividerThicknessDp) }
        dividerView.setBackgroundColor(tsColor(resolved.divider))
        footerView.visibility = if (hasFooter) VISIBLE else GONE
        footerView.orientation = if (stacked) VERTICAL else HORIZONTAL
        footerView.gravity = if (stacked) Gravity.END else Gravity.END or Gravity.CENTER_VERTICAL
        val horizontal = dpToPx(resolved.footerHorizontalPaddingDp)
        val vertical = dpToPx(resolved.footerVerticalPaddingDp)
        footerView.setPaddingRelative(horizontal, vertical, horizontal, vertical)
        val spacing = dpToPx(resolved.footerSpacingDp)
        footerButtons.forEachIndexed { index, button ->
            button.size = resolved.footerButtonSize
            button.isEnabled = isEnabled
            button.isDimmedByContainer = !isEnabled
            button.layoutParams = (button.layoutParams as LayoutParams).apply {
                marginStart = if (!stacked && index > 0) spacing else 0
                topMargin = if (stacked && index > 0) spacing else 0
            }
        }
        accessoryView?.let { view ->
            view.isEnabled = isEnabled
            when (view) {
                is TSButtonView -> view.isDimmedByContainer = !isEnabled
                is TSIconButtonView -> view.isDimmedByContainer = !isEnabled
            }
        }
    }

    private fun applyState() {
        if (!ready) return
        val resolved = resolved
        chrome.update(
            fillColor = tsColor(resolved.fill),
            borderColor = tsColor(resolved.border),
            borderWidth = dpToPx(resolved.borderWidthDp).toFloat(),
            cornerRadius = dpToPx(resolved.cornerRadiusDp).toFloat(),
            ringColor = tsColor(TSColorRole.Focus),
            ringGapColor = tsColor(TSColorRole.SurfaceCard),
            ringWidth = focusRingWidth,
            showsRing = resolved.showsFocusRing,
        )
        elevation = resolved.shadow?.let { dpToPx(it.values.elevation).toFloat() } ?: 0f
        alpha = resolved.opacity
    }

    private fun makeAccessoryView(accessory: TSCardAccessory): View = when (accessory) {
        is TSCardAccessory.IconButton -> TSIconButtonView(context).apply {
            icon = accessory.icon
            label = accessory.label
            setOnClickListener { accessory.onClick() }
        }
    }

    private fun lineHeightPx(role: TSTypeRole): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, role.values.lineHeight, resources.displayMetrics).roundToInt()
}
