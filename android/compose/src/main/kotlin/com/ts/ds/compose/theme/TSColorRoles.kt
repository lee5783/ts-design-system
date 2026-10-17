package com.ts.ds.compose.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSShadowRole
import com.ts.ds.core.style.TSTypeRole

public fun TSColors.color(role: TSColorRole): Color = when (role) {
    TSColorRole.BackgroundPrimary -> backgroundPrimary
    TSColorRole.BackgroundSecondary -> backgroundSecondary
    TSColorRole.SurfaceCard -> surfaceCard
    TSColorRole.SurfaceElevated -> surfaceElevated
    TSColorRole.SurfaceHover -> surfaceHover
    TSColorRole.SurfacePressed -> surfacePressed
    TSColorRole.SurfaceInverse -> surfaceInverse
    TSColorRole.Overlay -> overlay
    TSColorRole.BorderSubtle -> borderSubtle
    TSColorRole.BorderStrong -> borderStrong
    TSColorRole.TextPrimary -> textPrimary
    TSColorRole.TextSecondary -> textSecondary
    TSColorRole.TextMuted -> textMuted
    TSColorRole.TextDisabled -> textDisabled
    TSColorRole.TextInverse -> textInverse
    TSColorRole.TextOnAccent -> textOnAccent
    TSColorRole.AccentPrimary -> accentPrimary
    TSColorRole.AccentHover -> accentHover
    TSColorRole.AccentPressed -> accentPressed
    TSColorRole.AccentSubtle -> accentSubtle
    TSColorRole.AccentText -> accentText
    TSColorRole.Focus -> focus
    TSColorRole.Success -> success
    TSColorRole.SuccessSubtle -> successSubtle
    TSColorRole.Warning -> warning
    TSColorRole.WarningSubtle -> warningSubtle
    TSColorRole.Danger -> danger
    TSColorRole.DangerSubtle -> dangerSubtle
    TSColorRole.DangerSolid -> dangerSolid
    TSColorRole.Info -> info
    TSColorRole.InfoSubtle -> infoSubtle
    TSColorRole.DangerPressed -> dangerPressed
    TSColorRole.SwitchThumb -> switchThumb
    TSColorRole.SuccessOnInverse -> successOnInverse
    TSColorRole.WarningOnInverse -> warningOnInverse
    TSColorRole.DangerOnInverse -> dangerOnInverse
    TSColorRole.InfoOnInverse -> infoOnInverse
}

public fun TSTypography.style(role: TSTypeRole): TextStyle = when (role) {
    TSTypeRole.Display -> display
    TSTypeRole.Title1 -> title1
    TSTypeRole.Title2 -> title2
    TSTypeRole.Title3 -> title3
    TSTypeRole.BodyLg -> bodyLg
    TSTypeRole.Body -> body
    TSTypeRole.BodySm -> bodySm
    TSTypeRole.Label -> label
    TSTypeRole.Caption -> caption
    TSTypeRole.Overline -> overline
    TSTypeRole.Code -> code
}

public fun TSElevation.shadow(role: TSShadowRole): TSShadow = when (role) {
    TSShadowRole.Xs -> shadowXs
    TSShadowRole.Sm -> shadowSm
    TSShadowRole.Md -> shadowMd
    TSShadowRole.Lg -> shadowLg
}
