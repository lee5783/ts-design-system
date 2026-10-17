package com.ts.ds.compose.snapshots

import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.style.TSButtonVariant
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
class TSButtonSnapshotTest(private val variant: SnapshotVariant) {
    private fun button(buttonVariant: TSButtonVariant) {
        val name = "button_${buttonVariant.name.lowercase()}"
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, name, variant), variant) {
            TSTheme(darkTheme = variant.dark) { ButtonGallerySample(buttonVariant) }
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

    @Test
    fun iconButton() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "icon_button", variant), variant) {
            TSTheme(darkTheme = variant.dark) { IconButtonGallerySample() }
        }
    }

    @Test
    fun icon() {
        captureComposeSnapshot(SnapshotEnvironment.file(javaClass, "icon", variant), variant) {
            TSTheme(darkTheme = variant.dark) { IconGallerySample() }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = SnapshotMatrix.parameters(SnapshotMatrix.standard)
    }
}
