package com.ts.ds.testsupport.snapshot

import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt

public data class SnapshotVariant(
    val dark: Boolean,
    val fontScale: Float,
    val rtl: Boolean,
) {
    public val name: String
        get() = listOf(
            if (dark) "dark" else "light",
            "font${(fontScale * 100).roundToInt()}",
            if (rtl) "rtl" else "ltr",
        ).joinToString("_")

    public val layoutDirection: LayoutDirection
        get() = if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    override fun toString(): String = name
}

public object SnapshotMatrix {
    public const val DEFAULT_FONT_SCALE: Float = 1f
    public const val LARGE_FONT_SCALE: Float = 2f

    public val standard: List<SnapshotVariant> = listOf(false, true).flatMap { dark ->
        listOf(DEFAULT_FONT_SCALE, LARGE_FONT_SCALE).flatMap { fontScale ->
            listOf(false, true).map { rtl -> SnapshotVariant(dark, fontScale, rtl) }
        }
    }

    public val sdkSubset: List<SnapshotVariant> = listOf(
        SnapshotVariant(dark = false, fontScale = DEFAULT_FONT_SCALE, rtl = false),
        SnapshotVariant(dark = false, fontScale = LARGE_FONT_SCALE, rtl = false),
    )

    public fun parameters(variants: List<SnapshotVariant>): List<Array<Any>> = variants.map { arrayOf(it) }
}
