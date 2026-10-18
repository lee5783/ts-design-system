package com.tinysol.designsystem.compose.foundation

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.core.haptics.DsHapticEvent
import com.tinysol.designsystem.core.haptics.DsHaptics

@Stable
public class TSHaptics internal constructor(
    private val view: View,
    public val enabled: Boolean,
) {
    public fun perform(event: DsHapticEvent): Boolean =
        enabled && view.performHapticFeedback(DsHaptics.constantFor(event))
}

@Composable
public fun rememberTSHaptics(): TSHaptics {
    val view = LocalView.current
    val enabled = TSTheme.hapticsEnabled
    return remember(view, enabled) { TSHaptics(view, enabled) }
}
