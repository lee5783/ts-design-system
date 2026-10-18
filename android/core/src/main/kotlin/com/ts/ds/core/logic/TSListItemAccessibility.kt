package com.ts.ds.core.logic

import android.content.Context
import com.ts.ds.core.R

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

public object TSListItemAccessibility {
    public fun label(title: CharSequence, subtitle: CharSequence?): String =
        if (subtitle.isNullOrEmpty()) title.toString() else "$title, $subtitle"

    public fun hint(context: Context, role: TSListItemRole): String? =
        if (role == TSListItemRole.Navigation) context.getString(R.string.ts_list_item_navigates) else null

    public fun expansionState(context: Context, expanded: Boolean): String =
        context.getString(if (expanded) R.string.ts_list_item_expanded else R.string.ts_list_item_collapsed)
}
