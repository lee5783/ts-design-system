package com.ts.ds.core.style

import com.ts.ds.tokens.TSTokenValues
import com.ts.ds.tokens.TSTypeValues

public enum class TSTypeRole(
    public val values: TSTypeValues,
) {
    Display(TSTokenValues.Typography.display),
    Title1(TSTokenValues.Typography.title1),
    Title2(TSTokenValues.Typography.title2),
    Title3(TSTokenValues.Typography.title3),
    BodyLg(TSTokenValues.Typography.bodyLg),
    Body(TSTokenValues.Typography.body),
    BodySm(TSTokenValues.Typography.bodySm),
    Label(TSTokenValues.Typography.label),
    Caption(TSTokenValues.Typography.caption),
    Overline(TSTokenValues.Typography.overline),
    Code(TSTokenValues.Typography.code),
}
