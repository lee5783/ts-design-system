package com.tinysol.designsystem.views.components.textfield

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.tinysol.designsystem.core.input.TSCapitalization
import com.tinysol.designsystem.core.input.TSImeAction
import com.tinysol.designsystem.core.input.TSKeyboardType
import com.tinysol.designsystem.core.input.TSTextInputTraits

internal fun TSTextInputTraits.inputType(secure: Boolean, revealed: Boolean, multiline: Boolean): Int {
    val masked = secure && !revealed
    return when (keyboardType) {
        TSKeyboardType.Phone -> InputType.TYPE_CLASS_PHONE
        TSKeyboardType.Number -> InputType.TYPE_CLASS_NUMBER or numberVariation(masked)
        TSKeyboardType.Decimal -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or numberVariation(masked)
        TSKeyboardType.Text, TSKeyboardType.Email, TSKeyboardType.Uri, TSKeyboardType.Ascii ->
            InputType.TYPE_CLASS_TEXT or textVariation(secure, revealed) or textFlags(secure, multiline)
    }
}

internal fun TSTextInputTraits.imeOptions(): Int = imeActionOption() or asciiOption()

private fun TSTextInputTraits.imeActionOption(): Int = when (imeAction) {
    TSImeAction.Default -> EditorInfo.IME_ACTION_UNSPECIFIED
    TSImeAction.Done -> EditorInfo.IME_ACTION_DONE
    TSImeAction.Go -> EditorInfo.IME_ACTION_GO
    TSImeAction.Next -> EditorInfo.IME_ACTION_NEXT
    TSImeAction.Search -> EditorInfo.IME_ACTION_SEARCH
    TSImeAction.Send -> EditorInfo.IME_ACTION_SEND
}

private fun TSTextInputTraits.asciiOption(): Int = if (keyboardType == TSKeyboardType.Ascii) EditorInfo.IME_FLAG_FORCE_ASCII else 0

private fun numberVariation(masked: Boolean): Int = if (masked) InputType.TYPE_NUMBER_VARIATION_PASSWORD else 0

private fun TSTextInputTraits.textVariation(secure: Boolean, revealed: Boolean): Int = when {
    secure && revealed -> InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    secure -> InputType.TYPE_TEXT_VARIATION_PASSWORD
    keyboardType == TSKeyboardType.Email -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    keyboardType == TSKeyboardType.Uri -> InputType.TYPE_TEXT_VARIATION_URI
    else -> InputType.TYPE_TEXT_VARIATION_NORMAL
}

private fun TSTextInputTraits.textFlags(secure: Boolean, multiline: Boolean): Int {
    val capitalization = when (capitalization) {
        TSCapitalization.None -> 0
        TSCapitalization.Sentences -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        TSCapitalization.Words -> InputType.TYPE_TEXT_FLAG_CAP_WORDS
        TSCapitalization.Characters -> InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    }
    val correction = if (autoCorrect && !secure) InputType.TYPE_TEXT_FLAG_AUTO_CORRECT else InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
    val lines = if (multiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0
    return (if (secure) 0 else capitalization) or correction or lines
}
