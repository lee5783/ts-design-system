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
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSSwitchSdkSnapshotTest(private val variant: SnapshotVariant) {
    @Test
    fun states() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "states", variant), variant) {
            TSTheme(darkTheme = variant.dark) { SwitchStatesSample() }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.sdkSubset)
    }
}
