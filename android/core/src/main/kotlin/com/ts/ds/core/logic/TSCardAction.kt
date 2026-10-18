package com.ts.ds.core.logic

import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSButtonVariant

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
