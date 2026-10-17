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
class TSTabsSnapshotTest(private val variant: SnapshotVariant) {
    @Test
    fun underline() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "underline", variant), variant) {
            TSTheme(darkTheme = variant.dark) { UnderlineTabsSample() }
        }
    }

    @Test
    fun segmented() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "segmented", variant), variant) {
            TSTheme(darkTheme = variant.dark) { SegmentedTabsSample() }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
