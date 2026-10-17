package com.ts.ds.views.theme

import android.content.Context
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.views.R

@get:AttrRes
public val TSColorRole.themeAttr: Int
    get() = when (this) {
        TSColorRole.BackgroundPrimary -> R.attr.tsColorBackgroundPrimary
        TSColorRole.BackgroundSecondary -> R.attr.tsColorBackgroundSecondary
        TSColorRole.SurfaceCard -> R.attr.tsColorSurfaceCard
        TSColorRole.SurfaceElevated -> R.attr.tsColorSurfaceElevated
        TSColorRole.SurfaceHover -> R.attr.tsColorSurfaceHover
        TSColorRole.SurfacePressed -> R.attr.tsColorSurfacePressed
        TSColorRole.SurfaceInverse -> R.attr.tsColorSurfaceInverse
        TSColorRole.Overlay -> R.attr.tsColorOverlay
        TSColorRole.BorderSubtle -> R.attr.tsColorBorderSubtle
        TSColorRole.BorderStrong -> R.attr.tsColorBorderStrong
        TSColorRole.TextPrimary -> R.attr.tsColorTextPrimary
        TSColorRole.TextSecondary -> R.attr.tsColorTextSecondary
        TSColorRole.TextMuted -> R.attr.tsColorTextMuted
        TSColorRole.TextDisabled -> R.attr.tsColorTextDisabled
        TSColorRole.TextInverse -> R.attr.tsColorTextInverse
        TSColorRole.TextOnAccent -> R.attr.tsColorTextOnAccent
        TSColorRole.AccentPrimary -> R.attr.tsColorAccentPrimary
        TSColorRole.AccentHover -> R.attr.tsColorAccentHover
        TSColorRole.AccentPressed -> R.attr.tsColorAccentPressed
        TSColorRole.AccentSubtle -> R.attr.tsColorAccentSubtle
        TSColorRole.AccentText -> R.attr.tsColorAccentText
        TSColorRole.Focus -> R.attr.tsColorFocus
        TSColorRole.Success -> R.attr.tsColorSuccess
        TSColorRole.SuccessSubtle -> R.attr.tsColorSuccessSubtle
        TSColorRole.Warning -> R.attr.tsColorWarning
        TSColorRole.WarningSubtle -> R.attr.tsColorWarningSubtle
        TSColorRole.Danger -> R.attr.tsColorDanger
        TSColorRole.DangerSubtle -> R.attr.tsColorDangerSubtle
        TSColorRole.DangerSolid -> R.attr.tsColorDangerSolid
        TSColorRole.Info -> R.attr.tsColorInfo
        TSColorRole.InfoSubtle -> R.attr.tsColorInfoSubtle
        TSColorRole.DangerPressed -> R.attr.tsColorDangerPressed
        TSColorRole.SwitchThumb -> R.attr.tsColorSwitchThumb
        TSColorRole.SuccessOnInverse -> R.attr.tsColorSuccessOnInverse
        TSColorRole.WarningOnInverse -> R.attr.tsColorWarningOnInverse
        TSColorRole.DangerOnInverse -> R.attr.tsColorDangerOnInverse
        TSColorRole.InfoOnInverse -> R.attr.tsColorInfoOnInverse
    }

@get:StyleRes
public val TSTypeRole.textAppearance: Int
    get() = when (this) {
        TSTypeRole.Display -> R.style.TextAppearance_TS_Display
        TSTypeRole.Title1 -> R.style.TextAppearance_TS_Title1
        TSTypeRole.Title2 -> R.style.TextAppearance_TS_Title2
        TSTypeRole.Title3 -> R.style.TextAppearance_TS_Title3
        TSTypeRole.BodyLg -> R.style.TextAppearance_TS_BodyLg
        TSTypeRole.Body -> R.style.TextAppearance_TS_Body
        TSTypeRole.BodySm -> R.style.TextAppearance_TS_BodySm
        TSTypeRole.Label -> R.style.TextAppearance_TS_Label
        TSTypeRole.Caption -> R.style.TextAppearance_TS_Caption
        TSTypeRole.Overline -> R.style.TextAppearance_TS_Overline
        TSTypeRole.Code -> R.style.TextAppearance_TS_Code
    }

@ColorInt
public fun Context.tsColor(role: TSColorRole): Int = tsColor(role.themeAttr)

@ColorInt
public fun View.tsColor(role: TSColorRole): Int = tsColor(role.themeAttr)
