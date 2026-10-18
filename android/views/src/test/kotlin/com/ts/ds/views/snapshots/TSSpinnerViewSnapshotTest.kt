package com.ts.ds.views.snapshots

import android.content.Context
import android.view.Gravity
import android.view.View
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSSpinnerSize
import com.ts.ds.core.style.TSSpinnerTone
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.testsupport.snapshot.SnapshotEnvironment
import com.ts.ds.testsupport.snapshot.SnapshotMatrix
import com.ts.ds.testsupport.snapshot.SnapshotVariant
import com.ts.ds.testsupport.snapshot.captureViewSnapshot
import com.ts.ds.views.R
import com.ts.ds.views.components.spinner.TSSpinnerView
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

internal fun spinnerGallerySample(context: Context): View = context.galleryColumn().apply {
    TSSpinnerTone.entries.forEach { tone ->
        addView(
            context.galleryRow().apply {
                gravity = Gravity.CENTER_VERTICAL
                TSSpinnerSize.entries.forEach { size ->
                    addView(
                        TSSpinnerView(context).apply {
                            this.size = size
                            this.tone = tone
                        },
                    )
                }
                addView(
                    MaterialTextView(context).apply {
                        setTextAppearance(TSTypeRole.BodySm.textAppearance)
                        setTextColor(context.tsColor(TSColorRole.TextMuted))
                        text = tone.name
                    },
                )
            },
        )
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSSpinnerViewSnapshotTest(private val variant: SnapshotVariant) {
    @Test
    fun gallery() {
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, "gallery", variant), variant, R.style.Theme_TS, content = ::spinnerGallerySample)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
