package com.ts.ds.core.logic

import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSButtonVariant

public class TSCardAction(
    public val label: String,
    public val variant: TSButtonVariant = TSButtonVariant.Secondary,
    public val onClick: () -> Unit,
)

public sealed interface TSCardAccessory {
    public val customAction: TSCardAction

    public class IconButton(
        public val icon: TSIcon,
        public val label: String,
        public val onClick: () -> Unit,
    ) : TSCardAccessory {
        override val customAction: TSCardAction
            get() = TSCardAction(label, onClick = onClick)
    }
}

public object TSCardAccessibility {
    public fun label(title: CharSequence?, subtitle: CharSequence?, body: CharSequence? = null): String {
        val heading = heading(title, subtitle)
        return heading.ifEmpty { body?.toString().orEmpty() }
    }

    public fun value(title: CharSequence?, subtitle: CharSequence?, body: CharSequence?): String? {
        if (heading(title, subtitle).isEmpty() || body.isNullOrEmpty()) return null
        return body.toString()
    }

    public fun customActions(
        footer: List<TSCardAction>,
        additional: List<TSCardAction>,
        enabled: Boolean = true,
        accessory: TSCardAccessory? = null,
    ): List<TSCardAction> = if (enabled) footer + listOfNotNull(accessory?.customAction) + additional else emptyList()

    private fun heading(title: CharSequence?, subtitle: CharSequence?): String =
        listOfNotNull(title, subtitle)
            .filter { it.isNotEmpty() }
            .joinToString(", ")
}
