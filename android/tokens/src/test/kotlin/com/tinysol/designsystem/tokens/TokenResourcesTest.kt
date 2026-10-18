package com.tinysol.designsystem.tokens

import android.content.Context
import android.util.TypedValue
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class TokenResourcesTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun color(id: Int): Long = context.getColor(id).toLong() and 0xFFFFFFFFL

    @Test
    fun overlayUsesArgbOrderInDayMode() {
        assertEquals(0x6618181BL, color(R.color.ts_overlay))
        assertEquals(0xFF7C3AEDL, color(R.color.ts_accent_text))
    }

    @Test
    @Config(qualifiers = "night")
    fun overlayUsesArgbOrderInNightMode() {
        assertEquals(0xA3000000L, color(R.color.ts_overlay))
        assertEquals(0xFFA78BFAL, color(R.color.ts_accent_text))
    }

    @Test
    fun focusAliasFollowsAccentTextInDayMode() {
        assertEquals(color(R.color.ts_accent_text), color(R.color.ts_focus))
    }

    @Test
    @Config(qualifiers = "night")
    fun focusAliasFollowsAccentTextInNightMode() {
        assertEquals(0xFFA78BFAL, color(R.color.ts_focus))
    }

    @Test
    fun resourceColorsMatchTokenValuesInDayMode() {
        assertEquals(TSTokenValues.LightColors.overlay, color(R.color.ts_overlay))
        assertEquals(TSTokenValues.LightColors.backgroundPrimary, color(R.color.ts_background_primary))
        assertEquals(TSTokenValues.LightColors.dangerPressed, color(R.color.ts_danger_pressed))
    }

    @Test
    @Config(qualifiers = "night")
    fun resourceColorsMatchTokenValuesInNightMode() {
        assertEquals(TSTokenValues.DarkColors.overlay, color(R.color.ts_overlay))
        assertEquals(TSTokenValues.DarkColors.backgroundPrimary, color(R.color.ts_background_primary))
        assertEquals(TSTokenValues.DarkColors.dangerPressed, color(R.color.ts_danger_pressed))
    }

    @Test
    @Config(qualifiers = "mdpi")
    fun dimensResolveInDp() {
        assertEquals(2f, context.resources.getDimension(R.dimen.ts_space_0_5), 0f)
        assertEquals(16f, context.resources.getDimension(R.dimen.ts_space_4), 0f)
        assertEquals(44f, context.resources.getDimension(R.dimen.ts_control_md), 0f)
        assertEquals(1.5f, context.resources.getDimension(R.dimen.ts_checkbox_border_width), 0f)
    }

    @Test
    @Config(qualifiers = "xxhdpi")
    fun dimensScaleWithDensity() {
        assertEquals(48f, context.resources.getDimension(R.dimen.ts_space_4), 0f)
        assertEquals(TSTokenValues.Radius.radiusMd * 3, context.resources.getDimension(R.dimen.ts_radius_md), 0f)
    }

    @Test
    fun opacityIsAFloatResource() {
        val value = TypedValue()
        context.resources.getValue(R.dimen.ts_opacity_disabled, value, true)

        assertEquals(TypedValue.TYPE_FLOAT, value.type)
        assertEquals(0.45f, value.float, 0f)
    }

    @Test
    fun motionAndComponentIntegersResolve() {
        assertEquals(120, context.resources.getInteger(R.integer.ts_duration_fast))
        assertEquals(200, context.resources.getInteger(R.integer.ts_duration_slow))
        assertEquals(600, context.resources.getInteger(R.integer.ts_alert_title_font_weight))
        assertEquals(700, context.resources.getInteger(R.integer.ts_spinner_rotation_duration))
    }
}
