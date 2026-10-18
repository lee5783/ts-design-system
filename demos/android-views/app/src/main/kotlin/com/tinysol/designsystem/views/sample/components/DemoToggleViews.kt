package com.tinysol.designsystem.views.sample.components

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.tinysol.designsystem.sample.support.R as DemoR

class DemoToggleViewSpec<T : View>(
    val page: String,
    @StringRes val stateSampleRes: Int,
    @StringRes val playgroundSampleRes: Int,
    @StringRes val onRes: Int,
    @StringRes val offRes: Int,
    val create: (Context) -> T,
    val bind: T.(label: CharSequence, checked: Boolean, labelHidden: Boolean) -> Unit,
    val setLabelHidden: T.(Boolean) -> Unit,
    val onChange: T.((Boolean) -> Unit) -> Unit,
) {
    val playgroundControlTag: String get() = "demo.$page.playground.$page"
    val playgroundDisabledTag: String get() = "demo.$page.playground.disabled"
    val playgroundLabelHiddenTag: String get() = "demo.$page.playground.label-hidden"
    val playgroundValueTag: String get() = "demo.$page.playground.value"
    val playgroundCountTag: String get() = "demo.$page.playground.count"
}

fun <T : View> LinearLayout.bindToggleStates(spec: DemoToggleViewSpec<T>) {
    listOf(false, true).forEach { disabled ->
        listOf(false, true).forEach { checked ->
            val row = addFlowRow()
            row.addView(
                spec.create(context).apply {
                    spec.bind(this, context.getString(spec.stateSampleRes), checked, false)
                    isEnabled = !disabled
                },
            )
            val value = context.getString(if (checked) spec.onRes else spec.offRes)
            row.addCaption(if (disabled) context.getString(DemoR.string.demo_component_value_disabled, value) else value)
        }
    }
}

fun <T : View> LinearLayout.bindTogglePlayground(spec: DemoToggleViewSpec<T>) {
    addSectionHeader(DemoR.string.demo_component_playground)
    val control = spec.create(context).apply {
        tag = spec.playgroundControlTag
        spec.bind(this, context.getString(spec.playgroundSampleRes), false, false)
    }
    addToggle(DemoR.string.demo_component_disabled, tag = spec.playgroundDisabledTag) { control.isEnabled = !it }
    addToggle(DemoR.string.demo_component_label_hidden, tag = spec.playgroundLabelHiddenTag) { spec.setLabelHidden(control, it) }
    addSpaced(control, matchWidth = true)
    val value = addCaption(context.getString(spec.offRes)).apply { tag = spec.playgroundValueTag }
    val counter = addTapCounter(spec.playgroundCountTag)
    spec.onChange(control) { checked ->
        counter.increment()
        value.text = context.getString(if (checked) spec.onRes else spec.offRes)
    }
}
