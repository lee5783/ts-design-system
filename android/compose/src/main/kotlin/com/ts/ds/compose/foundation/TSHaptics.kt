package com.ts.ds.compose.foundation

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics

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
