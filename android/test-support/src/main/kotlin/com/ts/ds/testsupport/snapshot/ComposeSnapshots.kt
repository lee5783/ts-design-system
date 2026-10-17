package com.ts.ds.testsupport.snapshot

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File

public fun captureComposeSnapshot(
    file: File,
    variant: SnapshotVariant,
    options: RoborazziOptions = SnapshotEnvironment.roborazziOptions,
    content: @Composable () -> Unit,
) {
    SnapshotEnvironment.apply(variant)
    SnapshotHost.launch().use { host ->
        val composeView = ComposeView(host.activity).apply {
            setContent {
                CompositionLocalProvider(LocalLayoutDirection provides variant.layoutDirection, content = content)
            }
        }
        host.show(composeView, ViewGroup.LayoutParams.MATCH_PARENT)
        composeView.captureRoboImage(file, options)
    }
}
