package com.ts.ds.core.style

public enum class TSTextColor(
    public val role: TSColorRole,
) {
    Primary(TSColorRole.TextPrimary),
    Secondary(TSColorRole.TextSecondary),
    Muted(TSColorRole.TextMuted),
    Disabled(TSColorRole.TextDisabled),
    Inverse(TSColorRole.TextInverse),
    OnAccent(TSColorRole.TextOnAccent),
}
