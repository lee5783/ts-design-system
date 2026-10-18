package com.tinysol.designsystem.views.components.spinner

import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.tinysol.designsystem.core.style.TSSpinnerSize
import com.tinysol.designsystem.core.style.TSSpinnerTone
import com.tinysol.designsystem.views.R
import com.tinysol.designsystem.views.components.hostInActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
class TSSpinnerViewTest {
    private fun spinner(configure: TSSpinnerView.() -> Unit = {}): TSSpinnerView = hostInActivity { context ->
        TSSpinnerView(context).apply(configure)
    }

    @Test
    fun measuresToTheIconTokenSize() {
        val expected = mapOf(TSSpinnerSize.Sm to 16, TSSpinnerSize.Md to 20, TSSpinnerSize.Lg to 24)
        expected.forEach { (size, px) ->
            val view = spinner { this.size = size }
            assertEquals(px, view.width)
            assertEquals(px, view.height)
        }
    }

    @Test
    fun defaultsAreMediumPrimary() {
        val view = spinner()
        assertEquals(TSSpinnerSize.Md, view.size)
        assertEquals(TSSpinnerTone.Primary, view.tone)
    }

    @Test
    fun xmlAttributesSelectSizeAndTone() {
        val context = ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_TS)
        val attrs = Robolectric.buildAttributeSet()
            .addAttribute(R.attr.tsSpinnerSize, "lg")
            .addAttribute(R.attr.tsSpinnerTone, "accent")
            .build()
        val view = TSSpinnerView(context, attrs)
        assertEquals(TSSpinnerSize.Lg, view.size)
        assertEquals(TSSpinnerTone.Accent, view.tone)
        assertEquals(24f, view.resolved.sizeDp, 0f)
    }

    @Test
    fun spinsOnlyWhileShown() {
        val view = spinner()
        view.visibility = View.GONE
        assertFalse(view.isSpinning)
        view.visibility = View.VISIBLE
        assertTrue(view.isSpinning)
    }
}
