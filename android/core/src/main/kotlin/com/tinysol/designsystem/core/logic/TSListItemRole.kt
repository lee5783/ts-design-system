package com.tinysol.designsystem.core.logic

public enum class TSListItemRole {
    Plain,
    Action,
    Navigation,
    Toggle,
    ;

    public val isPressable: Boolean
        get() = this == Action || this == Navigation

    public companion object {
        public fun make(hasAction: Boolean, showsChevron: Boolean, hasToggle: Boolean): TSListItemRole = when {
            hasToggle -> Toggle
            !hasAction -> Plain
            showsChevron -> Navigation
            else -> Action
        }
    }
}
