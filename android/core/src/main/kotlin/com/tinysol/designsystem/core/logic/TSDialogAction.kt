package com.tinysol.designsystem.core.logic

import com.tinysol.designsystem.core.style.TSButtonVariant

public class TSDialogAction(
    public val label: String,
    public val variant: TSButtonVariant = TSButtonVariant.Secondary,
    public val dismisses: Boolean = true,
    public val onClick: () -> Unit = {},
)

public object TSDialogLayout {
    public fun orderedActions(actions: List<TSDialogAction>, primaryFirst: Boolean): List<TSDialogAction> =
        if (primaryFirst) actions.reversed() else actions
}
