package com.ts.ds.compose.snapshots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.ts.ds.compose.components.iconbutton.TSIconButton
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.components.textfield.TSTextField
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.input.TSTextInputTraits
import com.ts.ds.core.style.TSControlState
import com.ts.ds.core.style.TSTextFieldContentStyle

internal const val SAMPLE_EMAIL = "ada@example.com"
internal const val SAMPLE_EMAIL_PLACEHOLDER = "you@example.com"
internal const val SAMPLE_EMAIL_HINT = "We never share your email."
internal const val SAMPLE_EMAIL_ERROR = "Enter an email like name@example.com"
internal const val SAMPLE_PASSWORD_HINT = "At least 8 characters"
internal const val SAMPLE_BIO = "The quick brown fox jumps over the lazy dog"
internal const val SAMPLE_CODE = "0O1I5S"
internal const val SAMPLE_CODE_PLACEHOLDER = "ABCDEF"
internal const val SAMPLE_CODE_HINT = "6 characters, letters and digits"
internal const val SAMPLE_CODE_ERROR = "Enter all 6 characters"
internal val sampleLongBio = (1..9).joinToString("\n") { "Line $it of a long note" }

@Composable
private fun ForcedTextField(state: TSControlState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTSForcedControlState provides state, content = content)
}

@Composable
internal fun TextFieldStatesSample() {
    SampleColumn {
        TSTextField("", "Default", {}, placeholder = SAMPLE_EMAIL_PLACEHOLDER, hint = SAMPLE_EMAIL_HINT)
        TSTextField(SAMPLE_EMAIL, "Filled", {})
        ForcedTextField(TSControlState.Focused) {
            TSTextField(SAMPLE_EMAIL, "Focused", {}, hint = SAMPLE_EMAIL_HINT)
        }
        TSTextField("ada@", "Error", {}, hint = SAMPLE_EMAIL_HINT, error = SAMPLE_EMAIL_ERROR)
        ForcedTextField(TSControlState.Focused) {
            TSTextField("ada@", "Error focused", {}, error = SAMPLE_EMAIL_ERROR)
        }
        TSTextField("Ada Lovelace", "Disabled", {}, hint = "Managed by your organisation", enabled = false)
        TSTextField("TS-2048-7731", "Read-only", {}, hint = "Long-press to copy", readOnly = true)
    }
}

@Composable
internal fun TextFieldContentSample() {
    SampleColumn {
        TSTextField("Analytical", "Search", {}, leadingIcon = TSIcon.Search) {
            TSIconButton(TSIcon.Close, contentDescription = "Clear", onClick = {})
        }
        TSTextField("secret", "Password", {}, hint = SAMPLE_PASSWORD_HINT, leadingIcon = TSIcon.Lock, secure = true)
        ForcedTextField(TSControlState(selected = true)) {
            TSTextField("secret", "Password revealed", {}, secure = true)
        }
        TSTextField(SAMPLE_BIO, "Bio", {}, hint = "Grows to 6 lines, then scrolls", multiline = true)
        TSTextField(sampleLongBio, "Notes", {}, multiline = true)
    }
}

@Composable
internal fun TextFieldCodeSample() {
    val code = TSTextFieldContentStyle.Code
    val traits = TSTextInputTraits.Code
    SampleColumn {
        TSTextField("", "Idle", {}, code, placeholder = SAMPLE_CODE_PLACEHOLDER, hint = SAMPLE_CODE_HINT, inputTraits = traits)
        TSTextField(SAMPLE_CODE, "Filled", {}, code, hint = SAMPLE_CODE_HINT, inputTraits = traits)
        ForcedTextField(TSControlState.Focused) {
            TSTextField(SAMPLE_CODE, "Focused", {}, code, hint = SAMPLE_CODE_HINT, inputTraits = traits)
        }
        TSTextField("0O1", "Error", {}, code, hint = SAMPLE_CODE_HINT, error = SAMPLE_CODE_ERROR, inputTraits = traits)
        TSTextField(SAMPLE_CODE, "Disabled", {}, code, hint = SAMPLE_CODE_HINT, inputTraits = traits, enabled = false)
    }
}
