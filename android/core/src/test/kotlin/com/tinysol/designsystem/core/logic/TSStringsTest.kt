package com.tinysol.designsystem.core.logic

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.core.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class TSStringsTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun libraryStringsResolve() {
        assertEquals("Loading", context.getString(R.string.ts_loading))
        assertEquals("Close", context.getString(R.string.ts_close))
        assertEquals("Dismiss", context.getString(R.string.ts_dismiss))
        assertEquals("Show password", context.getString(R.string.ts_text_field_show_password))
        assertEquals("Hide password", context.getString(R.string.ts_text_field_hide_password))
    }

    @Test
    fun badgeOverflowUsesTheStringResource() {
        assertEquals("99+", TSBadgeCountFormatter.format(context, 100))
        assertEquals("42", TSBadgeCountFormatter.format(context, 42))
    }
}
