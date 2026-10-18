package com.tinysol.designsystem.compose.theme

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Test

class DsFontWeightsTest {
    @Test
    fun semiBoldFallsBackToMediumBelowApi28() {
        assertEquals(FontWeight.Medium, DsFontWeights.semiBold(sdkInt = 26))
        assertEquals(FontWeight.Medium, DsFontWeights.semiBold(sdkInt = 27))
        assertEquals(FontWeight.SemiBold, DsFontWeights.semiBold(sdkInt = 28))
        assertEquals(FontWeight.SemiBold, DsFontWeights.semiBold(sdkInt = 36))
    }

    @Test
    fun otherWeightsAreUnchangedOnEveryLevel() {
        listOf(26, 28, 36).forEach { sdk ->
            assertEquals(FontWeight.Normal, DsFontWeights.resolve(400, sdk))
            assertEquals(FontWeight.Medium, DsFontWeights.resolve(500, sdk))
            assertEquals(FontWeight.Bold, DsFontWeights.resolve(700, sdk))
        }
    }

    @Test
    fun everyTypeRoleResolvesThroughTheRoleMapping() {
        val typography = tsTypography { DsFontWeights.resolve(it, sdkInt = 26) }

        com.tinysol.designsystem.core.style.TSTypeRole.entries.forEach { role ->
            assertEquals(role.name, role.values.fontSize, typography.style(role).fontSize.value, 0f)
            assertEquals(role.name, DsFontWeights.resolve(role.values.fontWeight, 26), typography.style(role).fontWeight)
        }
    }
}
