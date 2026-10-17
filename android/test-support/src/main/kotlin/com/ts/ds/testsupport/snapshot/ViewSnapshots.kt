package com.ts.ds.testsupport.snapshot

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File

public fun captureViewSnapshot(
    file: File,
    variant: SnapshotVariant,
    @StyleRes theme: Int,
    options: RoborazziOptions = SnapshotEnvironment.roborazziOptions,
    content: (Context) -> View,
) {
    SnapshotEnvironment.apply(variant)
    SnapshotHost.launch().use { host ->
        val view = content(ContextThemeWrapper(host.activity, theme))
        host.show(view, ViewGroup.LayoutParams.MATCH_PARENT)
        view.captureRoboImage(file, options)
    }
}
