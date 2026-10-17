package com.ts.ds.demo.support.settings

import com.ts.ds.demo.support.catalog.DemoPageId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoLaunchOptionsTest {
    @Test
    fun parsesEveryOption() {
        val extras = mapOf(
            DemoLaunchOptions.THEME to "dark",
            DemoLaunchOptions.FONT_SCALE to "2",
            DemoLaunchOptions.RIGHT_TO_LEFT to "YES",
            DemoLaunchOptions.REDUCE_MOTION to "false",
            DemoLaunchOptions.PAGE to "typography",
        )
        assertEquals(
            DemoLaunchOptions(DemoTheme.Dark, 2f, rtl = true, reduceMotion = false, page = DemoPageId.Typography),
            DemoLaunchOptions.from(extras::get),
        )
    }

    @Test
    fun ignoresMissingAndInvalidValues() {
        val extras = mapOf(
            DemoLaunchOptions.THEME to "sepia",
            DemoLaunchOptions.FONT_SCALE to "3",
            DemoLaunchOptions.RIGHT_TO_LEFT to "maybe",
            DemoLaunchOptions.PAGE to "nope",
        )
        val options = DemoLaunchOptions.from(extras::get)
        assertEquals(DemoLaunchOptions(), options)
        assertFalse(options.hasSettings)
    }

    @Test
    fun appliesOnlyTheGivenSettings() {
        val current = DemoSettings(theme = DemoTheme.Light, fontScale = 1.3f, reduceMotion = true)
        val options = DemoLaunchOptions(rtl = true)
        assertTrue(options.hasSettings)
        assertEquals(current.copy(rtl = true), options.applyTo(current))
    }
}
