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
class TSDialogSnapshotTest(private val variant: SnapshotVariant) {
    @Test
    fun center() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "center", variant), variant) {
            TSTheme(darkTheme = variant.dark) { DialogCenterSample() }
        }
    }

    @Test
    fun sheet() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "sheet", variant), variant) {
            TSTheme(darkTheme = variant.dark) { DialogSheetSample() }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
