package com.ts.ds.testsupport.snapshot

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SnapshotEnvironmentTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val darkRtlLarge = SnapshotVariant(dark = true, fontScale = 2f, rtl = true)

    private val recordOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)

    @Test
    fun qualifiersFollowResourceQualifierOrder() {
        assertEquals("ar-rEG-ldrtl-w360dp-h2000dp-night-xhdpi", SnapshotEnvironment.qualifiers(darkRtlLarge))
        assertEquals(
            "en-rUS-ldltr-w360dp-h2000dp-notnight-xhdpi",
            SnapshotEnvironment.qualifiers(SnapshotVariant(dark = false, fontScale = 1f, rtl = false)),
        )
    }

    @Test
    fun applyConfiguresNightRtlFontScaleAndDensity() {
        SnapshotEnvironment.apply(darkRtlLarge)
        val configuration = ApplicationProvider.getApplicationContext<Context>().resources.configuration
        assertEquals(Configuration.UI_MODE_NIGHT_YES, configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        assertEquals(View.LAYOUT_DIRECTION_RTL, configuration.layoutDirection)
        assertEquals(2f, configuration.fontScale, 0f)
        assertEquals(320, configuration.densityDpi)
        assertEquals(360, configuration.screenWidthDp)
    }

    @Test
    fun snapshotFileNameCarriesVariantAndSdk() {
        val file = SnapshotEnvironment.file(SnapshotEnvironmentTest::class.java, "sample", darkRtlLarge)
        assertEquals(
            "src/test/snapshots/SnapshotEnvironmentTest/sample_dark_font200_rtl_sdk${Build.VERSION.SDK_INT}.png",
            file.path,
        )
    }

    @Test
    fun composeHostAppliesVariantLocals() {
        var layoutDirection: LayoutDirection? = null
        var fontScale = 0f
        var night = false
        val file = temporaryFolder.root.resolve("compose.png")
        captureComposeSnapshot(file, darkRtlLarge, recordOptions) {
            layoutDirection = LocalLayoutDirection.current
            fontScale = LocalDensity.current.fontScale
            night = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            Layout(content = {}) { _, _ -> layout(8, 8) {} }
        }
        assertEquals(LayoutDirection.Rtl, layoutDirection)
        assertEquals(2f, fontScale, 0f)
        assertTrue(night)
        assertTrue(file.isFile)
    }

    @Test
    fun viewHostResolvesRtlLayoutDirection() {
        var resolved = View.LAYOUT_DIRECTION_LTR
        val file = temporaryFolder.root.resolve("view.png")
        captureViewSnapshot(file, darkRtlLarge, android.R.style.Theme_DeviceDefault, recordOptions) { context ->
            object : FrameLayout(context) {
                override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
                    super.onLayout(changed, left, top, right, bottom)
                    resolved = layoutDirection
                }
            }.apply { minimumHeight = 8 }
        }
        assertEquals(View.LAYOUT_DIRECTION_RTL, resolved)
        assertTrue(file.isFile)
    }

    @Test
    fun contentTallerThanTheWindowFailsInsteadOfClipping() {
        val file = temporaryFolder.root.resolve("tall.png")
        assertThrows(IllegalStateException::class.java) {
            captureViewSnapshot(file, darkRtlLarge, android.R.style.Theme_DeviceDefault, recordOptions) { context ->
                View(context).apply { minimumHeight = SnapshotEnvironment.HEIGHT_DP * 4 }
            }
        }
    }
}
