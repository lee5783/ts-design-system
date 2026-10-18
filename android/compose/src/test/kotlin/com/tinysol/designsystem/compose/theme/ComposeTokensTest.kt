package com.tinysol.designsystem.compose.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class ComposeTokensTest {
    @Test
    fun colorsUseThemeValuesWithAlpha() {
        assertEquals(Color(0x6618181B), lightTSColors().overlay)
        assertEquals(Color(0xA3000000), darkTSColors().overlay)
        assertEquals(0.4f, lightTSColors().overlay.alpha, 0.01f)
        assertEquals(0.64f, darkTSColors().overlay.alpha, 0.01f)
        assertEquals(Color(0xFF7C3AED), lightTSColors().accentPrimary)
    }

    @Test
    fun focusAliasResolvesPerTheme() {
        assertEquals(lightTSColors().accentText, lightTSColors().focus)
        assertEquals(Color(0xFFA78BFA), darkTSColors().focus)
    }

    @Test
    fun typographyMatchesTokens() {
        val typography = tsTypography()

        assertEquals(36.sp, typography.display.fontSize)
        assertEquals(44.sp, typography.display.lineHeight)
        assertEquals(FontWeight.Bold, typography.display.fontWeight)
        assertEquals((-0.02f).em, typography.display.letterSpacing)
        assertEquals(FontWeight.SemiBold, typography.title2.fontWeight)
        assertEquals(FontFamily.Monospace, typography.code.fontFamily)
        assertEquals(
            LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
            typography.body.lineHeightStyle,
        )
    }

    @Test
    fun typographyRoutesWeightsThroughTheMapper() {
        val typography = tsTypography { weight -> if (weight == 600) FontWeight.Medium else FontWeight(weight) }

        assertEquals(FontWeight.Medium, typography.title2.fontWeight)
        assertEquals(FontWeight.Medium, typography.overline.fontWeight)
        assertEquals(FontWeight.Bold, typography.title1.fontWeight)
    }

    @Test
    fun dimensionsAndShapesMatchTokens() {
        assertEquals(2.dp, TSSpacing.space0_5)
        assertEquals(16.dp, TSSpacing.space4)
        assertEquals(44.dp, TSSize.controlMd)
        assertEquals(RoundedCornerShape(8.dp), TSShapes.radiusMd)
        assertEquals(CircleShape, TSShapes.radiusFull)
    }

    @Test
    fun elevationAndMotionMatchTokens() {
        assertEquals(1.dp, lightTSElevation().shadowXs.elevation)
        assertEquals(16.dp, darkTSElevation().shadowLg.elevation)
        assertEquals(Color(0xFF18181B), lightTSElevation().shadowMd.color)
        assertEquals(Color.Black, darkTSElevation().shadowMd.color)
        assertEquals(120, TSMotion.durationFast)
        assertEquals(200, TSMotion.durationSlow)
        assertEquals(1f, TSMotion.easingStandard.transform(1f), 0.001f)
    }
}
