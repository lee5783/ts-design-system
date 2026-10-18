package com.ts.ds.views.components.textfield

import android.graphics.Typeface
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.KeyCharacterMap
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.input.TSImeAction
import com.ts.ds.core.input.TSTextInputTraits
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTextFieldContentStyle
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.views.R
import com.ts.ds.views.components.hostInActivity
import com.ts.ds.views.components.tapCenter
import com.ts.ds.views.foundation.TSTouchDelegateGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSTextFieldViewTest {
    private fun field(configure: TSTextFieldView.() -> Unit = {}): TSTextFieldView = hostInActivity { context ->
        TSTextFieldView(context).apply {
            label = LABEL
            hint = HINT
            configure()
        }
    }

    private fun View.nodeInfo(): AccessibilityNodeInfoCompat =
        AccessibilityNodeInfoCompat.obtain().also { ViewCompat.onInitializeAccessibilityNodeInfo(this, it) }

    private fun TSTextFieldView.type(value: String) {
        editText.requestFocus()
        KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).getEvents(value.toCharArray()).forEach(editText::dispatchKeyEvent)
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun TSTextFieldView.layOut() {
        measure(View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
        layout(0, 0, measuredWidth, measuredHeight)
    }

    @Test
    fun typingEditsTheTextAndNotifiesWatchers() {
        val view = field()
        var changes = 0
        view.editText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    changes++
                }
                override fun afterTextChanged(s: Editable?) = Unit
            },
        )

        view.type("ada")

        assertEquals("ada", view.text)
        assertEquals(3, changes)
    }

    @Test
    fun theInputIsTheOnlyAccessibleElementAndCarriesLabelAndHint() {
        val view = field { placeholder = PLACEHOLDER }
        val info = view.editText.nodeInfo()

        assertEquals("$LABEL, $HINT", info.hintText.toString())
        assertNull(info.error)
        assertFalse(info.isContentInvalid)
        listOf(view.getChildAt(0), view.message).forEach {
            assertTrue(it.importantForAccessibility != View.IMPORTANT_FOR_ACCESSIBILITY_YES)
        }
    }

    @Test
    fun errorReplacesTheHintAddsTheIconAndSetsTheNodeError() {
        val view = field { errorMessage = ERROR }
        val info = view.editText.nodeInfo()

        assertEquals(ERROR, view.messageText.toString())
        assertEquals(View.VISIBLE, view.errorIcon.visibility)
        assertEquals(LABEL, info.hintText.toString())
        assertEquals(ERROR, info.error.toString())
        assertTrue(info.isContentInvalid)
        assertEquals(TSColorRole.Danger, view.resolved.border)
        assertEquals("Error: $ERROR", view.message.contentDescription.toString())
        assertEquals(ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE, ViewCompat.getAccessibilityLiveRegion(view.message))

        view.errorMessage = null

        assertEquals(HINT, view.messageText.toString())
        assertEquals(View.GONE, view.errorIcon.visibility)
        assertEquals(ViewCompat.ACCESSIBILITY_LIVE_REGION_NONE, ViewCompat.getAccessibilityLiveRegion(view.message))
        assertEquals(TSColorRole.BorderStrong, view.resolved.border)
    }

    @Test
    fun focusDrawsATwoDpFocusBorder() {
        val view = field()
        assertEquals(1f, view.resolved.borderWidthDp, 0f)

        view.editText.requestFocus()

        assertTrue(view.resolved.showsFocusRing)
        assertEquals(TSColorRole.Focus, view.resolved.border)
        assertEquals(2f, view.resolved.borderWidthDp, 0f)
    }

    @Test
    fun disabledBlocksFocusAndDimsOnlyTheBox() {
        val view = field { text = "Ada" }
        view.isEnabled = false

        view.editText.tapCenter()

        assertFalse(view.editText.isEnabled)
        assertFalse(view.editText.isFocused)
        assertEquals(0.45f, view.fieldBox.alpha, 0.001f)
        assertEquals(1f, view.getChildAt(0).alpha, 0f)
        assertEquals(TSColorRole.BackgroundSecondary, view.resolved.fill)
    }

    @Test
    fun readOnlyIsSelectableButNotEditable() {
        val view = field { text = "TS-2048"; isReadOnly = true }

        view.type("x")

        assertEquals("TS-2048", view.text)
        assertFalse(view.editText.onCheckIsTextEditor())
        assertTrue(view.editText.isEnabled)
        assertTrue(view.editText.isLongClickable)
        assertFalse(view.resolved.showsFocusRing)
        assertEquals(TSColorRole.BackgroundSecondary, view.resolved.fill)

        view.isReadOnly = false
        view.editText.setSelection(view.text.length)
        view.type("x")

        assertEquals("TS-2048x", view.text)
    }

    @Test
    fun readOnlySecureTextStaysMaskedWithoutAToggle() {
        val view = field { text = "secret"; isSecure = true; isReadOnly = true }

        assertTrue(view.editText.transformationMethod is PasswordTransformationMethod)
        assertEquals(View.GONE, view.passwordToggle.visibility)
    }

    @Test
    fun secureToggleRevealsAndHidesThePassword() {
        val view = field { text = "secret"; isSecure = true }
        val toggle = view.passwordToggle
        assertTrue(view.editText.transformationMethod is PasswordTransformationMethod)
        assertEquals("Show password", toggle.contentDescription)
        assertFalse(toggle.isSelected)
        assertEquals(ImageButton::class.java.name, toggle.nodeInfo().className)

        toggle.performClick()

        assertTrue(view.isPasswordRevealed)
        assertFalse(view.editText.transformationMethod is PasswordTransformationMethod)
        assertEquals("Hide password", toggle.contentDescription)
        assertTrue(toggle.isSelected)
        assertEquals(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD, view.editText.inputType and InputType.TYPE_MASK_VARIATION)

        toggle.performClick()

        assertTrue(view.editText.transformationMethod is PasswordTransformationMethod)
        assertEquals("secret", view.text)
    }

    @Test
    fun passwordToggleIs48dpWideAndFillsTheBoxHeight() {
        val view = field { isSecure = true }
        view.layOut()

        assertEquals(48, view.passwordToggle.width)
        assertEquals(44, view.passwordToggle.height)
        assertEquals(44, view.fieldBox.height)
    }

    @Test
    fun passwordToggleTouchTargetIs48By48OnTheField() {
        val view = field { isSecure = true }
        view.layOut()
        val bounds = requireNotNull((view.touchDelegate as TSTouchDelegateGroup).bounds[view.passwordToggle])
        assertEquals(48, bounds.width())
        assertEquals(48, bounds.height())
        assertTrue(bounds.top < view.fieldBox.top)
        assertTrue(bounds.bottom > view.fieldBox.bottom)
    }

    @Test
    fun tapJustOutsideTheBoxStillReachesThePasswordToggle() {
        val view = field { isSecure = true }
        view.layOut()
        val bounds = requireNotNull((view.touchDelegate as TSTouchDelegateGroup).bounds[view.passwordToggle])
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(time, time, action, bounds.exactCenterX(), bounds.bottom - 1f, 0)
            view.dispatchTouchEvent(event)
            event.recycle()
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(bounds.bottom - 1 >= view.fieldBox.bottom)
        assertTrue(view.isPasswordRevealed)
    }

    @Test
    fun inputTouchTargetIs48TallAndATapJustBelowTheBoxFocusesIt() {
        val view = field()
        view.layOut()
        val bounds = requireNotNull((view.touchDelegate as TSTouchDelegateGroup).bounds[view.editText])
        assertTrue(bounds.height() >= 48)
        assertFalse(view.editText.isFocused)
        val time = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(time, time, action, bounds.left + 10f, bounds.bottom - 1f, 0)
            view.dispatchTouchEvent(event)
            event.recycle()
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(bounds.bottom - 1 >= view.fieldBox.bottom)
        assertTrue(view.editText.isFocused)
    }

    @Test
    fun hiddenToggleHasNoTouchTarget() {
        val view = field()
        view.layOut()
        assertNull((view.touchDelegate as TSTouchDelegateGroup).bounds[view.passwordToggle])
    }

    @Test
    fun revealingKeepsTheSansSerifFace() {
        val view = field { text = "secret"; isSecure = true }
        val face = view.editText.typeface

        view.passwordToggle.performClick()
        view.passwordToggle.performClick()

        assertEquals(face, view.editText.typeface)
    }

    @Test
    fun multilineGrowsFromThreeToSixLinesThenScrolls() {
        val view = field { isMultiline = true; hint = null }
        view.layOut()
        assertEquals(92, view.fieldBox.height)

        view.text = "1\n2\n3"
        view.layOut()
        assertEquals(92, view.fieldBox.height)

        view.text = "1\n2\n3\n4\n5"
        view.layOut()
        assertTrue(view.fieldBox.height in 93..163)

        view.text = (1..10).joinToString("\n")
        view.layOut()
        assertEquals(164, view.fieldBox.height)
        assertTrue(view.editText.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0)
        assertTrue(view.editText.layout.height > view.editText.height)
    }

    @Test
    fun secureIsIgnoredWhenMultiline() {
        val view = field { isSecure = true; isMultiline = true }

        assertEquals(View.GONE, view.passwordToggle.visibility)
        assertFalse(view.editText.transformationMethod is PasswordTransformationMethod)
    }

    @Test
    fun inputTraitsSetKeyboardImeAndAutofill() {
        val view = field { inputTraits = TSTextInputTraits.Email.copy(imeAction = TSImeAction.Next) }

        assertEquals(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, view.editText.inputType and InputType.TYPE_MASK_VARIATION)
        assertEquals(EditorInfo.IME_ACTION_NEXT, view.editText.imeOptions)
        assertEquals(listOf(View.AUTOFILL_HINT_EMAIL_ADDRESS), view.editText.autofillHints?.toList())
        assertEquals(View.IMPORTANT_FOR_AUTOFILL_YES, view.editText.importantForAutofill)
    }

    @Test
    fun codeTraitsOpenAVisibleAsciiCapitalsKeyboardWithoutSuggestions() {
        val view = field { text = "AB12CD"; inputTraits = TSTextInputTraits.Code }
        val inputType = view.editText.inputType

        assertEquals(InputType.TYPE_CLASS_TEXT, inputType and InputType.TYPE_MASK_CLASS)
        assertEquals(InputType.TYPE_TEXT_VARIATION_NORMAL, inputType and InputType.TYPE_MASK_VARIATION)
        assertTrue(inputType and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS != 0)
        assertTrue(inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0)
        assertEquals(0, inputType and InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
        assertTrue(view.editText.imeOptions and EditorInfo.IME_FLAG_FORCE_ASCII != 0)
        assertFalse(view.editText.transformationMethod is PasswordTransformationMethod)
        assertEquals("AB12CD", view.editText.layout.text.toString())
    }

    @Test
    fun codeContentStyleUsesTheMonospaceCodeRoleForTheInputOnly() {
        val plain = field()
        val view = field { contentStyle = TSTextFieldContentStyle.Code }

        assertEquals(TSTypeRole.Code, view.resolved.inputTypeRole)
        assertEquals(Typeface.MONOSPACE, view.editText.typeface)
        assertEquals(13f, view.editText.textSize, 0f)
        assertEquals(plain.label, view.label)
        plain.layOut()
        view.layOut()
        assertEquals(plain.fieldBox.height, view.fieldBox.height)

        view.contentStyle = TSTextFieldContentStyle.Default
        assertEquals(Typeface.DEFAULT, view.editText.typeface)
    }

    @Test
    fun leadingIconAndTrailingViewAreShown() {
        val trailing = View(RuntimeEnvironment.getApplication())
        val view = field { leadingIcon = TSIcon.Search; trailingView = trailing }

        assertEquals(View.VISIBLE, (view.fieldBox as ViewGroup).getChildAt(0).visibility)
        assertEquals(view.fieldBox, trailing.parent.parent)
    }

    @Test
    fun xmlAttributesConfigureTheField() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(R.attr.tsLabel, "Password")
            .addAttribute(R.attr.tsHint, "At least 8 characters")
            .addAttribute(R.attr.tsError, "Too short")
            .addAttribute(R.attr.tsLeadingIcon, "lock")
            .addAttribute(R.attr.tsSecure, "true")
            .addAttribute(R.attr.tsInputTraits, "newPassword")
            .build()
        val view = hostInActivity { TSTextFieldView(it, attrs) }

        assertEquals("Password", view.label.toString())
        assertEquals("Too short", view.errorMessage.toString())
        assertEquals(TSIcon.Lock, view.leadingIcon)
        assertTrue(view.isSecure)
        assertEquals(TSTextInputTraits.NewPassword, view.inputTraits)
        assertNotEquals(View.GONE, view.passwordToggle.visibility)
    }

    @Test
    fun xmlAttributesSelectTheCodeStyleAndTraits() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(R.attr.tsLabel, "Join code")
            .addAttribute(R.attr.tsContentStyle, "code")
            .addAttribute(R.attr.tsInputTraits, "code")
            .build()
        val view = hostInActivity { TSTextFieldView(it, attrs) }

        assertEquals(TSTextFieldContentStyle.Code, view.contentStyle)
        assertEquals(TSTextInputTraits.Code, view.inputTraits)
        assertEquals(Typeface.MONOSPACE, view.editText.typeface)
    }

    @Test
    fun xmlDisabledDisablesTheInput() {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.enabled, "false")
            .addAttribute(R.attr.tsLabel, "Name")
            .build()
        val view = hostInActivity { TSTextFieldView(it, attrs) }

        assertFalse(view.isEnabled)
        assertFalse(view.editText.isEnabled)
        assertEquals(0.45f, view.fieldBox.alpha, 0.001f)
    }

    private companion object {
        const val LABEL = "Email"
        const val HINT = "We never share your email."
        const val PLACEHOLDER = "you@example.com"
        const val ERROR = "Enter an email like name@example.com"
    }
}
