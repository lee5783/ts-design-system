package com.ts.ds.demo.compose.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.ts.ds.compose.foundation.tsFocusRing
import com.ts.ds.compose.foundation.tsMinimumTouchTarget
import com.ts.ds.compose.theme.TSShapes
import com.ts.ds.compose.theme.TSSize
import com.ts.ds.compose.theme.TSSpacing
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.color
import com.ts.ds.compose.theme.style
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.demo.support.R as DemoR

@Composable
fun DemoText(
    text: String,
    modifier: Modifier = Modifier,
    role: TSTypeRole = TSTypeRole.Body,
    color: TSColorRole = TSColorRole.TextPrimary,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TSTheme.typography.style(role).copy(color = TSTheme.colors.color(color)),
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
    )
}

@Composable
fun DemoIcon(
    icon: TSIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = TSTheme.colors.textPrimary,
    size: Dp = TSSize.iconMd,
) {
    Image(
        painter = painterResource(icon.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(tint),
    )
}

@Composable
fun DemoIconAction(
    icon: TSIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .tsMinimumTouchTarget()
            .tsFocusRing(interactionSource, TSShapes.radiusFull, TSTheme.colors.focus)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        DemoIcon(icon = icon, contentDescription = contentDescription)
    }
}

@Composable
fun DemoTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .heightIn(min = TSSize.navBar)
            .padding(horizontal = TSSpacing.space1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            DemoIconAction(
                icon = TSIcon.ChevronLeft,
                contentDescription = stringResource(DemoR.string.demo_action_back),
                onClick = onBack,
                modifier = Modifier.testTag(DemoTags.ACTION_BACK),
            )
        }
        DemoText(
            text = title,
            role = TSTypeRole.Title2,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = TSSpacing.space3)
                .testTag(DemoTags.SCREEN_TITLE)
                .semantics { heading() },
        )
        LocalDemoThemeControl.current?.let { DemoThemeAction(it) }
        actions()
    }
}

@Composable
fun DemoScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: LazyListScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DemoTopBar(title = title, onBack = onBack, actions = actions)
        val insets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal).asPaddingValues()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = TSSpacing.space4,
                end = TSSpacing.space4,
                top = TSSpacing.space2,
                bottom = TSSpacing.space8 + insets.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(TSSpacing.space3),
            content = content,
        )
    }
}

@Composable
fun DemoSectionHeader(text: String, modifier: Modifier = Modifier) {
    DemoText(
        text = text.uppercase(),
        role = TSTypeRole.Overline,
        color = TSColorRole.TextSecondary,
        modifier = modifier
            .padding(top = TSSpacing.space4, bottom = TSSpacing.space1)
            .semantics { heading() },
    )
}

@Composable
fun DemoTokenRow(
    name: String,
    value: String,
    modifier: Modifier = Modifier,
    preview: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TSTheme.colors.surfaceCard, TSShapes.radiusMd)
            .padding(TSSpacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3),
    ) {
        preview()
        Column(modifier = Modifier.weight(1f)) {
            DemoText(text = name, role = TSTypeRole.Label)
            DemoText(text = value, role = TSTypeRole.Code, color = TSColorRole.TextSecondary)
        }
    }
}

fun Modifier.dismissOnTap(onDismiss: () -> Unit): Modifier =
    pointerInput(onDismiss) { detectTapGestures { onDismiss() } }
