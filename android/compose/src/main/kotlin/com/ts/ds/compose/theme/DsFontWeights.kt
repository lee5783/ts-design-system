package com.ts.ds.compose.theme

import android.graphics.Typeface
import android.os.Build
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

public object DsFontWeights {
    public const val TRUE_SEMI_BOLD_MIN_SDK: Int = Build.VERSION_CODES.P
    public const val LEGACY_MEDIUM_FAMILY: String = "sans-serif-medium"

    public fun resolve(weight: Int, sdkInt: Int = Build.VERSION.SDK_INT): FontWeight =
        if (weight == FontWeight.SemiBold.weight && sdkInt < TRUE_SEMI_BOLD_MIN_SDK) FontWeight.Medium else FontWeight(weight)

    public fun semiBold(sdkInt: Int = Build.VERSION.SDK_INT): FontWeight = resolve(FontWeight.SemiBold.weight, sdkInt)
}

internal fun TSTypography.withDsFontFamilies(sdkInt: Int = Build.VERSION.SDK_INT): TSTypography {
    if (sdkInt >= DsFontWeights.TRUE_SEMI_BOLD_MIN_SDK) return this
    val fallback: (TextStyle) -> TextStyle = { it.withLegacyMediumFamily(sdkInt) }
    return TSTypography(
        display = fallback(display),
        title1 = fallback(title1),
        title2 = fallback(title2),
        title3 = fallback(title3),
        bodyLg = fallback(bodyLg),
        body = fallback(body),
        bodySm = fallback(bodySm),
        label = fallback(label),
        caption = fallback(caption),
        overline = fallback(overline),
        code = fallback(code),
    )
}

internal fun TextStyle.withDsFontWeight(weight: Int, sdkInt: Int = Build.VERSION.SDK_INT): TextStyle =
    copy(fontWeight = DsFontWeights.resolve(weight, sdkInt)).withLegacyMediumFamily(sdkInt)

private fun TextStyle.withLegacyMediumFamily(sdkInt: Int): TextStyle {
    val usesDefaultFamily = fontFamily == null || fontFamily == FontFamily.Default
    if (sdkInt >= DsFontWeights.TRUE_SEMI_BOLD_MIN_SDK || !usesDefaultFamily || fontWeight != FontWeight.Medium) return this
    return copy(fontFamily = FontFamily(Typeface.create(DsFontWeights.LEGACY_MEDIUM_FAMILY, Typeface.NORMAL)))
}
