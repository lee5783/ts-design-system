package com.ts.ds.compose.components.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.password
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.icon.TSIconImage
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.components.shared.withContentDirectionAtLayoutStart
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.shadow
import com.ts.ds.compose.theme.style
import com.ts.ds.compose.theme.withDsFontWeight
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.input.TSTextInputTraits
import com.ts.ds.core.style.TSTextFieldContentStyle
import com.ts.ds.core.style.TSTextFieldStyleSpec

@Composable
public fun TSTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    hint: String? = null,
    error: String? = null,
    leadingIcon: TSIcon? = null,
    secure: Boolean = false,
    multiline: Boolean = false,
    readOnly: Boolean = false,
    inputTraits: TSTextInputTraits = TSTextInputTraits.Default,
    onSubmit: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    TSTextField(
        value = value,
        label = label,
        onValueChange = onValueChange,
        contentStyle = TSTextFieldContentStyle.Default,
        modifier = modifier,
        placeholder = placeholder,
        hint = hint,
        error = error,
        leadingIcon = leadingIcon,
        secure = secure,
        multiline = multiline,
        readOnly = readOnly,
        inputTraits = inputTraits,
        onSubmit = onSubmit,
        enabled = enabled,
        interactionSource = interactionSource,
        trailing = trailing,
    )
}

@Composable
public fun TSTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    contentStyle: TSTextFieldContentStyle,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    hint: String? = null,
    error: String? = null,
    leadingIcon: TSIcon? = null,
    secure: Boolean = false,
    multiline: Boolean = false,
    readOnly: Boolean = false,
    inputTraits: TSTextInputTraits = TSTextInputTraits.Default,
    onSubmit: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val forced = LocalTSForcedControlState.current
    val hasError = !error.isNullOrBlank()
    val state = TSTextFieldStyleSpec.state(enabled = enabled, focused = forced?.focused ?: focused, error = hasError)
    val resolved = TSTextFieldStyleSpec.resolve(TSTextFieldStyleSpec.Configuration(readOnly, contentStyle), state)
    var revealed by rememberSaveable { mutableStateOf(forced?.selected ?: false) }
    val isSecure = secure && !multiline
    val masked = isSecure && (readOnly || !revealed)
    val colors = TSTheme.colors
    val typography = TSTheme.typography
    val shape = RoundedCornerShape(resolved.cornerRadiusDp.dp)
    val elevation = resolved.shadow?.let { TSTheme.elevation.shadow(it) }
    val inputStyle = typography.style(resolved.inputTypeRole).copy(color = colors.color(resolved.inputColor))
    val visibleHint = hint?.takeIf { it.isNotBlank() && !hasError }
    val contentType = inputTraits.contentType()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.semantics {
            if (masked) password()
            if (contentType != null) this.contentType = contentType
        },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = inputStyle,
        keyboardOptions = inputTraits.keyboardOptions(masked = isSecure && !revealed),
        keyboardActions = inputTraits.keyboardActions(onSubmit),
        singleLine = !multiline,
        maxLines = if (multiline) TSTextFieldStyleSpec.MAXIMUM_LINES else 1,
        minLines = if (multiline) TSTextFieldStyleSpec.MINIMUM_LINES else 1,
        visualTransformation = if (masked) TSMaskTransformation else VisualTransformation.None,
        interactionSource = source,
        cursorBrush = SolidColor(colors.accentPrimary),
        decorationBox = { innerTextField ->
            Column(modifier = Modifier.fillMaxWidth()) {
                BasicText(
                    text = label,
                    style = typography.style(resolved.labelTypeRole)
                        .withDsFontWeight(resolved.labelFontWeight)
                        .copy(color = colors.color(resolved.labelColor))
                        .withContentDirectionAtLayoutStart(),
                    modifier = Modifier.padding(bottom = resolved.labelSpacingDp.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(resolved.opacity)
                        .then(
                            if (elevation != null) {
                                Modifier.shadow(elevation.elevation, shape, clip = false, ambientColor = elevation.color, spotColor = elevation.color)
                            } else {
                                Modifier
                            },
                        )
                        .background(colors.color(resolved.fill), shape)
                        .border(resolved.borderWidthDp.dp, colors.color(resolved.border), shape)
                        .defaultMinSize(minHeight = resolved.minHeightDp.dp)
                        .padding(horizontal = resolved.horizontalPaddingDp.dp),
                    horizontalArrangement = Arrangement.spacedBy(resolved.spacingDp.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingIcon != null) {
                        TSIconImage(
                            leadingIcon,
                            contentDescription = null,
                            size = resolved.leadingIconSizeDp.dp,
                            tint = colors.color(resolved.leadingIconColor),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = resolved.verticalPaddingDp.dp),
                    ) {
                        if (value.isEmpty() && placeholder != null) {
                            BasicText(
                                text = placeholder,
                                style = inputStyle.copy(color = colors.color(resolved.placeholderColor)),
                                maxLines = if (multiline) TSTextFieldStyleSpec.MAXIMUM_LINES else 1,
                            )
                        }
                        innerTextField()
                    }
                    if (trailing != null) trailing()
                    if (isSecure && !readOnly) {
                        TSPasswordToggle(revealed = revealed, onToggle = { revealed = !revealed }, enabled = enabled)
                    }
                }
                TSTextFieldMessage(
                    text = if (hasError) error else visibleHint,
                    resolved = resolved,
                )
            }
        },
    )
}

@Composable
private fun TSTextFieldMessage(
    text: String?,
    resolved: TSTextFieldStyleSpec.Resolved,
) {
    if (text == null) return
    val color = TSTheme.colors.color(resolved.messageColor)
    Row(
        modifier = Modifier
            .padding(top = resolved.messageSpacingDp.dp),
        horizontalArrangement = Arrangement.spacedBy(resolved.messageSpacingDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (resolved.showsErrorIcon) {
            TSIconImage(TSIcon.Alert, contentDescription = null, size = resolved.messageIconSizeDp.dp, tint = color)
        }
        BasicText(text = text, style = TSTheme.typography.style(resolved.messageTypeRole).copy(color = color).withContentDirectionAtLayoutStart())
    }
}

private object TSMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(AnnotatedString(TSTextFieldStyleSpec.maskedText(text.text, secure = true)), OffsetMapping.Identity)
}
