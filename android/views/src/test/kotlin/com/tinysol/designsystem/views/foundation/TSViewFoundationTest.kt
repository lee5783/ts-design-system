package com.tinysol.designsystem.views.foundation

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.views.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
class TSViewFoundationTest {
    private val context: Context
        get() = ContextThemeWrapper(ApplicationProvider.getApplicationContext<Context>(), R.style.Theme_TS)

    @Test
    fun pressedColorStateListPrefersDisabledThenPressed() {
        val colors = pressableColorStateList(normal = 1, pressed = 2, disabled = 3)
        assertEquals(1, colors.getColorForState(intArrayOf(android.R.attr.state_enabled), 0))
        assertEquals(2, colors.getColorForState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_pressed), 0))
        assertEquals(3, colors.getColorForState(intArrayOf(android.R.attr.state_pressed), 0))
    }

    @Test
    fun pressedAlphaInstallsAStateListAnimator() {
        val view = View(context)
        view.applyPressedAlpha()
        assertNotNull(view.stateListAnimator)
    }

    @Test
    fun systemBarInsetsAreAddedToTheInitialPaddingRespectingRtl() {
        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(10, 20, 30, 40))
            .build()
        val ltr = FrameLayout(context).apply {
            setPaddingRelative(1, 2, 3, 4)
            applySystemBarInsetsAsPadding(start = true, top = true, bottom = true)
        }
        ViewCompat.dispatchApplyWindowInsets(ltr, insets)
        assertEquals(listOf(11, 22, 3, 44), listOf(ltr.paddingStart, ltr.paddingTop, ltr.paddingEnd, ltr.paddingBottom))

        ViewCompat.dispatchApplyWindowInsets(ltr, insets)
        assertEquals(11, ltr.paddingStart)

        context.applicationInfo.flags = context.applicationInfo.flags or ApplicationInfo.FLAG_SUPPORTS_RTL
        val rtl = FrameLayout(context).apply {
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            applySystemBarInsetsAsPadding(start = true, end = true)
        }
        Robolectric.buildActivity(Activity::class.java).setup().get().setContentView(rtl)
        ViewCompat.dispatchApplyWindowInsets(rtl, insets)
        assertEquals(30, rtl.paddingStart)
        assertEquals(10, rtl.paddingEnd)
    }
}
