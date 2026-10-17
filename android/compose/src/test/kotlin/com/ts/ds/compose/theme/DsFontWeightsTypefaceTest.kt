package com.ts.ds.compose.theme

import android.graphics.Typeface
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import com.ts.ds.core.style.TSTypeRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DsFontWeightsTypefaceTest {
    private val semiBoldRoles = TSTypeRole.entries.filter { it.values.fontWeight == FontWeight.SemiBold.weight && !it.values.monospace }

    @Test
    @Config(sdk = [26])
    fun semiBoldRolesRenderAsSansSerifMediumBelowApi28() {
        val typography = tsTypography(DsFontWeights::resolve).withDsFontFamilies()
        val medium = Typeface.create(DsFontWeights.LEGACY_MEDIUM_FAMILY, Typeface.NORMAL)

        semiBoldRoles.forEach { role ->
            assertSame(role.name, medium, typeface(typography.style(role)))
        }
        assertNotSame(medium, typeface(typography.body))
    }

    @Test
    @Config(sdk = [36])
    fun semiBoldRolesKeepTheDefaultFamilyFromApi28() {
        val typography = tsTypography(DsFontWeights::resolve).withDsFontFamilies()

        semiBoldRoles.forEach { role ->
            assertEquals(role.name, FontFamily.Default, typography.style(role).fontFamily)
            assertEquals(role.name, FontWeight.SemiBold, typography.style(role).fontWeight)
        }
    }

    @Test
    @Config(sdk = [26])
    fun weightOverridesOnBodyUseSansSerifMediumBelowApi28() {
        val typography = tsTypography(DsFontWeights::resolve).withDsFontFamilies()
        val medium = Typeface.create(DsFontWeights.LEGACY_MEDIUM_FAMILY, Typeface.NORMAL)

        assertSame(medium, typeface(typography.body.withDsFontWeight(FontWeight.Medium.weight)))
        assertSame(medium, typeface(typography.body.withDsFontWeight(FontWeight.SemiBold.weight)))
        assertNotSame(medium, typeface(typography.body.withDsFontWeight(FontWeight.Normal.weight)))
    }

    @Test
    @Config(sdk = [36])
    fun weightOverridesKeepTheDefaultFamilyFromApi28() {
        val style = tsTypography(DsFontWeights::resolve).body.withDsFontWeight(FontWeight.Medium.weight)

        assertEquals(FontFamily.Default, style.fontFamily)
        assertEquals(FontWeight.Medium, style.fontWeight)
    }

    private fun typeface(style: TextStyle): Typeface =
        createFontFamilyResolver(RuntimeEnvironment.getApplication())
            .resolve(style.fontFamily, style.fontWeight ?: FontWeight.Normal, FontStyle.Normal, FontSynthesis.None)
            .value as Typeface
}
