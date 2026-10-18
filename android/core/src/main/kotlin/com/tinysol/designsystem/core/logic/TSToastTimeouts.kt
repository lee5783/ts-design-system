package com.tinysol.designsystem.core.logic

public object TSToastTimeouts {
    public const val DEFAULT_MILLIS: Long = 4_000L
    public const val EXTENDED_MILLIS: Long = 10_000L

    public val default: (TSToastMessage) -> Long? = { message ->
        if (message.hasAction) EXTENDED_MILLIS else DEFAULT_MILLIS
    }
}
