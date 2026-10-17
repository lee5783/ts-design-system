package com.ts.ds.views.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ts.ds.views.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.appcompat.R as AppCompatR
import com.google.android.material.R as MaterialR

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSMaterialSurfaceTest {
    private val modes = listOf("+notnight", "+night")

    private fun themed(theme: Int): Context =
        ContextThemeWrapper(ApplicationProvider.getApplicationContext<Context>(), theme)

    private fun overlaid(): Context =
        ContextThemeWrapper(themed(MaterialR.style.Theme_Material3_DayNight_NoActionBar), R.style.ThemeOverlay_TS)

    private fun eachContext(block: (String, Context) -> Unit) {
        modes.forEach { mode ->
            RuntimeEnvironment.setQualifiers(mode)
            block("$mode Theme.TS", themed(R.style.Theme_TS))
            block("$mode ThemeOverlay.TS", overlaid())
        }
    }

    private fun Drawable.centerPixel(width: Int = 400, height: Int = 400): Int {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        setBounds(0, 0, width, height)
        draw(Canvas(bitmap))
        return bitmap.getPixel(width / 2, height / 2)
    }

    private fun View.centerPixel(width: Int = 200, height: Int = 200): Int {
        measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        return bitmap.getPixel(width / 2, height / 2)
    }

    private fun assertColor(message: String, expected: Int, actual: Int) =
        assertEquals(message, Integer.toHexString(expected), Integer.toHexString(actual))

    @Test
    fun elevationOverlayIsDisabled() {
        eachContext { label, context ->
            val value = TypedValue()
            context.theme.resolveAttribute(MaterialR.attr.elevationOverlayEnabled, value, true)
            assertEquals(label, 0, value.data)
        }
    }

    @Test
    fun bottomSheetSurfaceIsSurfaceElevated() {
        eachContext { label, context ->
            val dialog = BottomSheetDialog(context)
            dialog.setContentView(FrameLayout(context).apply { minimumHeight = 200 })
            dialog.show()
            shadowOf(Looper.getMainLooper()).idle()
            val sheet = dialog.findViewById<View>(MaterialR.id.design_bottom_sheet)
            assertNotNull(label, sheet)
            assertColor(label, context.tsColor(R.attr.tsColorSurfaceElevated), sheet!!.background.centerPixel())
            dialog.dismiss()
        }
    }

    @Test
    fun alertDialogSurfaceIsSurfaceElevated() {
        eachContext { label, context ->
            val dialog = MaterialAlertDialogBuilder(context).setMessage("TS").create()
            val background = dialog.window!!.decorView.background
            assertColor(label, context.tsColor(R.attr.tsColorSurfaceElevated), background.centerPixel())
        }
    }

    @Test
    fun cardSurfacesAreSurfaceCard() {
        eachContext { label, context ->
            listOf(
                MaterialR.attr.materialCardViewStyle,
                MaterialR.attr.materialCardViewElevatedStyle,
                MaterialR.attr.materialCardViewOutlinedStyle,
            ).forEach { style ->
                val card = MaterialCardView(context, null, style)
                assertColor("$label ${context.resources.getResourceEntryName(style)}", context.tsColor(R.attr.tsColorSurfaceCard), card.centerPixel())
            }
        }
    }

    @Test
    fun popupMenuSurfaceIsSurfaceElevated() {
        eachContext { label, context ->
            val array = context.obtainStyledAttributes(null, intArrayOf(android.R.attr.popupBackground), AppCompatR.attr.popupMenuStyle, 0)
            val background = array.getDrawable(0)
            array.recycle()
            assertNotNull(label, background)
            assertColor(label, context.tsColor(R.attr.tsColorSurfaceElevated), background!!.centerPixel())
        }
    }
}
