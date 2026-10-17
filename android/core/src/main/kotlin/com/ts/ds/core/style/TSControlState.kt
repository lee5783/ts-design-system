package com.ts.ds.core.style

public data class TSControlState(
    public val enabled: Boolean = true,
    public val pressed: Boolean = false,
    public val focused: Boolean = false,
    public val selected: Boolean = false,
    public val error: Boolean = false,
    public val loading: Boolean = false,
) {
    public val interactive: Boolean
        get() = enabled && !loading

    public val dimmed: Boolean
        get() = !enabled && !loading

    public companion object {
        public val Default: TSControlState = TSControlState()
        public val Pressed: TSControlState = TSControlState(pressed = true)
        public val Disabled: TSControlState = TSControlState(enabled = false)
        public val Loading: TSControlState = TSControlState(loading = true)
        public val Focused: TSControlState = TSControlState(focused = true)

        public fun make(
            pressed: Boolean = false,
            enabled: Boolean = true,
            loading: Boolean = false,
            focused: Boolean = false,
        ): TSControlState = TSControlState(
            enabled = enabled || loading,
            pressed = pressed && enabled && !loading,
            focused = focused && enabled,
            loading = loading,
        )
    }
}
