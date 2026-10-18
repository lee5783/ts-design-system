package com.tinysol.designsystem.views.theme

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textview.MaterialTextView
import com.tinysol.designsystem.views.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 28], qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSMaterialTextViewTest {
    private val context: Context
        get() = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Context>(), R.style.Theme_TS)

    private fun inflated(appearance: String): MaterialTextView {
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.textAppearance, appearance)
            .build()
        return MaterialTextView(context, attrs).apply { text = "TS" }
    }

    @Test
    fun lineHeightFromXmlTextAppearanceIsApplied() {
        assertEquals(24, inflated("@style/TextAppearance.TS.Body").lineHeight)
        assertEquals(44, inflated("@style/TextAppearance.TS.Display").lineHeight)
    }

    @Test
    fun lineHeightFromProgrammaticTextAppearanceIsApplied() {
        val textView = MaterialTextView(context).apply {
            text = "TS"
            setTextAppearance(R.style.TextAppearance_TS_Title3)
        }
        assertEquals(24, textView.lineHeight)
        assertEquals(18f, textView.textSize, 0.01f)
    }

    @Test
    fun lineHeightScalesWithFontScale() {
        val scaled = context.createConfigurationContext(
            context.resources.configuration.apply { fontScale = 2f },
        )
        val textView = MaterialTextView(ContextThemeWrapper(scaled, R.style.Theme_TS)).apply {
            text = "TS"
            setTextAppearance(R.style.TextAppearance_TS_Body)
        }
        assertEquals(48, textView.lineHeight)
    }

    @Test
    fun semiBoldRendersAsMediumBeforeApi28AndTrueWeightFrom28() {
        val textView = inflated("@style/TextAppearance.TS.Title2")
        if (Build.VERSION.SDK_INT >= 28) {
            assertEquals(600, textView.typeface.weight)
        } else {
            assertEquals(Typeface.create("sans-serif-medium", Typeface.NORMAL), textView.typeface)
            assertNotEquals(Typeface.create("sans-serif", Typeface.NORMAL), textView.typeface)
        }
    }
}
