package com.tinysol.designsystem.compose.components.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinysol.designsystem.compose.components.icon.TSIconImage
import com.tinysol.designsystem.compose.components.shared.TSAvatarContentKey
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.color
import com.tinysol.designsystem.compose.theme.withDsFontWeight
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.logic.TSAvatarImage
import com.tinysol.designsystem.core.logic.TSInitials
import com.tinysol.designsystem.core.style.TSAvatarContent
import com.tinysol.designsystem.core.style.TSAvatarSize
import com.tinysol.designsystem.core.style.TSAvatarStyleSpec
import com.tinysol.designsystem.core.style.resolve

@Composable
public fun TSAvatar(
    name: String,
    modifier: Modifier = Modifier,
    image: Painter? = null,
    size: TSAvatarSize = TSAvatarSize.Md,
) {
    val initials = remember(name) { TSInitials.from(name) }
    val usableImage = image?.takeIf { it.isUsableAvatarImage() }
    val content = TSAvatarContent.make(initials, hasImage = usableImage != null)
    val resolved = TSAvatarStyleSpec.resolve(TSAvatarStyleSpec.Configuration(size, content))
    val colors = TSTheme.colors
    val foreground = colors.color(resolved.foreground)

    Box(
        modifier = modifier
            .semantics { tsAvatarContent = content }
            .size(resolved.diameterDp.dp)
            .clip(CircleShape)
            .background(colors.color(resolved.fill)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            usableImage != null && resolved.showsImage -> Image(
                painter = usableImage,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
            resolved.showsInitials -> AvatarInitials(initials, resolved)
            resolved.showsPlaceholder -> TSIconImage(
                TSIcon.User,
                contentDescription = null,
                size = resolved.placeholderIconSizeDp.dp,
                tint = foreground,
            )
        }
    }
}

@Composable
private fun AvatarInitials(initials: String, resolved: TSAvatarStyleSpec.Resolved) {
    val density = LocalDensity.current
    val maxFontSize = with(density) { resolved.initialsFontSizeDp.dp.toSp() }
    val minFontSize = with(density) { (resolved.initialsFontSizeDp * resolved.initialsMinimumScaleFactor).dp.toSp() }
    val style = TextStyle(
        color = TSTheme.colors.color(resolved.foreground),
        fontSize = maxFontSize,
        lineHeight = maxFontSize,
        textAlign = TextAlign.Center,
    ).withDsFontWeight(resolved.initialsFontWeight)
    BasicText(
        text = initials,
        modifier = Modifier.padding(resolved.contentInsetDp.dp),
        style = style,
        maxLines = 1,
        softWrap = false,
        autoSize = TextAutoSize.StepBased(minFontSize = minFontSize, maxFontSize = maxFontSize, stepSize = AUTO_SIZE_STEP),
    )
}

private var SemanticsPropertyReceiver.tsAvatarContent by TSAvatarContentKey

internal fun Painter.isUsableAvatarImage(): Boolean {
    val size = intrinsicSize
    return size.isUnspecified || TSAvatarImage.usableSize(size.width, size.height)
}

private val AUTO_SIZE_STEP = 0.5.sp
