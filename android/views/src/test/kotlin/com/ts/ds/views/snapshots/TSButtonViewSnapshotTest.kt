package com.ts.ds.views.snapshots

import com.ts.ds.core.style.TSButtonVariant
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
class TSButtonViewSnapshotTest(private val variant: SnapshotVariant) {
    private fun button(buttonVariant: TSButtonVariant) {
        val name = "button_${buttonVariant.name.lowercase()}"
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, name, variant), variant, R.style.Theme_TS) {
            buttonGallerySample(it, buttonVariant)
        }
    }

    @Test
    fun primary() = button(TSButtonVariant.Primary)

    @Test
    fun secondary() = button(TSButtonVariant.Secondary)

    @Test
    fun ghost() = button(TSButtonVariant.Ghost)

    @Test
    fun danger() = button(TSButtonVariant.Danger)

    @Test
    fun link() = button(TSButtonVariant.Link)

    private fun customIcon(buttonVariant: TSButtonVariant) {
        val name = "button_custom_icon_${buttonVariant.name.lowercase()}"
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, name, variant), variant, R.style.Theme_TS) {
            buttonCustomIconSample(it, buttonVariant)
        }
    }

    @Test
    fun primaryCustomIcon() = customIcon(TSButtonVariant.Primary)

    @Test
    fun secondaryCustomIcon() = customIcon(TSButtonVariant.Secondary)

    @Test
    fun ghostCustomIcon() = customIcon(TSButtonVariant.Ghost)

    @Test
    fun dangerCustomIcon() = customIcon(TSButtonVariant.Danger)

    @Test
    fun linkCustomIcon() = customIcon(TSButtonVariant.Link)

    @Test
    fun iconButtonCustomIcon() {
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, "icon_button_custom_icon", variant), variant, R.style.Theme_TS, content = ::iconButtonCustomIconSample)
    }

    @Test
    fun iconButton() {
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, "icon_button", variant), variant, R.style.Theme_TS, content = ::iconButtonGallerySample)
    }

    @Test
    fun icon() {
        captureViewSnapshot(SnapshotEnvironment.file(javaClass, "icon", variant), variant, R.style.Theme_TS, content = ::iconGallerySample)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
