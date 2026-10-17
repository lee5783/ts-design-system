package com.ts.ds.views.theme

import android.content.Context
import android.graphics.RectF
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.shape.ShapeAppearanceModel
import com.ts.ds.views.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import androidx.appcompat.R as AppCompatR
import com.google.android.material.R as MaterialR
import com.ts.ds.tokens.R as TokensR

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36], qualifiers = "mdpi")
class TSThemeTest {
    private val colorAttrs: Map<String, Int> = R.attr::class.java.fields
        .filter { it.name.startsWith("tsColor") }
        .associate { it.name to it.getInt(null) }

    private fun themed(theme: Int): Context =
        ContextThemeWrapper(ApplicationProvider.getApplicationContext<Context>(), theme)

    private fun overlaid(): Context =
        ContextThemeWrapper(themed(MaterialR.style.Theme_Material3_DayNight_NoActionBar), R.style.ThemeOverlay_TS)

    private fun tokenColor(context: Context, attrName: String): Int {
        val snake = attrName.removePrefix("tsColor").replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()
        val id = TokensR.color::class.java.getField("ts_$snake").getInt(null)
        return ContextCompat.getColor(context, id)
    }

    private fun Context.resourceId(attr: Int): Int =
        TypedValue().also { theme.resolveAttribute(attr, it, true) }.resourceId

    private fun assertEveryTokenAttrResolves(context: Context) {
        assertEquals(37, colorAttrs.size)
        colorAttrs.forEach { (name, attr) ->
            assertEquals(name, tokenColor(context, name), context.tsColor(attr))
        }
    }

    @Test
    fun everyTokenAttrResolvesToItsGeneratedColorInDayMode() {
        val context = themed(R.style.Theme_TS)
        assertEveryTokenAttrResolves(context)
        assertEquals(0xFF7C3AED.toInt(), context.tsColor(R.attr.tsColorAccentPrimary))
        assertEquals(0x6618181B, context.tsColor(R.attr.tsColorOverlay))
    }

    @Test
    fun everyTokenAttrResolvesToItsGeneratedColorInNightMode() {
        RuntimeEnvironment.setQualifiers("+night")
        val context = themed(R.style.Theme_TS)
        assertEveryTokenAttrResolves(context)
        assertEquals(0xA3000000.toInt(), context.tsColor(R.attr.tsColorOverlay))
        assertTrue(context.tsColor(R.attr.tsColorBackgroundPrimary) != 0xFFFCFCFC.toInt())
    }

    @Test
    fun overlayAppliesTokenAttrsOnTopOfAPlainMaterialTheme() {
        assertEveryTokenAttrResolves(overlaid())
        RuntimeEnvironment.setQualifiers("+night")
        assertEveryTokenAttrResolves(overlaid())
    }

    @Test
    fun materialColorAttrsAreMappedToTokens() {
        listOf("+notnight", "+night").forEach { qualifier ->
            RuntimeEnvironment.setQualifiers(qualifier)
            listOf(themed(R.style.Theme_TS), overlaid()).forEach { context ->
                val expected = mapOf(
                    AppCompatR.attr.colorPrimary to R.attr.tsColorAccentPrimary,
                    MaterialR.attr.colorOnPrimary to R.attr.tsColorTextOnAccent,
                    MaterialR.attr.colorPrimaryContainer to R.attr.tsColorAccentSubtle,
                    MaterialR.attr.colorSecondaryContainer to R.attr.tsColorAccentSubtle,
                    AppCompatR.attr.colorError to R.attr.tsColorDanger,
                    MaterialR.attr.colorErrorContainer to R.attr.tsColorDangerSubtle,
                    android.R.attr.colorBackground to R.attr.tsColorBackgroundPrimary,
                    MaterialR.attr.colorOnBackground to R.attr.tsColorTextPrimary,
                    MaterialR.attr.colorSurface to R.attr.tsColorSurfaceCard,
                    MaterialR.attr.colorOnSurface to R.attr.tsColorTextPrimary,
                    MaterialR.attr.colorOnSurfaceVariant to R.attr.tsColorTextSecondary,
                    MaterialR.attr.colorSurfaceContainerHigh to R.attr.tsColorSurfaceElevated,
                    MaterialR.attr.colorSurfaceInverse to R.attr.tsColorSurfaceInverse,
                    MaterialR.attr.colorOnSurfaceInverse to R.attr.tsColorTextInverse,
                    MaterialR.attr.colorOutline to R.attr.tsColorBorderStrong,
                    MaterialR.attr.colorOutlineVariant to R.attr.tsColorBorderSubtle,
                )
                expected.forEach { (material, token) ->
                    assertEquals("$qualifier ${context.resources.getResourceEntryName(material)}", context.tsColor(token), context.tsColor(material))
                }
            }
        }
    }

    @Test
    fun materialTextAppearancesUseTSTypography() {
        val context = themed(R.style.Theme_TS)
        val expected = mapOf(
            MaterialR.attr.textAppearanceDisplaySmall to R.style.TextAppearance_TS_Display,
            MaterialR.attr.textAppearanceHeadlineMedium to R.style.TextAppearance_TS_Title1,
            MaterialR.attr.textAppearanceTitleLarge to R.style.TextAppearance_TS_Title2,
            MaterialR.attr.textAppearanceTitleMedium to R.style.TextAppearance_TS_Title3,
            MaterialR.attr.textAppearanceBodyLarge to R.style.TextAppearance_TS_BodyLg,
            MaterialR.attr.textAppearanceBodyMedium to R.style.TextAppearance_TS_Body,
            MaterialR.attr.textAppearanceBodySmall to R.style.TextAppearance_TS_BodySm,
            MaterialR.attr.textAppearanceLabelLarge to R.style.TextAppearance_TS_Label,
            MaterialR.attr.textAppearanceLabelMedium to R.style.TextAppearance_TS_Caption,
            MaterialR.attr.textAppearanceLabelSmall to R.style.TextAppearance_TS_Overline,
        )
        expected.forEach { (attr, style) -> assertEquals(style, context.resourceId(attr)) }
        val enabled = TypedValue().also { context.theme.resolveAttribute(MaterialR.attr.textAppearanceLineHeightEnabled, it, true) }
        assertTrue(enabled.data != 0)
    }

    @Test
    fun materialShapeAttrsUseTSShapeAppearances() {
        val context = themed(R.style.Theme_TS)
        val expected = mapOf(
            MaterialR.attr.shapeAppearanceCornerExtraSmall to (R.style.ShapeAppearance_TS_Xs to 4f),
            MaterialR.attr.shapeAppearanceCornerSmall to (R.style.ShapeAppearance_TS_Sm to 6f),
            MaterialR.attr.shapeAppearanceCornerMedium to (R.style.ShapeAppearance_TS_Md to 8f),
            MaterialR.attr.shapeAppearanceCornerLarge to (R.style.ShapeAppearance_TS_Lg to 12f),
            MaterialR.attr.shapeAppearanceCornerExtraLarge to (R.style.ShapeAppearance_TS_Xl to 16f),
        )
        expected.forEach { (attr, styleAndRadius) ->
            val (style, radius) = styleAndRadius
            assertEquals(style, context.resourceId(attr))
            val model = ShapeAppearanceModel.builder(context, style, 0).build()
            assertEquals(radius, model.topLeftCornerSize.getCornerSize(RectF(0f, 0f, 100f, 100f)), 0.01f)
        }
    }

    @Test
    fun fullShapeAppearanceIsACapsule() {
        val context = themed(R.style.Theme_TS)
        val model = ShapeAppearanceModel.builder(context, R.style.ShapeAppearance_TS_Full, 0).build()
        assertEquals(20f, model.bottomRightCornerSize.getCornerSize(RectF(0f, 0f, 120f, 40f)), 0.01f)
    }
}
