package com.ts.ds.compose.theme

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.ts.ds.compose.foundation.TSPressIndication

@Immutable
internal class TSThemeValues(
    val colors: TSColors,
    val typography: TSTypography,
    val elevation: TSElevation,
    val isDark: Boolean,
    val rippleEnabled: Boolean,
    val hapticsEnabled: Boolean,
)

@Suppress("ComposeCompositionLocalUsage")
internal val LocalTSTheme = staticCompositionLocalOf {
    TSThemeValues(
        colors = lightTSColors(),
        typography = tsTypography(DsFontWeights::resolve).withDsFontFamilies(),
        elevation = lightTSElevation(),
        isDark = false,
        rippleEnabled = false,
        hapticsEnabled = true,
    )
}

@Composable
public fun TSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    rippleEnabled: Boolean = false,
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val values = remember(darkTheme, rippleEnabled, hapticsEnabled) {
        TSThemeValues(
            colors = if (darkTheme) darkTSColors() else lightTSColors(),
            typography = tsTypography(DsFontWeights::resolve).withDsFontFamilies(),
            elevation = if (darkTheme) darkTSElevation() else lightTSElevation(),
            isDark = darkTheme,
            rippleEnabled = rippleEnabled,
            hapticsEnabled = hapticsEnabled,
        )
    }
    val colorScheme = remember(values) { TSMaterialBridge.colorScheme(values.colors, darkTheme) }
    val indication: Indication =
        if (rippleEnabled) ripple() else remember(values) { TSPressIndication(values.colors.surfacePressed) }
    val rippleConfiguration = if (rippleEnabled) RippleConfiguration(color = values.colors.textPrimary) else null

    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(
            LocalTSTheme provides values,
            LocalIndication provides indication,
            LocalRippleConfiguration provides rippleConfiguration,
            content = content,
        )
    }
}

public object TSTheme {
    public val colors: TSColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTSTheme.current.colors

    public val typography: TSTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTSTheme.current.typography

    public val elevation: TSElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalTSTheme.current.elevation

    public val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalTSTheme.current.isDark

    public val rippleEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalTSTheme.current.rippleEnabled

    public val hapticsEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalTSTheme.current.hapticsEnabled
}
