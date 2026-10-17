package com.ts.ds.core.logic

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.core.R
import com.ts.ds.core.style.TSAlertTone
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
        assertEquals("2 of 5", context.getString(R.string.ts_position_of_total, 2, 5))
    }

    @Test
    fun textFieldStringsResolve() {
        assertEquals("Error: Too short", TSTextFieldAccessibility.errorDescription(context, "Too short"))
        assertEquals("Show password", TSTextFieldAccessibility.toggleLabel(context, revealed = false))
        assertEquals("Hide password", TSTextFieldAccessibility.toggleLabel(context, revealed = true))
    }

    @Test
    fun textFieldHintIsDroppedWhileThereIsAnError() {
        assertEquals("Hint", TSTextFieldAccessibility.hint("Hint", error = null))
        assertEquals("Hint", TSTextFieldAccessibility.hint("Hint", error = " "))
        assertEquals(null, TSTextFieldAccessibility.hint("Hint", error = "Bad"))
        assertEquals(null, TSTextFieldAccessibility.hint(" ", error = null))
        assertEquals(true, TSTextFieldAccessibility.hasError("Bad"))
        assertEquals(false, TSTextFieldAccessibility.hasError(""))
    }

    @Test
    fun toggleStatesResolve() {
        assertEquals("Checked", TSToggleAccessibility.checkboxState(context, checked = true))
        assertEquals("Unchecked", TSToggleAccessibility.checkboxState(context, checked = false))
        assertEquals("On", TSToggleAccessibility.switchState(context, checked = true))
        assertEquals("Off", TSToggleAccessibility.switchState(context, checked = false))
    }

    @Test
    fun tabPositionIsOneBased() {
        assertEquals("Tab, 2 of 4", TSTabsAccessibility.position(context, index = 1, count = 4))
    }

    @Test
    fun bottomNavValueAddsTheBadgeAfterThePosition() {
        assertEquals("3 new", context.getString(R.string.ts_bottom_nav_badge, "3"))
        assertEquals("Tab, 2 of 4", TSBottomNavAccessibility.value(context, index = 1, count = 4, badge = null))
        assertEquals("Tab, 2 of 4", TSBottomNavAccessibility.value(context, index = 1, count = 4, badge = 0))
        assertEquals("Tab, 2 of 4, 3 new", TSBottomNavAccessibility.value(context, index = 1, count = 4, badge = 3))
        assertEquals("Tab, 4 of 5, 99+ new", TSBottomNavAccessibility.value(context, index = 3, count = 5, badge = 120))
        assertEquals("2 of 4", TSTabsAccessibility.position(context, index = 1, count = 4, includesRole = false))
        assertEquals("2 of 4, 3 new", TSBottomNavAccessibility.value(context, index = 1, count = 4, badge = 3, includesRole = false))
        assertEquals("99+", TSBottomNavAccessibility.badgeText(context, 100))
        assertEquals(null, TSBottomNavAccessibility.badgeText(context, null))
    }

    @Test
    fun listItemNavigationHintIsTheNavigatesString() {
        assertEquals("Opens another screen", TSListItemAccessibility.hint(context, TSListItemRole.Navigation))
        assertEquals(null, TSListItemAccessibility.hint(context, TSListItemRole.Action))
        assertEquals(null, TSListItemAccessibility.hint(context, TSListItemRole.Toggle))
        assertEquals(null, TSListItemAccessibility.hint(context, TSListItemRole.Plain))
    }

    @Test
    fun alertToneNamesAndLabel() {
        assertEquals("Information", TSAlertAccessibility.toneName(context, TSAlertTone.Info))
        assertEquals("Success", TSAlertAccessibility.toneName(context, TSAlertTone.Success))
        assertEquals("Warning", TSAlertAccessibility.toneName(context, TSAlertTone.Warning))
        assertEquals("Error", TSAlertAccessibility.toneName(context, TSAlertTone.Danger))
        assertEquals(
            "Warning, Storage almost full, Free up space",
            TSAlertAccessibility.label(context, TSAlertTone.Warning, "Storage almost full", "Free up space"),
        )
        assertEquals("Error, Upload failed", TSAlertAccessibility.label(context, TSAlertTone.Danger, null, "Upload failed"))
        assertEquals("Information, Saved", TSAlertAccessibility.label(context, TSAlertTone.Info, "", "Saved"))
    }

    @Test
    fun badgeOverflowUsesTheStringResource() {
        assertEquals("99+", TSBadgeCountFormatter.format(context, 100))
        assertEquals("42", TSBadgeCountFormatter.format(context, 42))
    }
}
