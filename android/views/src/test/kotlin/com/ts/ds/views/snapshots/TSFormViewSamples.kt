package com.ts.ds.views.snapshots

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.views.components.iconbutton.TSIconButtonView
import com.ts.ds.views.components.textfield.TSTextFieldView

private const val SAMPLE_EMAIL = "ada@example.com"
private const val SAMPLE_EMAIL_PLACEHOLDER = "you@example.com"
private const val SAMPLE_EMAIL_HINT = "We never share your email."
private const val SAMPLE_EMAIL_ERROR = "Enter an email like name@example.com"
private const val SAMPLE_PASSWORD_HINT = "At least 8 characters"
private const val SAMPLE_BIO = "The quick brown fox jumps over the lazy dog"
private val sampleLongBio = (1..9).joinToString("\n") { "Line $it of a long note" }

private val fill = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

private fun LinearLayout.addField(context: Context, configure: TSTextFieldView.() -> Unit): TSTextFieldView =
    TSTextFieldView(context).apply(configure).also { addView(it, LinearLayout.LayoutParams(fill)) }

private fun TSTextFieldView.focusWhenAttached() {
    editText.isCursorVisible = false
    post { editText.requestFocus() }
}

internal fun textFieldStatesSample(context: Context): View = context.galleryColumn().apply {
    addField(context) { label = "Default"; placeholder = SAMPLE_EMAIL_PLACEHOLDER; hint = SAMPLE_EMAIL_HINT }
    addField(context) { label = "Filled"; text = SAMPLE_EMAIL }
    addField(context) { label = "Focused"; text = SAMPLE_EMAIL; hint = SAMPLE_EMAIL_HINT; focusWhenAttached() }
    addField(context) { label = "Error"; text = "ada@"; hint = SAMPLE_EMAIL_HINT; errorMessage = SAMPLE_EMAIL_ERROR }
    addField(context) { label = "Disabled"; text = "Ada Lovelace"; hint = "Managed by your organisation"; isEnabled = false }
    addField(context) { label = "Read-only"; text = "TS-2048-7731"; hint = "Long-press to copy"; isReadOnly = true }
}

internal fun textFieldContentSample(context: Context): View = context.galleryColumn().apply {
    addField(context) {
        label = "Search"
        text = "Analytical"
        leadingIcon = TSIcon.Search
        trailingView = TSIconButtonView(context).apply { icon = TSIcon.Close; label = "Clear" }
    }
    addField(context) { label = "Password"; text = "secret"; hint = SAMPLE_PASSWORD_HINT; leadingIcon = TSIcon.Lock; isSecure = true }
    addField(context) { label = "Password revealed"; text = "secret"; isSecure = true; passwordToggle.performClick() }
    addField(context) { label = "Bio"; text = SAMPLE_BIO; hint = "Grows to 6 lines, then scrolls"; isMultiline = true }
    addField(context) { label = "Notes"; text = sampleLongBio; isMultiline = true }
    addField(context) { label = "Error focused"; text = "ada@"; errorMessage = SAMPLE_EMAIL_ERROR; focusWhenAttached() }
}
