package com.ts.ds.demo.compose.shell

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.foundation.TSTouchTarget
import com.ts.ds.compose.foundation.tsFocusRing
import com.ts.ds.compose.theme.TSShapes
import com.ts.ds.compose.theme.TSSpacing
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoTheme

@Composable
fun DemoSettingsSheet(
    settings: DemoSettings,
    onSettingsChange: ((DemoSettings) -> DemoSettings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(DemoR.string.demo_settings_title)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                TSTheme.colors.surfaceElevated,
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TSSpacing.space4, vertical = TSSpacing.space2)
            .semantics { paneTitle = title }
            .testTag(DemoTags.SETTINGS),
        verticalArrangement = Arrangement.spacedBy(TSSpacing.space2),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DemoText(text = title, role = TSTypeRole.Title2, modifier = Modifier.weight(1f))
            DemoIconAction(
                icon = TSIcon.Close,
                contentDescription = stringResource(DemoR.string.demo_action_close),
                onClick = onClose,
                modifier = Modifier.testTag(DemoTags.ACTION_CLOSE_SETTINGS),
            )
        }

        DemoSectionHeader(text = stringResource(DemoR.string.demo_settings_theme))
        OptionGroup {
            DemoTheme.entries.forEach { theme ->
                OptionChip(
                    label = stringResource(theme.labelRes),
                    selected = settings.theme == theme,
                    onClick = { onSettingsChange { it.copy(theme = theme) } },
                    modifier = Modifier.testTag(DemoTags.theme(theme)),
                )
            }
        }

        DemoSectionHeader(text = stringResource(DemoR.string.demo_settings_font_scale))
        OptionGroup {
            DemoSettings.fontScales.forEach { scale ->
                OptionChip(
                    label = stringResource(DemoR.string.demo_settings_font_scale_value, (scale * 100).toInt()),
                    selected = settings.fontScale == scale,
                    onClick = { onSettingsChange { it.copy(fontScale = scale) } },
                    modifier = Modifier.testTag(DemoTags.fontScale(scale)),
                )
            }
        }

        ToggleRow(
            label = stringResource(DemoR.string.demo_settings_rtl),
            checked = settings.rtl,
            onCheckedChange = { checked -> onSettingsChange { it.copy(rtl = checked) } },
            modifier = Modifier.testTag(DemoTags.SETTING_RTL),
        )
        ToggleRow(
            label = stringResource(DemoR.string.demo_settings_reduce_motion),
            checked = settings.reduceMotion,
            onCheckedChange = { checked -> onSettingsChange { it.copy(reduceMotion = checked) } },
            modifier = Modifier.testTag(DemoTags.SETTING_REDUCE_MOTION),
        )

        OptionChip(
            label = stringResource(DemoR.string.demo_action_reset),
            selected = false,
            onClick = { onSettingsChange { DemoSettings() } },
            role = Role.Button,
            modifier = Modifier
                .padding(top = TSSpacing.space2)
                .testTag(DemoTags.ACTION_RESET_SETTINGS),
        )
    }
}

internal val DemoTheme.labelRes: Int
    get() = when (this) {
        DemoTheme.System -> DemoR.string.demo_settings_theme_system
        DemoTheme.Light -> DemoR.string.demo_settings_theme_light
        DemoTheme.Dark -> DemoR.string.demo_settings_theme_dark
    }

@Composable
fun OptionGroup(content: @Composable () -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(TSSpacing.space2),
        verticalArrangement = Arrangement.spacedBy(TSSpacing.space2),
    ) {
        content()
    }
}

@Composable
fun OptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.RadioButton,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = TSTheme.colors
    Box(
        modifier = modifier
            .heightIn(min = TSTouchTarget.MinSize)
            .tsFocusRing(interactionSource, TSShapes.radiusFull, TSTheme.colors.focus)
            .background(if (selected) colors.accentPrimary else colors.surfaceCard, TSShapes.radiusFull)
            .border(1.dp, if (selected) colors.accentPrimary else colors.borderStrong, TSShapes.radiusFull)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = role,
                onClick = onClick,
            )
            .padding(horizontal = TSSpacing.space4, vertical = TSSpacing.space2),
        contentAlignment = Alignment.Center,
    ) {
        DemoText(
            text = label,
            role = TSTypeRole.Label,
            color = if (selected) TSColorRole.TextOnAccent else TSColorRole.TextPrimary,
        )
    }
}

@Composable
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TSTouchTarget.MinSize)
            .tsFocusRing(interactionSource, TSShapes.radiusMd, TSTheme.colors.focus)
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = TSSpacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3),
    ) {
        DemoText(text = label, role = TSTypeRole.BodyLg, modifier = Modifier.weight(1f))
        SwitchIndicator(checked = checked)
    }
}

@Composable
private fun SwitchIndicator(checked: Boolean) {
    val colors = TSTheme.colors
    Box(
        modifier = Modifier
            .width(44.dp)
            .heightIn(min = 26.dp)
            .background(if (checked) colors.accentPrimary else colors.borderStrong, TSShapes.radiusFull)
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = if (checked) 18.dp else 0.dp)
                .size(20.dp)
                .background(colors.switchThumb, TSShapes.radiusFull),
        )
    }
}
