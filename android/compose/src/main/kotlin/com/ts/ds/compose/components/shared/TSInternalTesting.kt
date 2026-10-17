package com.ts.ds.compose.components.shared

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.semantics.SemanticsPropertyKey
import com.ts.ds.core.style.TSAvatarContent
import com.ts.ds.core.style.TSControlState

@Suppress("ComposeCompositionLocalUsage")
internal val LocalTSForcedControlState = staticCompositionLocalOf<TSControlState?> { null }

internal val TSAvatarContentKey = SemanticsPropertyKey<TSAvatarContent>("TSAvatarContent")

internal val TSListItemDividerKey = SemanticsPropertyKey<Boolean>("TSListItemDivider")
