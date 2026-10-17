package com.ts.ds.views.snapshots

import com.ts.ds.testsupport.snapshot.SnapshotEnvironment
import com.ts.ds.testsupport.snapshot.SnapshotMatrix
import com.ts.ds.testsupport.snapshot.SnapshotVariant
import com.ts.ds.testsupport.snapshot.captureViewSnapshot
import com.ts.ds.views.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSDialogViewSnapshotTest(private val variant: SnapshotVariant) {
    @Test
    fun center() {
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, "center", variant), variant, R.style.Theme_TS, content = ::dialogCenterSample)
    }

    @Test
    fun sheet() {
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, "sheet", variant), variant, R.style.Theme_TS, content = ::dialogSheetSample)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
