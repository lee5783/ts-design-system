package com.ts.ds.core.style

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TSControlStateMakeTest {
    @Test
    fun loadingWinsOverDisabled() {
        val state = TSControlState.make(enabled = false, loading = true)

        assertEquals(TSControlState.Loading, state)
        assertFalse(state.interactive)
        assertFalse(state.dimmed)
    }

    @Test
    fun disabledWinsOverPressed() {
        val state = TSControlState.make(pressed = true, enabled = false)

        assertEquals(TSControlState.Disabled, state)
        assertTrue(state.dimmed)
    }

    @Test
    fun loadingDropsPressAndKeepsFocusOnlyWhenEnabled() {
        assertEquals(TSControlState.Loading.copy(focused = true), TSControlState.make(pressed = true, loading = true, focused = true))
        assertEquals(TSControlState.Disabled, TSControlState.make(enabled = false, focused = true))
    }

    @Test
    fun plainFlagsMapToTheNamedStates() {
        assertEquals(TSControlState.Default, TSControlState.make())
        assertEquals(TSControlState.Pressed, TSControlState.make(pressed = true))
        assertEquals(TSControlState.Focused, TSControlState.make(focused = true))
    }
}
