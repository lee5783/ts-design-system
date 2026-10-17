package com.ts.ds.core.style

import com.ts.ds.tokens.TSShadowValues
import com.ts.ds.tokens.TSTokenValues

public enum class TSShadowRole(
    public val values: TSShadowValues,
) {
    Xs(TSTokenValues.Shadow.shadowXs),
    Sm(TSTokenValues.Shadow.shadowSm),
    Md(TSTokenValues.Shadow.shadowMd),
    Lg(TSTokenValues.Shadow.shadowLg),
}
