package com.ts.ds.views.components

import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.input.TSTextInputTraits
import com.ts.ds.core.style.TSBottomNavItem
import com.ts.ds.core.style.TSTabItem
import com.ts.ds.views.R
import com.ts.ds.views.components.bottomnav.TSBottomNavView
import com.ts.ds.views.components.checkbox.TSCheckboxView
import com.ts.ds.views.components.listitem.TSListItemTrailing
import com.ts.ds.views.components.listitem.TSListItemView
import com.ts.ds.views.components.switchcontrol.TSSwitchView
import com.ts.ds.views.components.tabs.TSTabsView
import com.ts.ds.views.components.textfield.TSTextFieldView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

class RestorationHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TS)
        super.onCreate(savedInstanceState)
        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                addView(TSTextFieldView(context).apply { id = NAME_ID; label = "Name" })
                addView(TSTextFieldView(context).apply { id = PASSWORD_ID; label = "Password"; isSecure = true; inputTraits = TSTextInputTraits.Password })
                addView(TSCheckboxView(context).apply { id = CHECKBOX_ID; label = "Accept" })
                addView(TSSwitchView(context).apply { id = SWITCH_ID; label = "Wi-Fi" })
                addView(
                    TSTabsView(context).apply {
                        id = TABS_ID
                        items = listOf(TSTabItem("all", "All"), TSTabItem("open", "Open"), TSTabItem("done", "Done"))
                        selectedId = "all"
                    },
                )
                addView(
                    TSBottomNavView(context).apply {
                        id = NAV_ID
                        items = listOf(
                            TSBottomNavItem("home", "Home", TSIcon.Home),
                            TSBottomNavItem("inbox", "Inbox", TSIcon.Mail),
                            TSBottomNavItem("profile", "Profile", TSIcon.User),
                        )
                        selectedId = "home"
                    },
                )
                addView(TSListItemView(context).apply { id = WIFI_ROW_ID; title = "Wi-Fi"; trailing = TSListItemTrailing.Switch; isChecked = true })
                addView(TSListItemView(context).apply { id = BLUETOOTH_ROW_ID; title = "Bluetooth"; trailing = TSListItemTrailing.Switch })
                addView(TSCheckboxView(context).apply { label = "No id" })
            },
        )
    }

    companion object {
        val NAME_ID: Int = View.generateViewId()
        val PASSWORD_ID: Int = View.generateViewId()
        val CHECKBOX_ID: Int = View.generateViewId()
        val SWITCH_ID: Int = View.generateViewId()
        val TABS_ID: Int = View.generateViewId()
        val NAV_ID: Int = View.generateViewId()
        val WIFI_ROW_ID: Int = View.generateViewId()
        val BLUETOOTH_ROW_ID: Int = View.generateViewId()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36])
class TSViewStateRestorationTest {
    private val controller: ActivityController<RestorationHostActivity> =
        Robolectric.buildActivity(RestorationHostActivity::class.java).setup()

    private inline fun <reified T : View> view(id: Int): T = controller.get().findViewById(id)

    private fun recreate() {
        controller.recreate()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun textFieldsKeepTheirOwnTextAndSelectionWithoutColliding() {
        view<TSTextFieldView>(RestorationHostActivity.NAME_ID).apply {
            text = "Ada Lovelace"
            editText.setSelection(3, 8)
        }
        view<TSTextFieldView>(RestorationHostActivity.PASSWORD_ID).text = "hunter2"
        recreate()
        val name = view<TSTextFieldView>(RestorationHostActivity.NAME_ID)
        assertEquals("Ada Lovelace", name.text.toString())
        assertEquals(3, name.editText.selectionStart)
        assertEquals(8, name.editText.selectionEnd)
        assertEquals("hunter2", view<TSTextFieldView>(RestorationHostActivity.PASSWORD_ID).text.toString())
    }

    @Test
    fun secureFieldKeepsItsRevealState() {
        view<TSTextFieldView>(RestorationHostActivity.PASSWORD_ID).apply {
            text = "hunter2"
            passwordToggle.performClick()
            assertTrue(isPasswordRevealed)
        }
        recreate()
        val password = view<TSTextFieldView>(RestorationHostActivity.PASSWORD_ID)
        assertTrue(password.isPasswordRevealed)
        assertEquals("hunter2", password.text.toString())
    }

    @Test
    fun checkboxKeepsItsCheckedState() {
        view<TSCheckboxView>(RestorationHostActivity.CHECKBOX_ID).isChecked = true
        recreate()
        assertTrue(view<TSCheckboxView>(RestorationHostActivity.CHECKBOX_ID).isChecked)
    }

    @Test
    fun switchKeepsItsCheckedStateAndThumbPosition() {
        view<TSSwitchView>(RestorationHostActivity.SWITCH_ID).isChecked = true
        recreate()
        val restored = view<TSSwitchView>(RestorationHostActivity.SWITCH_ID)
        assertTrue(restored.isChecked)
        assertEquals(1f, restored.thumbProgress, 0f)
    }

    @Test
    fun uncheckingIsRestoredToo() {
        view<TSCheckboxView>(RestorationHostActivity.CHECKBOX_ID).isChecked = true
        recreate()
        view<TSCheckboxView>(RestorationHostActivity.CHECKBOX_ID).isChecked = false
        recreate()
        assertFalse(view<TSCheckboxView>(RestorationHostActivity.CHECKBOX_ID).isChecked)
    }

    @Test
    fun tabsKeepTheirSelection() {
        view<TSTabsView>(RestorationHostActivity.TABS_ID).selectedId = "done"
        recreate()
        assertEquals("done", view<TSTabsView>(RestorationHostActivity.TABS_ID).selectedId)
    }

    @Test
    fun bottomNavKeepsItsSelection() {
        view<TSBottomNavView>(RestorationHostActivity.NAV_ID).selectedId = "profile"
        recreate()
        assertEquals("profile", view<TSBottomNavView>(RestorationHostActivity.NAV_ID).selectedId)
    }

    @Test
    fun listItemSwitchRowsKeepTheirCheckedStateAndThumbPosition() {
        view<TSListItemView>(RestorationHostActivity.WIFI_ROW_ID).switchView.performClick()
        view<TSListItemView>(RestorationHostActivity.BLUETOOTH_ROW_ID).isChecked = true
        recreate()
        val wifi = view<TSListItemView>(RestorationHostActivity.WIFI_ROW_ID)
        assertFalse(wifi.isChecked)
        assertEquals(0f, wifi.switchView.thumbProgress, 0f)
        val bluetooth = view<TSListItemView>(RestorationHostActivity.BLUETOOTH_ROW_ID)
        assertTrue(bluetooth.isChecked)
        assertEquals(1f, bluetooth.switchView.thumbProgress, 0f)
    }
}
