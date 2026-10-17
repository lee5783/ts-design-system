package com.ts.ds.demo.support.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoSettingsStoreTest {
    private class InMemoryStorage(var value: String? = null) : DemoSettingsStorage {
        var saves = 0

        override fun load(): String? = value

        override fun save(encoded: String) {
            value = encoded
            saves += 1
        }
    }

    @Test
    fun emptyStorageGivesDefaults() {
        assertEquals(DemoSettings(), DemoSettingsStore(InMemoryStorage()).settings)
    }

    @Test
    fun settingsRoundTripThroughStorage() {
        val storage = InMemoryStorage()
        val expected = DemoSettings(DemoTheme.Dark, fontScale = 2f, rtl = true, reduceMotion = true)
        DemoSettingsStore(storage).update { expected }
        assertEquals(expected, DemoSettingsStore(storage).settings)
    }

    @Test
    fun malformedOrUnsupportedValuesFallBackToDefaults() {
        val settings = DemoSettings.decode("theme=neon;fontScale=7;rtl=maybe;garbage")
        assertEquals(DemoSettings(), settings)
    }

    @Test
    fun listenersReceiveChangesUntilRemoved() {
        val store = DemoSettingsStore(InMemoryStorage())
        val received = mutableListOf<DemoSettings>()
        val listener = DemoSettingsListener { received += it }
        store.addListener(listener)
        store.update { it.copy(rtl = true) }
        store.removeListener(listener)
        store.update { it.copy(reduceMotion = true) }
        assertEquals(listOf(DemoSettings(rtl = true)), received)
    }

    @Test
    fun unchangedUpdatesAreNotSaved() {
        val storage = InMemoryStorage()
        val store = DemoSettingsStore(storage)
        store.update { it }
        assertEquals(0, storage.saves)
        assertNull(storage.value)
    }

    @Test
    fun resetRestoresDefaults() {
        val store = DemoSettingsStore(InMemoryStorage())
        store.update { it.copy(theme = DemoTheme.Light, fontScale = 1.5f) }
        store.reset()
        assertEquals(DemoSettings(), store.settings)
    }

    @Test
    fun themeResolvesAgainstTheSystem() {
        assertTrue(DemoTheme.System.isDark(systemIsDark = true))
        assertFalse(DemoTheme.System.isDark(systemIsDark = false))
        assertFalse(DemoTheme.Light.isDark(systemIsDark = true))
        assertTrue(DemoTheme.Dark.isDark(systemIsDark = false))
    }
}
