package com.ts.ds.views

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 28], qualifiers = "mdpi")
class TextAppearanceResourcesTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val attrs = intArrayOf(
        android.R.attr.textSize,
        android.R.attr.textStyle,
        android.R.attr.fontFamily,
        android.R.attr.letterSpacing,
        android.R.attr.lineHeight,
        android.R.attr.textFontWeight,
        androidx.appcompat.R.attr.lineHeight,
    ).sortedArray()

    private fun <T> withAppearance(style: Int, block: (TypedArray, (Int) -> Int) -> T): T {
        val array = context.obtainStyledAttributes(style, attrs)
        try {
            return block(array) { attr -> attrs.indexOf(attr) }
        } finally {
            array.recycle()
        }
    }

    private val isApi28: Boolean
        get() = Build.VERSION.SDK_INT >= 28

    @Test
    fun appCompatLineHeightIsAlwaysSet() {
        withAppearance(R.style.TextAppearance_TS_Body) { array, index ->
            assertEquals(24, array.getDimensionPixelSize(index(androidx.appcompat.R.attr.lineHeight), -1))
            assertEquals(16, array.getDimensionPixelSize(index(android.R.attr.textSize), -1))
        }
    }

    @Test
    fun platformLineHeightOnlyFromApi28() {
        withAppearance(R.style.TextAppearance_TS_Body) { array, index ->
            assertEquals(isApi28, array.hasValue(index(android.R.attr.lineHeight)))
            if (isApi28) {
                assertEquals(24, array.getDimensionPixelSize(index(android.R.attr.lineHeight), -1))
            }
        }
    }

    @Test
    fun semiBoldFallsBackToMediumBeforeApi28() {
        withAppearance(R.style.TextAppearance_TS_Title2) { array, index ->
            if (isApi28) {
                assertEquals("sans-serif", array.getString(index(android.R.attr.fontFamily)))
                assertEquals(600, array.getInt(index(android.R.attr.textFontWeight), -1))
            } else {
                assertEquals("sans-serif-medium", array.getString(index(android.R.attr.fontFamily)))
                assertFalse(array.hasValue(index(android.R.attr.textFontWeight)))
            }
        }
    }

    @Test
    fun mediumAndBoldWeightsMapOnEveryApiLevel() {
        withAppearance(R.style.TextAppearance_TS_Label) { array, index ->
            val expectedFamily = if (isApi28) "sans-serif" else "sans-serif-medium"
            assertEquals(expectedFamily, array.getString(index(android.R.attr.fontFamily)))
        }
        withAppearance(R.style.TextAppearance_TS_Title1) { array, index ->
            assertEquals(1, array.getInt(index(android.R.attr.textStyle), -1))
            assertEquals(-0.01f, array.getFloat(index(android.R.attr.letterSpacing), 0f), 0.0001f)
        }
    }

    @Test
    fun codeUsesMonospace() {
        withAppearance(R.style.TextAppearance_TS_Code) { array, index ->
            assertEquals("monospace", array.getString(index(android.R.attr.fontFamily)))
            assertTrue(array.getDimensionPixelSize(index(androidx.appcompat.R.attr.lineHeight), -1) == 20)
        }
    }

    @Test
    fun themeColorAttrsAreDeclared() {
        assertTrue(R.attr.tsColorAccentPrimary != 0)
        assertTrue(R.attr.tsColorOverlay != 0)
    }
}
