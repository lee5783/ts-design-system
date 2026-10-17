package com.ts.ds.views.snapshots

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ts.ds.views.components.checkbox.TSCheckboxView
import com.ts.ds.views.components.switchcontrol.TSSwitchView

private const val SAMPLE_LONG_CONSENT = "Email me about product updates, tips and occasional offers from the team"
private const val SAMPLE_LONG_SETTING = "Download updates automatically over mobile data when Wi-Fi is not available"

private enum class ToggleSampleState { Default, Pressed, Disabled }

private val fillWidth = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

private fun sampleLabel(checked: Boolean, state: String): String = "${if (checked) "On" else "Off"} ${state.lowercase()}"

private fun View.focusWhenAttached() {
    isFocusableInTouchMode = true
    post { requestFocus() }
}

private fun LinearLayout.addToggleSamples(context: Context, create: (Context) -> View, configure: View.(checked: Boolean, label: String, hidden: Boolean) -> Unit) {
    listOf(false, true).forEach { checked ->
        ToggleSampleState.entries.forEach { state ->
            addView(
                create(context).apply {
                    configure(checked, sampleLabel(checked, state.name), false)
                    isEnabled = state != ToggleSampleState.Disabled
                    isPressed = state == ToggleSampleState.Pressed
                },
                LinearLayout.LayoutParams(fillWidth),
            )
        }
    }
    addView(create(context).apply { configure(true, sampleLabel(true, "focused"), false); focusWhenAttached() }, LinearLayout.LayoutParams(fillWidth))
}

private fun LinearLayout.addHiddenLabelRow(context: Context, create: (Context) -> View, configure: View.(checked: Boolean, label: String, hidden: Boolean) -> Unit) {
    addView(
        context.galleryRow().apply {
            addView(create(context).apply { configure(false, "Hidden off", true) })
            addView(create(context).apply { configure(true, "Hidden on", true) })
        },
    )
}

private val checkboxConfiguration: View.(Boolean, String, Boolean) -> Unit = { checked, label, hidden ->
    (this as TSCheckboxView).also {
        it.label = label
        it.isChecked = checked
        it.isLabelHidden = hidden
    }
}

private val switchConfiguration: View.(Boolean, String, Boolean) -> Unit = { checked, label, hidden ->
    (this as TSSwitchView).also {
        it.label = label
        it.isChecked = checked
        it.isLabelHidden = hidden
    }
}

internal fun checkboxStatesSample(context: Context): View = context.galleryColumn().apply {
    addToggleSamples(context, ::TSCheckboxView, checkboxConfiguration)
    addView(TSCheckboxView(context).apply { checkboxConfiguration(true, SAMPLE_LONG_CONSENT, false) }, LinearLayout.LayoutParams(fillWidth))
    addHiddenLabelRow(context, ::TSCheckboxView, checkboxConfiguration)
}

internal fun switchStatesSample(context: Context): View = context.galleryColumn().apply {
    addToggleSamples(context, ::TSSwitchView, switchConfiguration)
    addView(TSSwitchView(context).apply { switchConfiguration(true, SAMPLE_LONG_SETTING, false) }, LinearLayout.LayoutParams(fillWidth))
    addHiddenLabelRow(context, ::TSSwitchView, switchConfiguration)
}
