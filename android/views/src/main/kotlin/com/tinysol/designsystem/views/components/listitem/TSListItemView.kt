package com.tinysol.designsystem.views.components.listitem

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.Gravity
import android.view.TouchDelegate
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.core.widget.ImageViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.icon.TSIcons
import com.tinysol.designsystem.core.logic.TSListItemRole
import com.tinysol.designsystem.core.style.TSAvatarSize
import com.tinysol.designsystem.core.style.TSBadgeTone
import com.tinysol.designsystem.core.style.TSControlState
import com.tinysol.designsystem.core.style.TSListGroupPosition
import com.tinysol.designsystem.core.style.TSListItemLayout
import com.tinysol.designsystem.core.style.TSListItemStyleSpec
import com.tinysol.designsystem.core.style.TSTypeRole
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.avatar.TSAvatarView
import com.tinysol.designsystem.views.components.badge.TSBadgeView
import com.tinysol.designsystem.views.components.shared.TSViewSavedState
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.components.shared.restoreTSViewState
import com.tinysol.designsystem.views.components.switchcontrol.TSSwitchView
import com.tinysol.designsystem.views.theme.textAppearance
import com.tinysol.designsystem.views.theme.tsColor
import kotlin.math.roundToInt

public sealed interface TSListItemLeading {
    public data class Icon(public val icon: TSIcon) : TSListItemLeading

    public data class Avatar(public val name: String, public val image: Drawable? = null) : TSListItemLeading
}

public sealed interface TSListItemTrailing {
    public data class Text(public val text: CharSequence) : TSListItemTrailing

    public data class Badge(public val text: CharSequence, public val tone: TSBadgeTone = TSBadgeTone.Neutral) : TSListItemTrailing

    public data object Switch : TSListItemTrailing
}

public class TSListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    public fun interface OnCheckedChangeListener {
        public fun onCheckedChanged(view: TSListItemView, isChecked: Boolean)
    }

    private val contentView = LinearLayout(context)
    private val leadingContainer = FrameLayout(context)
    private val tileView = FrameLayout(context)
    private val tileIconView = AppCompatImageView(context)
    private val avatarView = TSAvatarView(context)
    private val textColumn = LinearLayout(context)
    private val titleView = MaterialTextView(context)
    private val subtitleView = MaterialTextView(context)
    private val trailingContainer = FrameLayout(context)
    private val trailingTextView = MaterialTextView(context)
    private val badgeView = TSBadgeView(context)
    internal val switchView = TSSwitchView(context)
    internal val chevronView = AppCompatImageView(context)
    private val chrome = MaterialShapeDrawable()
    private val tileChrome = MaterialShapeDrawable()
    private val switchTouchBounds = Rect()
    private var onCheckedChangeListener: OnCheckedChangeListener? = null
    private var ready = false

    public var title: CharSequence
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    public var subtitle: CharSequence? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var leading: TSListItemLeading? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var trailing: TSListItemTrailing? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var showsChevron: Boolean = false
        set(value) {
            field = value
            applyConfiguration()
        }

    public var isChecked: Boolean
        get() = switchView.isChecked
        set(value) {
            switchView.setChecked(value, animated = false)
            applyState()
        }

    internal var expansionState: Boolean? = null
        set(value) {
            field = value
            applyState()
        }

    public var groupPosition: TSListGroupPosition? = null
        set(value) {
            if (field == value) return
            field = value
            applyState()
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = chrome
        contentView.gravity = Gravity.CENTER_VERTICAL
        chevronView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        tileView.background = tileChrome
        tileView.addView(tileIconView, FrameLayout.LayoutParams(0, 0, Gravity.CENTER))
        leadingContainer.addView(tileView, FrameLayout.LayoutParams(0, 0))
        leadingContainer.addView(avatarView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        avatarView.size = TSAvatarSize.Md
        textColumn.orientation = VERTICAL
        listOf(titleView, subtitleView, trailingTextView).forEach {
            it.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            it.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }
        textColumn.addView(titleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        textColumn.addView(subtitleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        trailingContainer.addView(trailingTextView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL or Gravity.START))
        trailingContainer.addView(badgeView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL or Gravity.START))
        trailingContainer.addView(switchView, FrameLayout.LayoutParams(0, 0, Gravity.CENTER_VERTICAL or Gravity.START))
        switchView.isLabelHidden = true
        switchView.setOnCheckedChangeListener { _, checked ->
            applyState()
            onCheckedChangeListener?.onCheckedChanged(this, checked)
        }
        contentView.addView(leadingContainer, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        contentView.addView(textColumn, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        contentView.addView(trailingContainer, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(contentView, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        addView(chevronView, LayoutParams(0, 0))
        context.obtainStyledAttributes(attrs, R.styleable.TSListItemView, defStyleAttr, 0).apply {
            title = getText(R.styleable.TSListItemView_tsTitle) ?: ""
            subtitle = getText(R.styleable.TSListItemView_tsSubtitle)
            leading = getString(R.styleable.TSListItemView_tsLeadingIcon)?.let(TSIcons::fromDsName)?.let(TSListItemLeading::Icon)
                ?: getString(R.styleable.TSListItemView_tsName)?.let { TSListItemLeading.Avatar(it) }
            trailing = getText(R.styleable.TSListItemView_tsTrailingText)?.let(TSListItemTrailing::Text)
            showsChevron = getBoolean(R.styleable.TSListItemView_tsShowsChevron, false)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    public fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        onCheckedChangeListener = listener
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
        if (listener == null) isClickable = false
        applyConfiguration()
    }

    override fun onSaveInstanceState(): Parcelable =
        TSViewSavedState(super.onSaveInstanceState(), bundleOf(CHECKED_KEY to isChecked))

    override fun onRestoreInstanceState(state: Parcelable?) {
        restoreTSViewState(state, { super.onRestoreInstanceState(it) }) { isChecked = it.getBoolean(CHECKED_KEY) }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (ready) applyState()
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed && role.isPressable)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (ready) applyState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        applyConfiguration()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        updateSwitchTouchDelegate()
    }

    internal val role: TSListItemRole
        get() = TSListItemRole.make(
            hasAction = hasOnClickListeners(),
            showsChevron = showsChevron,
            hasToggle = trailing == TSListItemTrailing.Switch,
        )

    internal val layout: TSListItemLayout
        get() = TSListItemLayout.make(resources.configuration.fontScale)

    internal val currentState: TSControlState
        get() = TSControlState.make(pressed = isPressed, enabled = isEnabled, focused = isFocused)

    internal val resolved: TSListItemStyleSpec.Resolved
        get() = TSListItemStyleSpec.resolve(
            TSListItemStyleSpec.Configuration(
                hasSubtitle = !subtitle.isNullOrEmpty(),
                isInteractive = role.isPressable,
                layout = layout,
            ),
            currentState,
        )

    internal val switchTouchArea: Rect
        get() = Rect(switchTouchBounds)

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        val role = role
        val stacked = resolved.isStacked
        isClickable = role.isPressable
        isFocusable = role.isPressable
        if (!role.isPressable) isPressed = false
        val horizontal = dpToPx(resolved.horizontalPaddingDp)
        val vertical = dpToPx(resolved.verticalPaddingDp)
        setPaddingRelative(horizontal, vertical, horizontal, vertical)
        minimumHeight = dpToPx(resolved.minHeightDp)
        contentView.orientation = if (stacked) VERTICAL else HORIZONTAL
        contentView.gravity = if (stacked) Gravity.START else Gravity.CENTER_VERTICAL
        applyText(resolved)
        applyLeading()
        applyTrailing(resolved)
        applySpacing(resolved, stacked)
        val chevronSize = dpToPx(resolved.chevronSize.dp)
        chevronView.setImageResource(resolved.chevron.drawableRes)
        chevronView.visibility = if (showsChevron) VISIBLE else GONE
        chevronView.layoutParams = (chevronView.layoutParams as LayoutParams).apply {
            width = chevronSize
            height = chevronSize
            marginStart = dpToPx(if (trailing != null && !stacked) resolved.trailingSpacingDp else resolved.spacingDp)
        }
        ImageViewCompat.setImageTintList(chevronView, ColorStateList.valueOf(tsColor(resolved.chevronColor)))
        applyState()
    }

    private fun applyText(resolved: TSListItemStyleSpec.Resolved) {
        titleView.setTextAppearance(resolved.titleTypeRole.textAppearance)
        titleView.setTextColor(tsColor(resolved.titleColor))
        titleView.applyLineLimit(resolved.titleMaxLines)
        titleView.minHeight = lineHeightPx(resolved.titleTypeRole)
        subtitleView.text = subtitle
        subtitleView.setTextAppearance(resolved.subtitleTypeRole.textAppearance)
        subtitleView.setTextColor(tsColor(resolved.subtitleColor))
        subtitleView.applyLineLimit(resolved.subtitleMaxLines)
        subtitleView.minHeight = lineHeightPx(resolved.subtitleTypeRole)
        subtitleView.visibility = if (resolved.showsSubtitle) VISIBLE else GONE
        trailingTextView.setTextAppearance(resolved.trailingTypeRole.textAppearance)
        trailingTextView.setTextColor(tsColor(resolved.trailingColor))
    }

    private fun applyLeading() {
        val tile = TSListItemStyleSpec.tile
        val leading = leading
        leadingContainer.visibility = if (leading == null) GONE else VISIBLE
        tileView.visibility = if (leading is TSListItemLeading.Icon) VISIBLE else GONE
        avatarView.visibility = if (leading is TSListItemLeading.Avatar) VISIBLE else GONE
        val tileSize = dpToPx(tile.sizeDp)
        tileView.layoutParams = (tileView.layoutParams as FrameLayout.LayoutParams).apply {
            width = tileSize
            height = tileSize
        }
        val iconSize = dpToPx(tile.iconSize.dp)
        tileIconView.layoutParams = (tileIconView.layoutParams as FrameLayout.LayoutParams).apply {
            width = iconSize
            height = iconSize
        }
        tileChrome.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(dpToPx(tile.cornerRadiusDp).toFloat()).build()
        tileChrome.fillColor = ColorStateList.valueOf(tsColor(tile.fill))
        ImageViewCompat.setImageTintList(tileIconView, ColorStateList.valueOf(tsColor(tile.foreground)))
        when (leading) {
            is TSListItemLeading.Icon -> tileIconView.setImageResource(leading.icon.drawableRes)
            is TSListItemLeading.Avatar -> {
                avatarView.name = leading.name
                avatarView.image = leading.image
            }
            null -> Unit
        }
    }

    private fun applyTrailing(resolved: TSListItemStyleSpec.Resolved) {
        val trailing = trailing
        trailingContainer.visibility = if (trailing == null) GONE else VISIBLE
        trailingTextView.visibility = if (trailing is TSListItemTrailing.Text) VISIBLE else GONE
        badgeView.visibility = if (trailing is TSListItemTrailing.Badge) VISIBLE else GONE
        switchView.visibility = if (trailing == TSListItemTrailing.Switch) VISIBLE else GONE
        when (trailing) {
            is TSListItemTrailing.Text -> trailingTextView.text = trailing.text
            is TSListItemTrailing.Badge -> {
                badgeView.text = trailing.text
                badgeView.tone = trailing.tone
            }
            TSListItemTrailing.Switch, null -> Unit
        }
        val switchResolved = switchView.resolved
        switchView.layoutParams = (switchView.layoutParams as FrameLayout.LayoutParams).apply {
            width = dpToPx(switchResolved.trackWidthDp)
            height = dpToPx(switchResolved.trackHeightDp)
        }
        trailingTextView.maxLines = resolved.titleMaxLines ?: Int.MAX_VALUE
    }

    private fun applySpacing(resolved: TSListItemStyleSpec.Resolved, stacked: Boolean) {
        val spacing = dpToPx(resolved.spacingDp)
        val stackedSpacing = dpToPx(resolved.stackedSpacingDp)
        leadingContainer.layoutParams = (leadingContainer.layoutParams as LayoutParams).apply {
            marginEnd = if (stacked) 0 else spacing
            bottomMargin = if (stacked) stackedSpacing else 0
        }
        textColumn.layoutParams = (textColumn.layoutParams as LayoutParams).apply {
            width = if (stacked) LayoutParams.MATCH_PARENT else 0
            weight = if (stacked) 0f else 1f
        }
        trailingContainer.layoutParams = (trailingContainer.layoutParams as LayoutParams).apply {
            marginStart = if (stacked) 0 else spacing
            topMargin = if (stacked) stackedSpacing else 0
        }
    }

    private fun applyState() {
        if (!ready) return
        val resolved = resolved
        val radius = dpToPx(TSListItemStyleSpec.group.cornerRadiusDp).toFloat()
        val position = groupPosition
        chrome.shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setTopLeftCornerSize(if (position?.roundsTop == true) radius else 0f)
            .setTopRightCornerSize(if (position?.roundsTop == true) radius else 0f)
            .setBottomLeftCornerSize(if (position?.roundsBottom == true) radius else 0f)
            .setBottomRightCornerSize(if (position?.roundsBottom == true) radius else 0f)
            .build()
        chrome.fillColor = ColorStateList.valueOf(resolved.background?.let(::tsColor) ?: 0)
        val dimmed = listOf(leadingContainer, textColumn, chevronView)
        dimmed.forEach { it.alpha = resolved.opacity }
        trailingContainer.alpha = if (trailing == TSListItemTrailing.Switch) 1f else resolved.opacity
        switchView.isEnabled = isEnabled
    }

    private fun updateSwitchTouchDelegate() {
        if (switchView.visibility != VISIBLE || trailingContainer.visibility != VISIBLE) {
            switchTouchBounds.setEmpty()
            touchDelegate = null
            return
        }
        val bounds = Rect(0, 0, switchView.width, switchView.height)
        offsetDescendantRectToMyCoords(switchView, bounds)
        val minTouchTarget = dpToPx(TSListItemStyleSpec.MIN_TOUCH_TARGET_DP)
        val growX = ((minTouchTarget - bounds.width()) / 2).coerceAtLeast(0)
        val growY = ((minTouchTarget - bounds.height()) / 2).coerceAtLeast(0)
        bounds.inset(-growX, -growY)
        bounds.set(maxOf(bounds.left, 0), maxOf(bounds.top, 0), minOf(bounds.right, width), minOf(bounds.bottom, height))
        switchTouchBounds.set(bounds)
        touchDelegate = TouchDelegate(bounds, switchView)
    }

    private fun lineHeightPx(role: TSTypeRole): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, role.values.lineHeight, resources.displayMetrics).roundToInt()

    private fun MaterialTextView.applyLineLimit(maxLines: Int?) {
        if (maxLines == null) {
            this.maxLines = Int.MAX_VALUE
            isSingleLine = false
            ellipsize = null
        } else {
            this.maxLines = maxLines
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    private companion object {
        private const val CHECKED_KEY = "ts.checked"
    }
}
