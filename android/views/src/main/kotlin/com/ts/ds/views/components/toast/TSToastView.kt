package com.ts.ds.views.components.toast

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.logic.TSToastTone
import com.ts.ds.core.style.TSToastStyleSpec
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.core.style.resolve
import com.ts.ds.views.R
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.foundation.applyPressedAlpha
import com.ts.ds.views.foundation.ensureMinTouchTarget
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor
import kotlin.math.roundToInt

public class TSToastView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    internal val messageView = LinearLayout(context)
    internal val iconView = AppCompatImageView(context)
    internal val textView = MaterialTextView(context)
    internal val actionView = MaterialTextView(context)
    private val chrome = GradientDrawable()
    private var ready = false

    public var message: CharSequence = ""
        set(value) {
            field = value
            applyConfiguration()
        }

    public var tone: TSToastTone = TSToastTone.Neutral
        set(value) {
            field = value
            applyConfiguration()
        }

    public var actionLabel: CharSequence? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var onAction: (() -> Unit)? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = chrome
        messageView.orientation = HORIZONTAL
        messageView.gravity = Gravity.CENTER_VERTICAL
        textView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        textView.gravity = Gravity.START
        iconView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        messageView.addView(iconView, LayoutParams(0, 0))
        messageView.addView(textView, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        addView(messageView, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        addView(actionView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        actionView.isClickable = true
        actionView.isFocusable = true
        actionView.setOnClickListener { onAction?.invoke() }
        context.obtainStyledAttributes(attrs, R.styleable.TSToastView, defStyleAttr, 0).apply {
            tone = TSToastTone.entries[getInt(R.styleable.TSToastView_tsToastTone, TSToastTone.Neutral.ordinal)]
            message = getText(R.styleable.TSToastView_tsMessage) ?: ""
            actionLabel = getText(R.styleable.TSToastView_tsActionLabel)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean = true

    internal val resolved: TSToastStyleSpec.Resolved
        get() = TSToastStyleSpec.resolve(
            TSToastStyleSpec.Configuration(tone = tone, hasAction = actionLabel != null && onAction != null),
        )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = dpToPx(resolved.maxWidthDp)
        val spec = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)
            else -> MeasureSpec.makeMeasureSpec(minOf(maxWidth, MeasureSpec.getSize(widthMeasureSpec)), MeasureSpec.getMode(widthMeasureSpec))
        }
        super.onMeasure(spec, heightMeasureSpec)
    }

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        val horizontal = dpToPx(resolved.horizontalPaddingDp)
        val vertical = dpToPx(resolved.verticalPaddingDp)
        val spacing = dpToPx(resolved.spacingDp)
        setPaddingRelative(horizontal, vertical, horizontal, vertical)
        chrome.cornerRadius = dpToPx(resolved.cornerRadiusDp).toFloat()
        chrome.setColor(tsColor(resolved.fill))
        elevation = dpToPx(resolved.shadow.values.elevation).toFloat()
        val icon = resolved.icon
        val iconColor = resolved.iconColor
        val iconSize = dpToPx(resolved.iconSize.dp)
        iconView.visibility = if (icon != null && iconColor != null) VISIBLE else GONE
        iconView.layoutParams = (iconView.layoutParams as LayoutParams).apply {
            width = iconSize
            height = iconSize
            marginEnd = spacing
        }
        if (icon != null && iconColor != null) {
            iconView.setImageResource(icon.drawableRes)
            ImageViewCompat.setImageTintList(iconView, ColorStateList.valueOf(tsColor(iconColor)))
        }
        textView.text = message
        textView.setTextAppearance(resolved.messageTypeRole.textAppearance)
        textView.setTextColor(tsColor(resolved.messageColor))
        textView.minHeight = lineHeightPx(resolved.messageTypeRole)
        actionView.visibility = if (resolved.showsAction) VISIBLE else GONE
        actionView.text = actionLabel
        actionView.setTextAppearance(resolved.actionTypeRole.textAppearance)
        actionView.setTextAppearance(R.style.TextAppearance_TS_Component_ToastAction)
        actionView.setTextColor(tsColor(resolved.actionColor))
        actionView.minHeight = lineHeightPx(resolved.actionTypeRole)
        actionView.paintFlags = if (resolved.actionIsUnderlined) {
            actionView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else {
            actionView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
        actionView.setPaddingRelative(dpToPx(resolved.actionHorizontalPaddingDp), 0, 0, 0)
        actionView.layoutParams = (actionView.layoutParams as LayoutParams).apply { marginStart = spacing }
        actionView.applyPressedAlpha(resolved.actionPressedOpacity)
        actionView.ensureMinTouchTarget(dpToPx(resolved.minTouchTargetDp))
    }

    private fun lineHeightPx(role: TSTypeRole): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, role.values.lineHeight, resources.displayMetrics).roundToInt()
}
