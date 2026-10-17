package com.ts.ds.core.style

import com.ts.ds.tokens.TSTokenValues

public enum class TSColorRole(
    public val light: Long,
    public val dark: Long,
) {
    BackgroundPrimary(TSTokenValues.LightColors.backgroundPrimary, TSTokenValues.DarkColors.backgroundPrimary),
    BackgroundSecondary(TSTokenValues.LightColors.backgroundSecondary, TSTokenValues.DarkColors.backgroundSecondary),
    SurfaceCard(TSTokenValues.LightColors.surfaceCard, TSTokenValues.DarkColors.surfaceCard),
    SurfaceElevated(TSTokenValues.LightColors.surfaceElevated, TSTokenValues.DarkColors.surfaceElevated),
    SurfaceHover(TSTokenValues.LightColors.surfaceHover, TSTokenValues.DarkColors.surfaceHover),
    SurfacePressed(TSTokenValues.LightColors.surfacePressed, TSTokenValues.DarkColors.surfacePressed),
    SurfaceInverse(TSTokenValues.LightColors.surfaceInverse, TSTokenValues.DarkColors.surfaceInverse),
    Overlay(TSTokenValues.LightColors.overlay, TSTokenValues.DarkColors.overlay),
    BorderSubtle(TSTokenValues.LightColors.borderSubtle, TSTokenValues.DarkColors.borderSubtle),
    BorderStrong(TSTokenValues.LightColors.borderStrong, TSTokenValues.DarkColors.borderStrong),
    TextPrimary(TSTokenValues.LightColors.textPrimary, TSTokenValues.DarkColors.textPrimary),
    TextSecondary(TSTokenValues.LightColors.textSecondary, TSTokenValues.DarkColors.textSecondary),
    TextMuted(TSTokenValues.LightColors.textMuted, TSTokenValues.DarkColors.textMuted),
    TextDisabled(TSTokenValues.LightColors.textDisabled, TSTokenValues.DarkColors.textDisabled),
    TextInverse(TSTokenValues.LightColors.textInverse, TSTokenValues.DarkColors.textInverse),
    TextOnAccent(TSTokenValues.LightColors.textOnAccent, TSTokenValues.DarkColors.textOnAccent),
    AccentPrimary(TSTokenValues.LightColors.accentPrimary, TSTokenValues.DarkColors.accentPrimary),
    AccentHover(TSTokenValues.LightColors.accentHover, TSTokenValues.DarkColors.accentHover),
    AccentPressed(TSTokenValues.LightColors.accentPressed, TSTokenValues.DarkColors.accentPressed),
    AccentSubtle(TSTokenValues.LightColors.accentSubtle, TSTokenValues.DarkColors.accentSubtle),
    AccentText(TSTokenValues.LightColors.accentText, TSTokenValues.DarkColors.accentText),
    Focus(TSTokenValues.LightColors.focus, TSTokenValues.DarkColors.focus),
    Success(TSTokenValues.LightColors.success, TSTokenValues.DarkColors.success),
    SuccessSubtle(TSTokenValues.LightColors.successSubtle, TSTokenValues.DarkColors.successSubtle),
    Warning(TSTokenValues.LightColors.warning, TSTokenValues.DarkColors.warning),
    WarningSubtle(TSTokenValues.LightColors.warningSubtle, TSTokenValues.DarkColors.warningSubtle),
    Danger(TSTokenValues.LightColors.danger, TSTokenValues.DarkColors.danger),
    DangerSubtle(TSTokenValues.LightColors.dangerSubtle, TSTokenValues.DarkColors.dangerSubtle),
    DangerSolid(TSTokenValues.LightColors.dangerSolid, TSTokenValues.DarkColors.dangerSolid),
    Info(TSTokenValues.LightColors.info, TSTokenValues.DarkColors.info),
    InfoSubtle(TSTokenValues.LightColors.infoSubtle, TSTokenValues.DarkColors.infoSubtle),
    DangerPressed(TSTokenValues.LightColors.dangerPressed, TSTokenValues.DarkColors.dangerPressed),
    SwitchThumb(TSTokenValues.LightColors.switchThumb, TSTokenValues.DarkColors.switchThumb),
    SuccessOnInverse(TSTokenValues.LightColors.successOnInverse, TSTokenValues.DarkColors.successOnInverse),
    WarningOnInverse(TSTokenValues.LightColors.warningOnInverse, TSTokenValues.DarkColors.warningOnInverse),
    DangerOnInverse(TSTokenValues.LightColors.dangerOnInverse, TSTokenValues.DarkColors.dangerOnInverse),
    InfoOnInverse(TSTokenValues.LightColors.infoOnInverse, TSTokenValues.DarkColors.infoOnInverse),
    ;

    public fun argb(dark: Boolean): Long = if (dark) this.dark else light
}
