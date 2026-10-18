package com.ts.ds.demo.views.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.chip.Chip
import com.google.android.material.materialswitch.MaterialSwitch
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.demo.views.R
import com.ts.ds.tokens.R as TokensR
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor

fun LinearLayout.addSectionHeader(@StringRes title: Int): TextView =
    (LayoutInflater.from(context).inflate(R.layout.item_section_header, this, false) as TextView).also {
        it.setText(title)
        addView(it)
    }

fun ViewGroup.addCaption(text: CharSequence): TextView = TextView(context).also {
    it.setTextAppearance(TSTypeRole.Caption.textAppearance)
    it.setTextColor(context.tsColor(TSColorRole.TextSecondary))
    it.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    it.text = text
    it.freezesText = true
    addView(it)
}

fun ViewGroup.addFlowRow(): DemoFlowRow = DemoFlowRow(context).also {
    addView(it, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
}

fun ViewGroup.addSpaced(view: View, matchWidth: Boolean = false): View {
    val params = LinearLayout.LayoutParams(
        if (matchWidth) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    ).apply { bottomMargin = resources.getDimensionPixelSize(TokensR.dimen.ts_space_2) }
    addView(view, params)
    return view
}

fun <T> ViewGroup.addChoiceGroup(options: List<T>, label: (T) -> String, selected: T, tag: (T) -> String? = { null }, onSelect: (T) -> Unit) {
    val group = DemoChoiceGroup(context).apply { isSingleSelection = true; isSelectionRequired = true }
    options.forEach { option ->
        val chip = LayoutInflater.from(context).inflate(R.layout.item_settings_chip, group, false) as Chip
        chip.text = label(option)
        chip.isCheckable = true
        chip.isChecked = option == selected
        chip.tag = tag(option)
        chip.setOnCheckedChangeListener { _, checked -> if (checked) onSelect(option) }
        group.addView(chip)
    }
    addView(group)
}

fun ViewGroup.addToggle(@StringRes label: Int, checked: Boolean = false, tag: String? = null, onToggle: (Boolean) -> Unit): MaterialSwitch {
    val toggle = LayoutInflater.from(context).inflate(R.layout.item_demo_switch, this, false) as MaterialSwitch
    toggle.setText(label)
    toggle.isChecked = checked
    toggle.tag = tag
    toggle.setOnCheckedChangeListener { _, isChecked -> onToggle(isChecked) }
    addView(toggle)
    return toggle
}

fun ViewGroup.addTapCounter(tag: String): DemoTapCounter = DemoTapCounter(context).also {
    it.setTextAppearance(TSTypeRole.BodySm.textAppearance)
    it.setTextColor(context.tsColor(TSColorRole.TextSecondary))
    it.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    it.tag = tag
    it.count = 0
    addView(it)
}
