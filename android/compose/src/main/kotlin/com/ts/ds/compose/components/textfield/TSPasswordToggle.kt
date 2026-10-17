package com.ts.ds.compose.components.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.icon.TSIconImage
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.core.R as CoreR
import com.ts.ds.core.style.TSTextFieldStyleSpec

@Composable
internal fun TSPasswordToggle(
    revealed: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val toggle = TSTextFieldStyleSpec.passwordToggle(revealed)
    val colors = TSTheme.colors
    val shape = RoundedCornerShape(toggle.cornerRadiusDp.dp)
    val label = stringResource(if (revealed) CoreR.string.ts_text_field_hide_password else CoreR.string.ts_text_field_show_password)

    Box(
        modifier = modifier
            .clickable(interactionSource = null, indication = null, enabled = enabled, role = Role.Button, onClick = onToggle)
            .semantics {
                contentDescription = label
                selected = toggle.selected
            }
            .size(toggle.sizeDp.dp)
            .background(toggle.fill?.let(colors::color) ?: Color.Transparent, shape),
        contentAlignment = Alignment.Center,
    ) {
        TSIconImage(toggle.icon, contentDescription = null, size = toggle.iconSizeDp.dp, tint = colors.color(toggle.iconColor))
    }
}
