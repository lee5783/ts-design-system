package com.tinysol.designsystem.core.icon

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.VectorDrawable
import android.util.TypedValue
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.core.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class TSIconResourcesTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun density(): Float = context.resources.displayMetrics.density

    private fun resolvedFile(id: Int): String = TypedValue().also { context.resources.getValue(id, it, true) }.string.toString()

    @Test
    fun everyIconResolvesItsVectorDrawable() {
        assertEquals(33, TSIcon.entries.size)
        TSIcon.entries.forEach { icon ->
            val drawable = context.getDrawable(icon.drawableRes)
            assertNotNull("${icon.name} drawable", drawable)
            assertTrue("${icon.name} is a VectorDrawable", drawable is VectorDrawable)
            assertEquals("${icon.name} width", Math.round(24 * density()), drawable!!.intrinsicWidth)
            assertEquals("${icon.name} height", Math.round(24 * density()), drawable.intrinsicHeight)
        }
    }

    @Test
    fun directionalIconsAreAutoMirrored() {
        assertTrue(context.getDrawable(TSIcon.ArrowLeft.drawableRes)!!.isAutoMirrored)
        assertTrue(context.getDrawable(TSIcon.ChevronRight.drawableRes)!!.isAutoMirrored)
        assertFalse(context.getDrawable(TSIcon.Home.drawableRes)!!.isAutoMirrored)
        TSIcon.entries.forEach { icon ->
            assertEquals(icon.name, icon.autoMirrored, context.getDrawable(icon.drawableRes)!!.isAutoMirrored)
        }
        assertEquals(
            listOf(TSIcon.ArrowLeft, TSIcon.ArrowRight, TSIcon.ChevronLeft, TSIcon.ChevronRight, TSIcon.Logout),
            TSIcon.entries.filter { it.autoMirrored },
        )
    }

    @Test
    fun wordmarkResolvesInDayMode() {
        val drawable = context.getDrawable(R.drawable.ts_logo_wordmark)
        assertTrue(drawable is VectorDrawable)
        assertEquals(Math.round(248 * density()), drawable!!.intrinsicWidth)
        assertEquals(Math.round(48 * density()), drawable.intrinsicHeight)
        assertFalse(resolvedFile(R.drawable.ts_logo_wordmark).contains("night"))
        assertNotNull(context.getDrawable(R.drawable.ts_logo_monogram))
    }

    @Test
    @Config(qualifiers = "night")
    fun wordmarkHasANightVariant() {
        val night = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        assertEquals(Configuration.UI_MODE_NIGHT_YES, night)
        assertTrue(resolvedFile(R.drawable.ts_logo_wordmark).contains("drawable-night"))
        assertTrue(context.getDrawable(R.drawable.ts_logo_wordmark) is VectorDrawable)
    }
}
