package com.tinysol.designsystem.views.components.alert

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.icon.TSIcons
import com.tinysol.designsystem.core.style.TSAlertStyleSpec
import com.tinysol.designsystem.core.style.TSAlertTone
import com.tinysol.designsystem.core.style.TSTypeRole
import com.tinysol.designsystem.core.style.resolve
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.foundation.applyPressedAlpha
import com.tinysol.designsystem.views.foundation.ensureMinTouchTarget
import com.tinysol.designsystem.views.theme.textAppearance
import com.tinysol.designsystem.views.theme.tsColor
import kotlin.math.roundToInt

public class TSAlertView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    internal val messageView = LinearLayout(context)
    internal val iconView = AppCompatImageView(context)
    private val textColumn = LinearLayout(context)
    private val titleView = MaterialTextView(context)
    private val bodyView = MaterialTextView(context)
    private val actionContainer = FrameLayout(context)
    internal val dismissButton = AppCompatImageView(context)
    private val chrome = GradientDrawable()
    private var ready = false

    public var tone: TSAlertTone = TSAlertTone.Info
        set(value) {
            field = value
            applyConfiguration()
        }

    public var title: CharSequence? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var message: CharSequence = ""
        set(value) {
            field = value
            applyConfiguration()
        }

    public var icon: TSIcon? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var actionView: View? = null
        set(value) {
            if (field === value) return
            actionContainer.removeAllViews()
            field = value
            value?.let { actionContainer.addView(it, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)) }
            applyConfiguration()
        }

    public var onDismiss: (() -> Unit)? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.TOP
        background = chrome
        messageView.orientation = HORIZONTAL
        messageView.gravity = Gravity.TOP
        textColumn.orientation = VERTICAL
        listOf(titleView, bodyView).forEach {
            it.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            it.gravity = Gravity.START
        }
        iconView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        textColumn.addView(titleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        textColumn.addView(bodyView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        messageView.addView(iconView, LayoutParams(0, 0))
        messageView.addView(textColumn, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        addView(messageView, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        addView(actionContainer, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(dismissButton, LayoutParams(0, 0))
        dismissButton.isClickable = true
        dismissButton.isFocusable = true
        dismissButton.setOnClickListener { onDismiss?.invoke() }
        dismissButton.applyPressedAlpha(TSAlertStyleSpec.resolve(TSAlertStyleSpec.Configuration()).dismissPressedOpacity)
        context.obtainStyledAttributes(attrs, R.styleable.TSAlertView, defStyleAttr, 0).apply {
            tone = TSAlertTone.entries[getInt(R.styleable.TSAlertView_tsAlertTone, TSAlertTone.Info.ordinal)]
            title = getText(R.styleable.TSAlertView_tsTitle)
            message = getText(R.styleable.TSAlertView_tsMessage) ?: ""
            icon = getString(R.styleable.TSAlertView_tsIcon)?.let(TSIcons::fromDsName)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    internal val resolved: TSAlertStyleSpec.Resolved
        get() = TSAlertStyleSpec.resolve(
            TSAlertStyleSpec.Configuration(tone = tone, hasTitle = !title.isNullOrEmpty(), isDismissible = onDismiss != null),
        )

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        val horizontal = dpToPx(resolved.contentHorizontalInsetDp)
        val vertical = dpToPx(resolved.contentVerticalInsetDp)
        val spacing = dpToPx(resolved.spacingDp)
        setPaddingRelative(horizontal, vertical, horizontal, vertical)
        chrome.cornerRadius = dpToPx(resolved.cornerRadiusDp).toFloat()
        chrome.setColor(tsColor(resolved.fill))
        val iconSize = dpToPx(resolved.iconSize.dp)
        iconView.layoutParams = (iconView.layoutParams as LayoutParams).apply {
            width = iconSize
            height = iconSize
            marginEnd = spacing
        }
        iconView.setImageResource((icon ?: resolved.icon).drawableRes)
        ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(tsColor(resolved.iconColor)))
        titleView.text = title
        titleView.visibility = if (resolved.showsTitle) VISIBLE else GONE
        titleView.setTextAppearance(resolved.titleTypeRole.textAppearance)
        titleView.setTextAppearance(R.style.TextAppearance_TS_Component_AlertTitle)
        titleView.setTextColor(tsColor(resolved.titleColor))
        titleView.minHeight = lineHeightPx(resolved.titleTypeRole)
        bodyView.text = message
        bodyView.setTextAppearance(resolved.messageTypeRole.textAppearance)
        bodyView.setTextColor(tsColor(resolved.messageColor))
        bodyView.minHeight = lineHeightPx(resolved.messageTypeRole)
        actionContainer.visibility = if (actionView == null) GONE else VISIBLE
        actionContainer.layoutParams = (actionContainer.layoutParams as LayoutParams).apply { marginStart = spacing }
        val dismissSize = dpToPx(resolved.dismissIconSize.dp)
        dismissButton.visibility = if (resolved.showsDismiss) VISIBLE else GONE
        dismissButton.layoutParams = (dismissButton.layoutParams as LayoutParams).apply {
            width = dismissSize
            height = dismissSize
            marginStart = spacing
        }
        dismissButton.setImageResource(resolved.dismissIcon.drawableRes)
        ImageViewCompat.setImageTintList(dismissButton, ColorStateList.valueOf(tsColor(resolved.dismissColor)))
        dismissButton.ensureMinTouchTarget(dpToPx(resolved.minTouchTargetDp))
    }

    private fun lineHeightPx(role: TSTypeRole): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, role.values.lineHeight, resources.displayMetrics).roundToInt()
}
