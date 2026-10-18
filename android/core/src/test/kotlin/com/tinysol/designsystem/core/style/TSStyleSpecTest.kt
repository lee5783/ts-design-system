package com.tinysol.designsystem.core.style

import com.tinysol.designsystem.tokens.TSTokenValues
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

class TSStyleSpecTest {
    private fun tokenFields(holder: Class<*>): Map<String, Long> =
        holder.declaredFields
            .filter { Modifier.isStatic(it.modifiers) && it.type == Long::class.javaPrimitiveType }
            .associate { it.name to it.getLong(null) }

    private val TSColorRole.tokenName: String
        get() = name.replaceFirstChar { it.lowercase() }

    @Test
    fun everyColorTokenHasExactlyOneRole() {
        val light = tokenFields(TSTokenValues.LightColors::class.java)

        assertEquals(light.keys, TSColorRole.entries.map { it.tokenName }.toSet())
        assertEquals(light.size, TSColorRole.entries.size)
    }

    @Test
    fun rolesCarryTheLightAndDarkTokenValues() {
        val light = tokenFields(TSTokenValues.LightColors::class.java)
        val dark = tokenFields(TSTokenValues.DarkColors::class.java)

        TSColorRole.entries.forEach { role ->
            assertEquals(role.name, light.getValue(role.tokenName), role.light)
            assertEquals(role.name, dark.getValue(role.tokenName), role.dark)
            assertEquals(role.light, role.argb(dark = false))
            assertEquals(role.dark, role.argb(dark = true))
        }
    }

    @Test
    fun typeRolesCoverEveryTypeStyle() {
        assertEquals(11, TSTypeRole.entries.size)
        assertSame(TSTokenValues.Typography.title2, TSTypeRole.Title2.values)
        assertEquals(600, TSTypeRole.Overline.values.fontWeight)
        assertTrue(TSTypeRole.Code.values.monospace)
    }

    @Test
    fun controlStateDerivesInteractivity() {
        assertTrue(TSControlState.Default.interactive)
        assertFalse(TSControlState(enabled = false).interactive)
        assertFalse(TSControlState(loading = true).interactive)
    }

    @Test
    fun specsResolveConfigurationAndStateToTokens() {
        val spec = TSStyleSpec<Boolean, TSColorRole> { primary, state ->
            when {
                !state.enabled -> TSColorRole.TextDisabled
                primary && state.pressed -> TSColorRole.AccentPressed
                primary -> TSColorRole.AccentPrimary
                state.pressed -> TSColorRole.SurfacePressed
                else -> TSColorRole.SurfaceCard
            }
        }

        assertEquals(TSColorRole.AccentPrimary, spec.resolve(true))
        assertEquals(TSColorRole.AccentPressed, spec.resolve(true, TSControlState(pressed = true)))
        assertEquals(TSColorRole.SurfacePressed, spec.resolve(false, TSControlState(pressed = true)))
        assertEquals(TSColorRole.TextDisabled, spec.resolve(true, TSControlState(enabled = false)))
    }
}
