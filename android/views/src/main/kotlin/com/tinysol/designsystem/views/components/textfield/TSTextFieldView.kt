package com.tinysol.designsystem.views.components.textfield

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.InsetDrawable
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.icon.TSIcons
import com.tinysol.designsystem.core.input.TSTextInputTraits
import com.tinysol.designsystem.core.style.TSControlState
import com.tinysol.designsystem.core.style.TSTextFieldContentStyle
import com.tinysol.designsystem.core.style.TSTextFieldStyleSpec
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.shared.TSViewSavedState
import com.tinysol.designsystem.views.components.shared.dpToPx
import com.tinysol.designsystem.views.components.shared.restoreTSViewState
import com.tinysol.designsystem.views.foundation.TSOffsetTouchDelegate
import com.tinysol.designsystem.views.foundation.TSTouchDelegateGroup
import com.tinysol.designsystem.views.foundation.expandedTouchBounds
import com.tinysol.designsystem.views.theme.textAppearance
import com.tinysol.designsystem.views.theme.tsColor

public class TSTextFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private val labelView = MaterialTextView(context)
    private val box = LinearLayout(context)
    private val boxBackground = MaterialShapeDrawable()
    private val leadingView = AppCompatImageView(context)
    private val input = TSTextFieldInput(context)
    private val trailingContainer = FrameLayout(context)
    private val toggleView = AppCompatImageButton(context)
    private val toggleFill = MaterialShapeDrawable()
    private val messageRow = LinearLayout(context)
    private val messageIcon = AppCompatImageView(context)
    private val messageView = MaterialTextView(context)
    private val minTouchTarget = resources.getDimensionPixelSize(R.dimen.ts_min_touch_target)
    private var userEnabled = true
    private var ready = false

    public val editText: EditText
        get() = input

    public var text: CharSequence
        get() = input.text?.toString().orEmpty()
        set(value) {
            input.setText(value)
        }

    public var label: CharSequence
        get() = labelView.text
        set(value) {
            labelView.text = value
        }

    public var placeholder: CharSequence?
        get() = input.hint
        set(value) {
            input.hint = value
        }

    public var hint: CharSequence? = null
        set(value) {
            field = value
            applyMessage()
        }

    public var errorMessage: CharSequence? = null
        set(value) {
            field = value
            applyMessage()
            applyState()
        }

    public var leadingIcon: TSIcon? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var trailingView: View? = null
        set(value) {
            field = value
            trailingContainer.removeAllViews()
            if (value != null) trailingContainer.addView(value, FrameLayout.LayoutParams(WRAP, WRAP, Gravity.CENTER))
            trailingContainer.visibility = if (value == null) GONE else VISIBLE
        }

    public var isSecure: Boolean = false
        set(value) {
            field = value
            isPasswordRevealed = false
            applyConfiguration()
        }

    public var isMultiline: Boolean = false
        set(value) {
            field = value
            applyConfiguration()
        }

    public var isReadOnly: Boolean = false
        set(value) {
            field = value
            applyConfiguration()
        }

    public var inputTraits: TSTextInputTraits = TSTextInputTraits.Default
        set(value) {
            field = value
            applyConfiguration()
        }

    public var contentStyle: TSTextFieldContentStyle = TSTextFieldContentStyle.Default
        set(value) {
            field = value
            applyConfiguration()
        }

    internal var isPasswordRevealed: Boolean = false
        private set

    init {
        orientation = VERTICAL
        val initial = TSTextFieldStyleSpec.resolve(TSTextFieldStyleSpec.Configuration(), TSControlState.Default)
        val toggle = TSTextFieldStyleSpec.passwordToggle(revealed = false)
        val spacing = dpToPx(initial.spacingDp)
        labelView.textAlignment = TEXT_ALIGNMENT_VIEW_START
        addView(labelView, LayoutParams(MATCH, WRAP).apply { bottomMargin = dpToPx(initial.labelSpacingDp) })
        box.orientation = HORIZONTAL
        box.gravity = Gravity.CENTER_VERTICAL
        box.background = boxBackground
        box.clipToPadding = false
        leadingView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        val leadingSize = dpToPx(initial.leadingIconSizeDp)
        box.addView(leadingView, LayoutParams(leadingSize, leadingSize).apply { marginEnd = spacing })
        input.background = null
        input.textAlignment = TEXT_ALIGNMENT_VIEW_START
        input.onFocusChange = { applyState() }
        box.addView(input, LayoutParams(0, WRAP, 1f))
        trailingContainer.visibility = GONE
        box.addView(trailingContainer, LayoutParams(WRAP, WRAP).apply { marginStart = spacing })
        val toggleVisual = dpToPx(toggle.sizeDp)
        val toggleHeight = dpToPx(initial.minHeightDp)
        val insetX = (minTouchTarget - toggleVisual) / 2
        val insetY = (toggleHeight - toggleVisual) / 2
        toggleView.background = InsetDrawable(toggleFill, insetX, insetY, insetX, insetY)
        toggleView.scaleType = ImageView.ScaleType.CENTER
        toggleView.setPadding(0, 0, 0, 0)
        toggleView.setImageResource(toggle.icon.drawableRes)
        toggleView.setOnClickListener { togglePasswordVisibility() }
        box.addView(toggleView, LayoutParams(minTouchTarget, toggleHeight).apply { marginStart = spacing - insetX; marginEnd = -insetX })
        addView(box, LayoutParams(MATCH, WRAP))
        messageRow.orientation = HORIZONTAL
        messageRow.gravity = Gravity.CENTER_VERTICAL
        messageIcon.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        messageView.textAlignment = TEXT_ALIGNMENT_VIEW_START
        messageIcon.setImageResource(TSIcon.Alert.drawableRes)
        val messageIconSize = dpToPx(initial.messageIconSizeDp)
        messageRow.addView(messageIcon, LayoutParams(messageIconSize, messageIconSize).apply { marginEnd = dpToPx(initial.messageSpacingDp) })
        messageRow.addView(messageView, LayoutParams(0, WRAP, 1f))
        addView(messageRow, LayoutParams(MATCH, WRAP).apply { topMargin = dpToPx(initial.messageSpacingDp) })
        context.obtainStyledAttributes(attrs, R.styleable.TSTextFieldView, defStyleAttr, 0).apply {
            label = getText(R.styleable.TSTextFieldView_tsLabel) ?: ""
            placeholder = getText(R.styleable.TSTextFieldView_tsPlaceholder)
            hint = getText(R.styleable.TSTextFieldView_tsHint)
            errorMessage = getText(R.styleable.TSTextFieldView_tsError)
            leadingIcon = getString(R.styleable.TSTextFieldView_tsLeadingIcon)?.let(TSIcons::fromDsName)
            isSecure = getBoolean(R.styleable.TSTextFieldView_tsSecure, false)
            isMultiline = getBoolean(R.styleable.TSTextFieldView_tsMultiline, false)
            isReadOnly = getBoolean(R.styleable.TSTextFieldView_tsReadOnly, false)
            inputTraits = traitsFor(getInt(R.styleable.TSTextFieldView_tsInputTraits, 0))
            contentStyle = TSTextFieldContentStyle.entries[getInt(R.styleable.TSTextFieldView_tsContentStyle, 0)]
            getText(R.styleable.TSTextFieldView_android_text)?.let(input::setText)
            isEnabled = getBoolean(R.styleable.TSTextFieldView_android_enabled, isEnabled)
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        updateTouchTargets()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        userEnabled = enabled
        input.isEnabled = enabled
        toggleView.isEnabled = enabled
        trailingContainer.isEnabled = enabled
        if (ready) applyState()
    }

    override fun onSaveInstanceState(): Parcelable =
        TSViewSavedState(
            super.onSaveInstanceState(),
            bundleOf(
                TEXT_KEY to input.text?.toString(),
                SELECTION_START_KEY to input.selectionStart,
                SELECTION_END_KEY to input.selectionEnd,
                REVEALED_KEY to isPasswordRevealed,
            ),
        )

    override fun onRestoreInstanceState(state: Parcelable?) {
        restoreTSViewState(state, { super.onRestoreInstanceState(it) }) { values ->
            input.setText(values.getString(TEXT_KEY))
            isPasswordRevealed = values.getBoolean(REVEALED_KEY)
            applyConfiguration()
            val length = input.text?.length ?: 0
            val start = values.getInt(SELECTION_START_KEY, -1)
            val end = values.getInt(SELECTION_END_KEY, -1)
            if (start in 0..length && end in 0..length) input.setSelection(start, end)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    internal val currentState: TSControlState
        get() = TSTextFieldStyleSpec.state(
            enabled = userEnabled,
            focused = input.isFocused,
            error = !errorMessage.isNullOrBlank(),
        )

    internal val resolved: TSTextFieldStyleSpec.Resolved
        get() = TSTextFieldStyleSpec.resolve(TSTextFieldStyleSpec.Configuration(isReadOnly, contentStyle), currentState)

    internal val passwordToggle: View
        get() = toggleView

    internal val message: View
        get() = messageRow

    internal val messageText: CharSequence?
        get() = messageView.text.takeIf { messageRow.isVisible }

    internal val errorIcon: View
        get() = messageIcon

    internal val fieldBox: View
        get() = box

    private val secureActive: Boolean
        get() = isSecure && !isMultiline

    private fun togglePasswordVisibility() {
        isPasswordRevealed = !isPasswordRevealed
        applyConfiguration()
    }

    private fun updateTouchTargets() {
        val group = touchDelegate as? TSTouchDelegateGroup ?: TSTouchDelegateGroup(this).also { touchDelegate = it }
        val inputBounds = expandedTouchBounds(Rect().also(input::getHitRect).apply { offset(box.left, box.top) }, minTouchTarget)
        group.put(input, inputBounds, TSOffsetTouchDelegate(inputBounds, input) { box.left + input.left to box.top + input.top })
        if (!toggleView.isVisible) {
            group.remove(toggleView)
            return
        }
        val hitRect = Rect().also(toggleView::getHitRect).apply { offset(box.left, box.top) }
        group.put(toggleView, expandedTouchBounds(hitRect, minTouchTarget))
    }

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        labelView.setTextAppearance(resolved.labelTypeRole.textAppearance)
        labelView.typeface = Typeface.create(MEDIUM_FAMILY, Typeface.NORMAL)
        labelView.setTextColor(tsColor(resolved.labelColor))
        val horizontal = dpToPx(resolved.horizontalPaddingDp)
        box.setPaddingRelative(horizontal, 0, horizontal, 0)
        box.minimumHeight = dpToPx(resolved.minHeightDp)
        val currentIcon = leadingIcon
        if (currentIcon != null) {
            leadingView.setImageResource(currentIcon.drawableRes)
            leadingView.visibility = VISIBLE
        } else {
            leadingView.setImageDrawable(null)
            leadingView.visibility = GONE
        }
        ImageViewCompat.setImageTintList(leadingView, ColorStateList.valueOf(tsColor(resolved.leadingIconColor)))
        applyInput(resolved)
        toggleView.visibility = if (secureActive && !isReadOnly) VISIBLE else GONE
        applyMessage()
        applyState()
    }

    private fun applyInput(resolved: TSTextFieldStyleSpec.Resolved) {
        val selectionStart = input.selectionStart
        val selectionEnd = input.selectionEnd
        input.inputType = inputTraits.inputType(secure = secureActive, revealed = isPasswordRevealed && !isReadOnly, multiline = isMultiline)
        input.imeOptions = inputTraits.imeOptions()
        if (inputTraits.autofillHints.isNotEmpty()) {
            input.setAutofillHints(*inputTraits.autofillHints.toTypedArray())
            input.importantForAutofill = IMPORTANT_FOR_AUTOFILL_YES
        } else {
            input.setAutofillHints()
            input.importantForAutofill = IMPORTANT_FOR_AUTOFILL_AUTO
        }
        if (isReadOnly) {
            input.keyListener = null
            input.isCursorVisible = false
            input.showSoftInputOnFocus = false
        } else {
            input.isCursorVisible = true
            input.showSoftInputOnFocus = true
        }
        input.setTextAppearance(resolved.inputTypeRole.textAppearance)
        input.typeface = if (resolved.inputTypeRole.values.monospace) Typeface.MONOSPACE else Typeface.DEFAULT
        TextViewCompat.setLineHeight(input, sp(resolved.inputTypeRole.values.lineHeight))
        input.setTextColor(tsColor(resolved.inputColor))
        input.setHintTextColor(tsColor(resolved.placeholderColor))
        val vertical = dpToPx(resolved.verticalPaddingDp)
        input.setPaddingRelative(0, vertical, 0, vertical)
        val lineHeight = sp(resolved.inputTypeRole.values.lineHeight)
        if (isMultiline) {
            input.maxLines = Int.MAX_VALUE
            input.minHeight = TSTextFieldStyleSpec.MINIMUM_LINES * lineHeight + vertical * 2
            input.maxHeight = TSTextFieldStyleSpec.MAXIMUM_LINES * lineHeight + vertical * 2
            input.isVerticalScrollBarEnabled = true
            input.gravity = Gravity.TOP or Gravity.START
        } else {
            input.minHeight = 0
            input.maxHeight = Int.MAX_VALUE
            input.maxLines = 1
            input.isVerticalScrollBarEnabled = false
            input.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }
        val length = input.text?.length ?: 0
        if (selectionStart >= 0 && selectionEnd >= 0) {
            input.setSelection(selectionStart.coerceAtMost(length), selectionEnd.coerceAtMost(length))
        }
    }

    private fun applyMessage() {
        if (!ready) return
        val resolved = resolved
        val error = errorMessage?.toString()
        val text = if (!error.isNullOrBlank()) error else hint?.toString()?.takeIf { it.isNotBlank() }
        val color = tsColor(resolved.messageColor)
        messageView.setTextAppearance(resolved.messageTypeRole.textAppearance)
        messageView.setTextColor(color)
        messageView.text = text
        messageIcon.visibility = if (resolved.showsErrorIcon) VISIBLE else GONE
        ImageViewCompat.setImageTintList(messageIcon, ColorStateList.valueOf(color))
        messageRow.visibility = if (text == null) GONE else VISIBLE
    }

    private fun applyState() {
        if (!ready) return
        val resolved = resolved
        boxBackground.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(dpToPx(resolved.cornerRadiusDp).toFloat()).build()
        boxBackground.fillColor = ColorStateList.valueOf(tsColor(resolved.fill))
        boxBackground.setStroke(dpToPx(resolved.borderWidthDp).toFloat(), tsColor(resolved.border))
        box.elevation = resolved.shadow?.let { dpToPx(it.values.elevation).toFloat() } ?: 0f
        box.alpha = resolved.opacity
        val toggle = TSTextFieldStyleSpec.passwordToggle(isPasswordRevealed)
        toggleFill.shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(dpToPx(toggle.cornerRadiusDp).toFloat()).build()
        toggleFill.fillColor = ColorStateList.valueOf(toggle.fill?.let(::tsColor) ?: 0)
        ImageViewCompat.setImageTintList(toggleView, ColorStateList.valueOf(tsColor(toggle.iconColor)))
        toggleView.isSelected = toggle.selected
    }

    private fun sp(value: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics).toInt()

    private fun traitsFor(value: Int): TSTextInputTraits = TRAITS[value]

    private class TSTextFieldInput(context: Context) : AppCompatEditText(context) {
        var onFocusChange: (() -> Unit)? = null

        override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect)
            onFocusChange?.invoke()
        }
    }

    private companion object {
        const val MEDIUM_FAMILY = "sans-serif-medium"
        const val TEXT_KEY = "ts.text"
        const val SELECTION_START_KEY = "ts.selectionStart"
        const val SELECTION_END_KEY = "ts.selectionEnd"
        const val REVEALED_KEY = "ts.passwordRevealed"
        const val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
        const val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
        val TRAITS = listOf(
            TSTextInputTraits.Default,
            TSTextInputTraits.Name,
            TSTextInputTraits.Email,
            TSTextInputTraits.Username,
            TSTextInputTraits.Password,
            TSTextInputTraits.NewPassword,
            TSTextInputTraits.OneTimeCode,
            TSTextInputTraits.Phone,
            TSTextInputTraits.Number,
            TSTextInputTraits.Decimal,
            TSTextInputTraits.Url,
            TSTextInputTraits.Search,
            TSTextInputTraits.Code,
        )
    }
}
