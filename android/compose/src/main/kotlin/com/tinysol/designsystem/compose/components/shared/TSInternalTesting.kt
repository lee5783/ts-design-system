package com.tinysol.designsystem.compose.components.shared

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.semantics.SemanticsPropertyKey
import com.tinysol.designsystem.core.style.TSAvatarContent
import com.tinysol.designsystem.core.style.TSControlState

@Suppress("ComposeCompositionLocalUsage")
internal val LocalTSForcedControlState = staticCompositionLocalOf<TSControlState?> { null }

internal val TSAvatarContentKey = SemanticsPropertyKey<TSAvatarContent>("TSAvatarContent")

internal val TSListItemDividerKey = SemanticsPropertyKey<Boolean>("TSListItemDivider")
