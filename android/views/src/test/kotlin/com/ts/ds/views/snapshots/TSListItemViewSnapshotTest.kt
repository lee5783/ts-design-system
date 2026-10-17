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
class TSListItemViewSnapshotTest(private val variant: SnapshotVariant) {
    @Test
    fun contents() {
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, "contents", variant), variant, R.style.Theme_TS, content = ::listItemContentsSample)
    }

    @Test
    fun states() {
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, "states", variant), variant, R.style.Theme_TS, content = ::listItemStatesSample)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
