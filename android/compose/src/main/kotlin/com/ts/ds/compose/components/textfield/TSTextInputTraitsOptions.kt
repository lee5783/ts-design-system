package com.ts.ds.compose.components.textfield

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.ts.ds.core.input.TSCapitalization
import com.ts.ds.core.input.TSImeAction
import com.ts.ds.core.input.TSKeyboardType
import com.ts.ds.core.input.TSTextInputTraits

internal fun TSTextInputTraits.keyboardOptions(masked: Boolean): KeyboardOptions = KeyboardOptions(
    capitalization = when (capitalization) {
        TSCapitalization.None -> KeyboardCapitalization.None
        TSCapitalization.Sentences -> KeyboardCapitalization.Sentences
        TSCapitalization.Words -> KeyboardCapitalization.Words
        TSCapitalization.Characters -> KeyboardCapitalization.Characters
    },
    autoCorrectEnabled = autoCorrect && !masked,
    keyboardType = keyboardType(masked),
    imeAction = composeImeAction(),
)

internal fun TSTextInputTraits.composeImeAction(): ImeAction = when (imeAction) {
    TSImeAction.Default -> ImeAction.Default
    TSImeAction.Done -> ImeAction.Done
    TSImeAction.Go -> ImeAction.Go
    TSImeAction.Next -> ImeAction.Next
    TSImeAction.Search -> ImeAction.Search
    TSImeAction.Send -> ImeAction.Send
}

internal fun TSTextInputTraits.keyboardType(masked: Boolean): KeyboardType = when (keyboardType) {
    TSKeyboardType.Text -> if (masked) KeyboardType.Password else KeyboardType.Text
    TSKeyboardType.Email -> if (masked) KeyboardType.Password else KeyboardType.Email
    TSKeyboardType.Uri -> if (masked) KeyboardType.Password else KeyboardType.Uri
    TSKeyboardType.Ascii -> if (masked || !autoCorrect) KeyboardType.Password else KeyboardType.Ascii
    TSKeyboardType.Phone -> KeyboardType.Phone
    TSKeyboardType.Number -> if (masked) KeyboardType.NumberPassword else KeyboardType.Number
    TSKeyboardType.Decimal -> if (masked) KeyboardType.NumberPassword else KeyboardType.Decimal
}

internal fun TSTextInputTraits.contentType(): ContentType? =
    autofillHints.map(::ContentType).reduceOrNull { first, second -> first + second }

internal fun TSTextInputTraits.keyboardActions(onSubmit: (() -> Unit)?): KeyboardActions {
    if (onSubmit == null) return KeyboardActions.Default
    val action = composeImeAction()
    return KeyboardActions {
        onSubmit()
        defaultKeyboardAction(action)
    }
}
