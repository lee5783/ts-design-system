package com.tinysol.designsystem.sample.support.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DemoLaunchOverridesTest {
    private val scheme = DemoLaunchOverrides.COMPOSE_SCHEME

    @Test
    fun extraParsesEveryThemeIgnoringCaseAndSpaces() {
        assertEquals(DemoTheme.Light, DemoLaunchOverrides.themeFromExtra("light"))
        assertEquals(DemoTheme.Dark, DemoLaunchOverrides.themeFromExtra(" DARK "))
        assertEquals(DemoTheme.System, DemoLaunchOverrides.themeFromExtra("system"))
    }

    @Test
    fun unknownOrEmptyExtraFallsBackToSystem() {
        assertEquals(DemoTheme.System, DemoLaunchOverrides.themeFromExtra("sepia"))
        assertEquals(DemoTheme.System, DemoLaunchOverrides.themeFromExtra(""))
        assertNull(DemoLaunchOverrides.themeFromExtra(null))
    }

    @Test
    fun settingsLinkParsesTheThemeParameter() {
        assertEquals(DemoTheme.Dark, DemoLaunchOverrides.themeFromLink("tsds-compose://settings?theme=dark", scheme))
        assertEquals(DemoTheme.Light, DemoLaunchOverrides.themeFromLink("TSDS-COMPOSE://Settings?other=1&theme=Light", scheme))
        assertEquals(DemoTheme.System, DemoLaunchOverrides.themeFromLink("tsds-compose://settings?theme=system", scheme))
    }

    @Test
    fun unknownOrEmptyLinkValueFallsBackToSystem() {
        assertEquals(DemoTheme.System, DemoLaunchOverrides.themeFromLink("tsds-compose://settings?theme=sepia", scheme))
        assertEquals(DemoTheme.System, DemoLaunchOverrides.themeFromLink("tsds-compose://settings?theme=", scheme))
        assertEquals(DemoTheme.System, DemoLaunchOverrides.themeFromLink("tsds-compose://settings?theme", scheme))
    }

    @Test
    fun linksForAnotherDemoHostOrWithoutAThemeAreIgnored() {
        assertNull(DemoLaunchOverrides.themeFromLink("tsds-views://settings?theme=dark", scheme))
        assertNull(DemoLaunchOverrides.themeFromLink("tsds-compose://page?theme=dark", scheme))
        assertNull(DemoLaunchOverrides.themeFromLink("tsds-compose://settings", scheme))
        assertNull(DemoLaunchOverrides.themeFromLink("not a uri", scheme))
        assertNull(DemoLaunchOverrides.themeFromLink(null, scheme))
    }

    @Test
    fun theLinkWinsOverTheExtra() {
        assertEquals(DemoTheme.Light, DemoLaunchOverrides.theme("dark", "tsds-compose://settings?theme=light", scheme))
        assertEquals(DemoTheme.Dark, DemoLaunchOverrides.theme("dark", null, scheme))
        assertNull(DemoLaunchOverrides.theme(null, null, scheme))
    }

    @Test
    fun settingsLinkRoundTrips() {
        DemoTheme.entries.forEach { theme ->
            assertEquals(theme, DemoLaunchOverrides.themeFromLink(DemoLaunchOverrides.settingsLink(DemoLaunchOverrides.VIEWS_SCHEME, theme), DemoLaunchOverrides.VIEWS_SCHEME))
        }
    }

    @Test
    fun quickToggleCyclesSystemLightDark() {
        assertEquals(DemoTheme.Light, DemoTheme.System.next())
        assertEquals(DemoTheme.Dark, DemoTheme.Light.next())
        assertEquals(DemoTheme.System, DemoTheme.Dark.next())
    }
}
