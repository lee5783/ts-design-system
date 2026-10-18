package com.tinysol.designsystem.compose.components.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.tinysol.designsystem.compose.components.icon.TSIconImage
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.core.style.TSTextFieldStyleSpec

internal object TSTextFieldTestTags {
    const val PASSWORD_TOGGLE = "ts.text-field.password-toggle"
}

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

    Box(
        modifier = modifier
            .clickable(interactionSource = null, indication = null, enabled = enabled, onClick = onToggle)
            .testTag(TSTextFieldTestTags.PASSWORD_TOGGLE)
            .size(toggle.sizeDp.dp)
            .background(toggle.fill?.let(colors::color) ?: Color.Transparent, shape),
        contentAlignment = Alignment.Center,
    ) {
        TSIconImage(toggle.icon, contentDescription = null, size = toggle.iconSizeDp.dp, tint = colors.color(toggle.iconColor))
    }
}
