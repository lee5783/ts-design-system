package com.ts.ds.core.style

import com.ts.ds.core.icon.TSIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSTextFieldStyleSpecTest(
    private val configuration: TSTextFieldStyleSpec.Configuration,
    private val state: TSControlState,
) {
    private val resolved = TSTextFieldStyleSpec.resolve(configuration, state)
    private val readOnly = configuration.readOnly
    private val disabled = !state.enabled
    private val focusVisible = state.focused && !disabled && !readOnly

    @Test
    fun borderFollowsTheStateTable() {
        val expected = when {
            state.error -> TSColorRole.Danger
            focusVisible -> TSColorRole.Focus
            else -> TSColorRole.BorderStrong
        }
        assertEquals(expected, resolved.border)
        assertEquals(if (focusVisible) 2f else 1f, resolved.borderWidthDp, 0f)
        assertEquals(focusVisible, resolved.showsFocusRing)
    }

    @Test
    fun fillShadowAndOpacity() {
        assertEquals(if (disabled || readOnly) TSColorRole.BackgroundSecondary else TSColorRole.SurfaceCard, resolved.fill)
        assertEquals(if (readOnly) null else TSShadowRole.Xs, resolved.shadow)
        assertEquals(if (disabled) 0.45f else 1f, resolved.opacity, 0f)
    }

    @Test
    fun editabilityAndSelection() {
        assertEquals(!disabled && !readOnly, resolved.editable)
        assertEquals(!disabled, resolved.selectable)
    }

    @Test
    fun messageFollowsTheErrorState() {
        assertEquals(if (state.error) TSColorRole.Danger else TSColorRole.TextMuted, resolved.messageColor)
        assertEquals(state.error, resolved.showsErrorIcon)
    }

    @Test
    fun anatomy() {
        assertEquals(TSTypeRole.Label, resolved.labelTypeRole)
        assertEquals(500, resolved.labelFontWeight)
        assertEquals(TSColorRole.TextPrimary, resolved.labelColor)
        val inputTypeRole = when (configuration.contentStyle) {
            TSTextFieldContentStyle.Default -> TSTypeRole.Body
            TSTextFieldContentStyle.Code -> TSTypeRole.Code
        }
        assertEquals(inputTypeRole, resolved.inputTypeRole)
        assertEquals(TSColorRole.TextPrimary, resolved.inputColor)
        assertEquals(TSColorRole.TextMuted, resolved.placeholderColor)
        assertEquals(TSTypeRole.Caption, resolved.messageTypeRole)
        assertEquals(16f, resolved.messageIconSizeDp, 0f)
        assertEquals(20f, resolved.leadingIconSizeDp, 0f)
        assertEquals(TSColorRole.TextMuted, resolved.leadingIconColor)
        assertEquals(44f, resolved.minHeightDp, 0f)
        assertEquals(12f, resolved.horizontalPaddingDp, 0f)
        assertEquals(10f, resolved.verticalPaddingDp, 0f)
        assertEquals(8f, resolved.spacingDp, 0f)
        assertEquals(4f, resolved.labelSpacingDp, 0f)
        assertEquals(4f, resolved.messageSpacingDp, 0f)
        assertEquals(8f, resolved.cornerRadiusDp, 0f)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} {1}")
        fun parameters(): List<Array<Any>> = TSTextFieldStyleSpec.configurations.flatMap { configuration ->
            TSTextFieldStyleSpec.states.map { arrayOf<Any>(configuration, it) }
        }
    }
}

class TSTextFieldStyleSpecMatrixTest {
    @Test
    fun matrixHas20Configurations() {
        val matrix = TSTextFieldStyleSpec.configurations.flatMap { configuration -> TSTextFieldStyleSpec.states.map { configuration to it } }
        assertEquals(20, matrix.size)
        assertEquals(20, matrix.toSet().size)
    }

    @Test
    fun codeContentStyleChangesOnlyTheInputTypeRole() {
        TSTextFieldStyleSpec.states.forEach { state ->
            listOf(false, true).forEach { readOnly ->
                val default = TSTextFieldStyleSpec.resolve(TSTextFieldStyleSpec.Configuration(readOnly = readOnly), state)
                val code = TSTextFieldStyleSpec.resolve(
                    TSTextFieldStyleSpec.Configuration(readOnly = readOnly, contentStyle = TSTextFieldContentStyle.Code),
                    state,
                )
                assertEquals(TSTypeRole.Code, code.inputTypeRole)
                assertEquals(default.copy(inputTypeRole = TSTypeRole.Code), code)
            }
        }
    }

    @Test
    fun stateBuilderAppliesThePrecedence() {
        assertEquals(TSControlState(focused = true, error = true), TSTextFieldStyleSpec.state(focused = true, error = true))
        assertEquals(TSControlState(enabled = false, error = true), TSTextFieldStyleSpec.state(enabled = false, focused = true, error = true))
        assertEquals(TSControlState.Default, TSTextFieldStyleSpec.state())
    }

    @Test
    fun disabledFocusedNeverShowsTheFocusBorder() {
        val resolved = TSTextFieldStyleSpec.resolve(TSTextFieldStyleSpec.Configuration(), TSControlState(enabled = false, focused = true))
        assertFalse(resolved.showsFocusRing)
        assertEquals(TSColorRole.BorderStrong, resolved.border)
    }

    @Test
    fun passwordToggleTokens() {
        val hidden = TSTextFieldStyleSpec.passwordToggle(revealed = false)
        assertEquals(TSIcon.Eye, hidden.icon)
        assertEquals(TSColorRole.TextMuted, hidden.iconColor)
        assertNull(hidden.fill)
        assertFalse(hidden.selected)
        assertEquals(32f, hidden.sizeDp, 0f)
        assertEquals(20f, hidden.iconSizeDp, 0f)
        assertEquals(6f, hidden.cornerRadiusDp, 0f)
        assertEquals(48f, hidden.minTouchTargetDp, 0f)

        val revealed = TSTextFieldStyleSpec.passwordToggle(revealed = true)
        assertEquals(TSIcon.Eye, revealed.icon)
        assertEquals(TSColorRole.AccentText, revealed.iconColor)
        assertEquals(TSColorRole.AccentSubtle, revealed.fill)
        assertTrue(revealed.selected)
    }

    @Test
    fun multilineGrowsFromThreeToSixLines() {
        assertEquals(3, TSTextFieldStyleSpec.MINIMUM_LINES)
        assertEquals(6, TSTextFieldStyleSpec.MAXIMUM_LINES)
    }

    @Test
    fun maskedTextReplacesEveryCharacter() {
        assertEquals("•••", TSTextFieldStyleSpec.maskedText("abc", secure = true))
        assertEquals("abc", TSTextFieldStyleSpec.maskedText("abc", secure = false))
        assertEquals("", TSTextFieldStyleSpec.maskedText("", secure = true))
    }
}
