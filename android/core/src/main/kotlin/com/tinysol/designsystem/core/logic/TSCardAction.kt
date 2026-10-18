package com.tinysol.designsystem.core.logic

import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.style.TSButtonVariant

public class TSCardAction(
    public val label: String,
    public val variant: TSButtonVariant = TSButtonVariant.Secondary,
    public val onClick: () -> Unit,
)

public sealed interface TSCardAccessory {
    public class IconButton(
        public val icon: TSIcon,
        public val label: String,
        public val onClick: () -> Unit,
    ) : TSCardAccessory
}
