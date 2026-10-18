package com.tinysol.designsystem.compose.theme

import android.os.Build
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.compose.foundation.TSPressIndication
import com.tinysol.designsystem.core.style.TSColorRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class TSThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private class Captured {
        lateinit var colors: TSColors
        lateinit var controllerColors: TSControllerColors
        lateinit var typography: TSTypography
        lateinit var elevation: TSElevation
        lateinit var scheme: ColorScheme
        var isDark: Boolean = false
        var indication: Any? = null
    }

    private fun capture(darkTheme: Boolean, rippleEnabled: Boolean = false): Captured {
        val captured = Captured()
        composeRule.setContent {
            TSTheme(darkTheme = darkTheme, rippleEnabled = rippleEnabled) {
                captured.colors = TSTheme.colors
                captured.controllerColors = TSTheme.controllerColors
                captured.typography = TSTheme.typography
                captured.elevation = TSTheme.elevation
                captured.scheme = MaterialTheme.colorScheme
                captured.isDark = TSTheme.isDark
                captured.indication = LocalIndication.current
            }
        }
        composeRule.waitForIdle()
        return captured
    }

    private fun assertControllerColors(expected: TSControllerColors, actual: TSControllerColors) {
        listOf(
            TSControllerColors::backdrop,
            TSControllerColors::key,
            TSControllerColors::keyPressed,
            TSControllerColors::keyOutline,
            TSControllerColors::keyLabel,
            TSControllerColors::backing,
            TSControllerColors::overlayScrimMedia,
            TSControllerColors::textOnOverlayMedia,
        ).forEach { role -> assertEquals(role.name, role.get(expected), role.get(actual)) }
    }

    @Test
    @Config(sdk = [26, 36])
    fun lightThemeProvidesLightColors() {
        val captured = capture(darkTheme = false)

        TSColorRole.entries.forEach { role ->
            assertEquals(role.name, lightTSColors().color(role), captured.colors.color(role))
        }
        assertFalse(captured.isDark)
        assertEquals(lightTSElevation().shadowMd.color, captured.elevation.shadowMd.color)
    }

    @Test
    @Config(sdk = [26, 36])
    fun darkThemeProvidesDarkColors() {
        val captured = capture(darkTheme = true)

        TSColorRole.entries.forEach { role ->
            assertEquals(role.name, darkTSColors().color(role), captured.colors.color(role))
        }
        assertTrue(captured.isDark)
        assertEquals(darkTSElevation().shadowMd.color, captured.elevation.shadowMd.color)
        assertNotEquals(lightTSColors().backgroundPrimary, captured.colors.backgroundPrimary)
    }

    @Test
    fun lightThemeProvidesLightControllerColors() {
        val captured = capture(darkTheme = false)

        assertControllerColors(lightTSControllerColors(), captured.controllerColors)
    }

    @Test
    fun darkThemeProvidesDarkControllerColors() {
        val captured = capture(darkTheme = true)

        assertControllerColors(darkTSControllerColors(), captured.controllerColors)
        assertNotEquals(lightTSControllerColors().backing, captured.controllerColors.backing)
    }

    @Test
    @Config(sdk = [31, 34, 36])
    fun dynamicColorIsNeverApplied() {
        assertTrue(Build.VERSION.SDK_INT >= 31)
        listOf(false, true).forEach { dark ->
            val colors = if (dark) darkTSColors() else lightTSColors()
            val scheme = TSMaterialBridge.colorScheme(colors, dark)

            assertEquals(colors.accentPrimary, scheme.primary)
            assertEquals(colors.textOnAccent, scheme.onPrimary)
            assertEquals(colors.surfaceCard, scheme.surface)
            assertEquals(colors.backgroundPrimary, scheme.background)
            assertEquals(colors.danger, scheme.error)
            assertEquals(colors.borderStrong, scheme.outline)
            assertEquals(colors.borderSubtle, scheme.outlineVariant)
            assertEquals(colors.surfaceInverse, scheme.inverseSurface)
            assertEquals(colors.textInverse, scheme.inverseOnSurface)
            assertEquals(colors.overlay, scheme.scrim)
        }

        val captured = capture(darkTheme = false)
        assertEquals(lightTSColors().accentPrimary, captured.scheme.primary)
        assertEquals(lightTSColors().accentSubtle, captured.scheme.primaryContainer)
        assertEquals(lightTSColors().surfaceElevated, captured.scheme.surfaceContainer)
    }

    @Test
    @Config(sdk = [36])
    fun materialSchemeFollowsDarkTheme() {
        val captured = capture(darkTheme = true)

        assertEquals(darkTSColors().accentPrimary, captured.scheme.primary)
        assertEquals(darkTSColors().backgroundPrimary, captured.scheme.background)
        assertEquals(darkTSColors().overlay, captured.scheme.scrim)
    }

    @Test
    @Config(sdk = [26])
    fun semiBoldRendersAsMediumOnApi26() {
        val typography = capture(darkTheme = false).typography

        assertEquals(FontWeight.Medium, typography.title2.fontWeight)
        assertEquals(FontWeight.Medium, typography.title3.fontWeight)
        assertEquals(FontWeight.Medium, typography.overline.fontWeight)
        assertEquals(FontWeight.Bold, typography.title1.fontWeight)
        assertEquals(FontWeight.Medium, typography.label.fontWeight)
        assertEquals(FontWeight.Normal, typography.body.fontWeight)
    }

    @Test
    @Config(sdk = [28, 36])
    fun semiBoldIsTrueSemiBoldFromApi28() {
        val typography = capture(darkTheme = false).typography

        assertEquals(FontWeight.SemiBold, typography.title2.fontWeight)
        assertEquals(FontWeight.SemiBold, typography.title3.fontWeight)
        assertEquals(FontWeight.SemiBold, typography.overline.fontWeight)
        assertEquals(FontWeight.Bold, typography.title1.fontWeight)
    }

    @Test
    @Config(sdk = [36])
    fun flatPressFillIsTheDefaultIndication() {
        val captured = capture(darkTheme = false)

        assertEquals(TSPressIndication(lightTSColors().surfacePressed), captured.indication)
    }

    @Test
    @Config(sdk = [36])
    fun rippleIsOptIn() {
        val captured = capture(darkTheme = false, rippleEnabled = true)

        assertFalse(captured.indication is TSPressIndication)
    }
}
