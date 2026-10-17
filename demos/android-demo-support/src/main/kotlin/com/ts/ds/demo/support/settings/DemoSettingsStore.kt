package com.ts.ds.demo.support.settings

import android.content.Context
import android.content.SharedPreferences

public interface DemoSettingsStorage {
    public fun load(): String?

    public fun save(encoded: String)
}

public fun interface DemoSettingsListener {
    public fun onSettingsChanged(settings: DemoSettings)
}

public class DemoSettingsStore(private val storage: DemoSettingsStorage) {
    private val listeners = mutableListOf<DemoSettingsListener>()

    public var settings: DemoSettings = DemoSettings.decode(storage.load())
        private set

    public fun update(transform: (DemoSettings) -> DemoSettings) {
        val updated = transform(settings)
        if (updated == settings) return
        settings = updated
        storage.save(updated.encode())
        listeners.toList().forEach { it.onSettingsChanged(updated) }
    }

    public fun reset() {
        update { DemoSettings() }
    }

    public fun addListener(listener: DemoSettingsListener) {
        listeners += listener
    }

    public fun removeListener(listener: DemoSettingsListener) {
        listeners -= listener
    }

    public companion object {
        private const val PREFERENCES_NAME = "demo_settings"
        private const val KEY_SETTINGS = "settings"

        @Volatile
        private var shared: DemoSettingsStore? = null

        public fun from(context: Context): DemoSettingsStore = shared ?: synchronized(this) {
            shared ?: DemoSettingsStore(
                SharedPreferencesStorage(
                    context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
                ),
            ).also { shared = it }
        }
    }

    private class SharedPreferencesStorage(private val preferences: SharedPreferences) : DemoSettingsStorage {
        override fun load(): String? = preferences.getString(KEY_SETTINGS, null)

        override fun save(encoded: String) {
            preferences.edit().putString(KEY_SETTINGS, encoded).apply()
        }
    }
}
