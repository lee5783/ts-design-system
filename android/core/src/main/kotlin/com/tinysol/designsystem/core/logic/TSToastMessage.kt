package com.tinysol.designsystem.core.logic

public enum class TSToastTone {
    Neutral,
    Success,
    Warning,
    Danger,
    Info,
}

public class TSToastMessage(
    public val text: String,
    public val tone: TSToastTone = TSToastTone.Neutral,
    public val actionLabel: String? = null,
    public val onAction: (() -> Unit)? = null,
) {
    public val hasAction: Boolean
        get() = actionLabel != null
}

public class TSToastEntry internal constructor(
    public val id: Long,
    public val message: TSToastMessage,
    public val timeoutMillis: Long?,
) {
    public val autoDismisses: Boolean
        get() = timeoutMillis != null
}
