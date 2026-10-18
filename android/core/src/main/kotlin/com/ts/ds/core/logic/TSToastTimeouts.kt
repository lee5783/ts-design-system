package com.ts.ds.core.logic

public object TSToastTimeouts {
    public const val DEFAULT_MILLIS: Long = 4_000L

    public val default: (TSToastMessage) -> Long? = { DEFAULT_MILLIS }
}
