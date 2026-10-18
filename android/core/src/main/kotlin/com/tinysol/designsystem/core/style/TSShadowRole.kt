package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSShadowValues
import com.tinysol.designsystem.tokens.TSTokenValues

public enum class TSShadowRole(
    public val values: TSShadowValues,
) {
    Xs(TSTokenValues.Shadow.shadowXs),
    Sm(TSTokenValues.Shadow.shadowSm),
    Md(TSTokenValues.Shadow.shadowMd),
    Lg(TSTokenValues.Shadow.shadowLg),
}
