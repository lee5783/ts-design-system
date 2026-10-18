package com.tinysol.designsystem.core.input

import android.view.View

public enum class TSKeyboardType {
    Text,
    Email,
    Phone,
    Number,
    Decimal,
    Uri,
    Ascii,
}

public enum class TSImeAction {
    Default,
    Done,
    Go,
    Next,
    Search,
    Send,
}

public enum class TSCapitalization {
    None,
    Sentences,
    Words,
    Characters,
}

public data class TSTextInputTraits(
    public val keyboardType: TSKeyboardType = TSKeyboardType.Text,
    public val imeAction: TSImeAction = TSImeAction.Default,
    public val capitalization: TSCapitalization = TSCapitalization.Sentences,
    public val autoCorrect: Boolean = true,
    public val autofillHints: List<String> = emptyList(),
) {
    public companion object {
        public const val AUTOFILL_HINT_NEW_PASSWORD: String = "newPassword"
        public const val AUTOFILL_HINT_SMS_OTP: String = "smsOTPCode"

        public val Default: TSTextInputTraits = TSTextInputTraits()
        public val Name: TSTextInputTraits = TSTextInputTraits(
            capitalization = TSCapitalization.Words,
            autoCorrect = false,
            autofillHints = listOf(View.AUTOFILL_HINT_NAME),
        )
        public val Email: TSTextInputTraits = TSTextInputTraits(
            keyboardType = TSKeyboardType.Email,
            capitalization = TSCapitalization.None,
            autoCorrect = false,
            autofillHints = listOf(View.AUTOFILL_HINT_EMAIL_ADDRESS),
        )
        public val Username: TSTextInputTraits = TSTextInputTraits(
            capitalization = TSCapitalization.None,
            autoCorrect = false,
            autofillHints = listOf(View.AUTOFILL_HINT_USERNAME),
        )
        public val Password: TSTextInputTraits = TSTextInputTraits(
            capitalization = TSCapitalization.None,
            autoCorrect = false,
            autofillHints = listOf(View.AUTOFILL_HINT_PASSWORD),
        )
        public val NewPassword: TSTextInputTraits = TSTextInputTraits(
            capitalization = TSCapitalization.None,
            autoCorrect = false,
            autofillHints = listOf(AUTOFILL_HINT_NEW_PASSWORD),
        )
        public val OneTimeCode: TSTextInputTraits = TSTextInputTraits(
            keyboardType = TSKeyboardType.Number,
            capitalization = TSCapitalization.None,
            autoCorrect = false,
            autofillHints = listOf(AUTOFILL_HINT_SMS_OTP),
        )
        public val Phone: TSTextInputTraits = TSTextInputTraits(
            keyboardType = TSKeyboardType.Phone,
            capitalization = TSCapitalization.None,
            autoCorrect = false,
            autofillHints = listOf(View.AUTOFILL_HINT_PHONE),
        )
        public val Number: TSTextInputTraits = TSTextInputTraits(
            keyboardType = TSKeyboardType.Number,
            capitalization = TSCapitalization.None,
            autoCorrect = false,
        )
        public val Decimal: TSTextInputTraits = TSTextInputTraits(
            keyboardType = TSKeyboardType.Decimal,
            capitalization = TSCapitalization.None,
            autoCorrect = false,
        )
        public val Url: TSTextInputTraits = TSTextInputTraits(
            keyboardType = TSKeyboardType.Uri,
            capitalization = TSCapitalization.None,
            autoCorrect = false,
        )
        public val Code: TSTextInputTraits = TSTextInputTraits(
            keyboardType = TSKeyboardType.Ascii,
            capitalization = TSCapitalization.Characters,
            autoCorrect = false,
        )
        public val Search: TSTextInputTraits = TSTextInputTraits(
            imeAction = TSImeAction.Search,
            capitalization = TSCapitalization.None,
            autoCorrect = false,
        )
    }
}
