package com.ts.ds.views.components

import android.app.Activity
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ts.ds.core.logic.TSCardAction
import com.ts.ds.views.R
import com.ts.ds.views.components.card.TSCardView
import com.ts.ds.views.components.checkbox.TSCheckboxView
import com.ts.ds.views.components.switchcontrol.TSSwitchView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private class RowAdapter<T : View>(
    private val make: (ViewGroup) -> T,
    private val bind: T.(Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int = ROW_COUNT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = make(parent).apply {
            layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return object : RecyclerView.ViewHolder(view) {}
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder.itemView as T).bind(position)
    }

    companion object {
        const val ROW_COUNT = 40
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26, 36], qualifiers = "w360dp-h640dp-mdpi")
class TSRecycledAccessibilityTest {
    private fun <T : View> recycledAfterAScreenReaderStarts(make: (ViewGroup) -> T, bind: T.(Int) -> Unit): List<T> {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val context = ContextThemeWrapper(activity, R.style.Theme_TS)
        val list = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RowAdapter(make, bind)
        }
        activity.setContentView(FrameLayout(context).apply { addView(list, FrameLayout.LayoutParams(360, 600)) })
        shadowOf(Looper.getMainLooper()).idle()
        shadowOf(context.getSystemService(AccessibilityManager::class.java)).setEnabled(true)
        repeat(8) {
            list.scrollBy(0, 400)
            shadowOf(Looper.getMainLooper()).idle()
        }
        @Suppress("UNCHECKED_CAST")
        val rows = (0 until list.childCount).map { list.getChildAt(it) as T }
        assertTrue(rows.isNotEmpty())
        return rows
    }

    private fun View.event(): AccessibilityEvent = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_CLICKED).also(::onInitializeAccessibilityEvent)

    @Test
    fun cardRowsKeepTheirRoleAndLabel() {
        recycledAfterAScreenReaderStarts({ TSCardView(it.context) }) { position ->
            title = "Card $position"
            footerActions = listOf(TSCardAction("Open") {})
            setOnClickListener {}
        }.forEach { card ->
            val info: AccessibilityNodeInfo = card.createAccessibilityNodeInfo()
            assertEquals(Button::class.java.name, info.className?.toString())
            assertEquals(card.accessibilityLabel, info.contentDescription?.toString())
            assertTrue(info.actionList.any { it.label?.toString() == "Open" })
        }
    }

    @Test
    fun checkboxRowsKeepTheirRoleLabelAndState() {
        recycledAfterAScreenReaderStarts({ TSCheckboxView(it.context) }) { position ->
            label = "Option $position"
            isChecked = position % 2 == 0
        }.forEach { checkbox ->
            val info = checkbox.createAccessibilityNodeInfo()
            assertEquals(CheckBox::class.java.name, info.className?.toString())
            assertEquals(checkbox.label.toString(), info.text?.toString())
            assertTrue(info.isCheckable)
            assertEquals(checkbox.isChecked, info.isChecked)
            assertEquals(CheckBox::class.java.name, checkbox.event().className?.toString())
        }
    }

    @Test
    fun switchRowsKeepTheirRoleLabelAndState() {
        recycledAfterAScreenReaderStarts({ TSSwitchView(it.context) }) { position ->
            label = "Setting $position"
            isChecked = position % 3 == 0
        }.forEach { switch ->
            val info = switch.createAccessibilityNodeInfo()
            assertEquals(SwitchCompat::class.java.name, info.className?.toString())
            assertEquals(switch.label.toString(), info.text?.toString())
            assertTrue(info.isCheckable)
            assertEquals(switch.isChecked, info.isChecked)
            assertEquals(switch.isChecked, switch.event().isChecked)
        }
    }
}
