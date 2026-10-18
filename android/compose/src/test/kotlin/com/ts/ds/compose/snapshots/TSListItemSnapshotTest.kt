package com.ts.ds.compose.snapshots

import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.testsupport.snapshot.SnapshotEnvironment
import com.ts.ds.testsupport.snapshot.SnapshotMatrix
import com.ts.ds.testsupport.snapshot.SnapshotVariant
import com.ts.ds.testsupport.snapshot.captureComposeSnapshot
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSListItemSnapshotTest(private val variant: SnapshotVariant) {
    @Test
    fun contents() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "contents", variant), variant) {
            TSTheme(darkTheme = variant.dark) { ListItemContentsSample() }
        }
    }

    @Test
    fun states() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "states", variant), variant) {
            TSTheme(darkTheme = variant.dark) { ListItemStatesSample() }
        }
    }

    @Test
    fun disclosure() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "disclosure", variant), variant) {
            TSTheme(darkTheme = variant.dark) { ListItemDisclosureSample() }
        }
    }

    @Test
    fun divider() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "divider", variant), variant) {
            TSTheme(darkTheme = variant.dark) { ListItemDividerSample() }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
