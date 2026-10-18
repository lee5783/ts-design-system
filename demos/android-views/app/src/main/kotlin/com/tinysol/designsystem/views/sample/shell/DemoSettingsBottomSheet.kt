package com.tinysol.designsystem.views.sample.shell

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.tinysol.designsystem.sample.support.settings.DemoSettings
import com.tinysol.designsystem.sample.support.settings.DemoSettingsListener
import com.tinysol.designsystem.sample.support.settings.DemoSettingsStore
import com.tinysol.designsystem.sample.support.settings.DemoTheme
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.foundation.applySystemBarInsetsAsPadding
import com.tinysol.designsystem.sample.support.R as DemoR

class DemoSettingsBottomSheet : BottomSheetDialogFragment() {
    private val store: DemoSettingsStore
        get() = DemoSettingsStore.from(requireContext())

    private val settingsListener = DemoSettingsListener { settings -> view?.let { bind(it, settings) } }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.skipCollapsed = true
        if (store.settings.reduceMotion) {
            dialog.window?.setWindowAnimations(0)
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.sheet_demo_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.tag = DemoTags.SETTINGS
        view.applySystemBarInsetsAsPadding(start = true, end = true, bottom = true)

        view.findViewById<View>(R.id.settings_close).setOnClickListener { dismiss() }
        val themes = view.findViewById<ChipGroup>(R.id.settings_theme)
        DemoTheme.entries.forEach { theme ->
            themes.addChip(getString(theme.labelRes), DemoTags.theme(theme)) {
                store.update { it.copy(theme = theme) }
            }
        }
        val fontScales = view.findViewById<ChipGroup>(R.id.settings_font_scale)
        DemoSettings.fontScales.forEach { scale ->
            val label = getString(DemoR.string.demo_settings_font_scale_value, (scale * 100).toInt())
            fontScales.addChip(label, DemoTags.fontScale(scale)) {
                store.update { it.copy(fontScale = scale) }
            }
        }
        view.findViewById<View>(R.id.settings_reset).setOnClickListener { store.reset() }
        bind(view, store.settings)
        store.addListener(settingsListener)
    }

    override fun onDestroyView() {
        store.removeListener(settingsListener)
        super.onDestroyView()
    }

    private fun bind(view: View, settings: DemoSettings) {
        DemoTheme.entries.forEach { theme ->
            view.findViewWithTag<Chip>(DemoTags.theme(theme)).isChecked = settings.theme == theme
        }
        DemoSettings.fontScales.forEach { scale ->
            view.findViewWithTag<Chip>(DemoTags.fontScale(scale)).isChecked = settings.fontScale == scale
        }
        view.findViewById<MaterialSwitch>(R.id.settings_rtl).bindToggle(settings.rtl) { checked ->
            store.update { it.copy(rtl = checked) }
        }
        view.findViewById<MaterialSwitch>(R.id.settings_reduce_motion).bindToggle(settings.reduceMotion) { checked ->
            store.update { it.copy(reduceMotion = checked) }
        }
    }

    private fun ChipGroup.addChip(label: String, tag: String, onSelect: () -> Unit) {
        val chip = LayoutInflater.from(context).inflate(R.layout.item_settings_chip, this, false) as Chip
        chip.text = label
        chip.tag = tag
        chip.isCheckable = true
        chip.setOnClickListener { onSelect() }
        addView(chip)
    }

    private fun MaterialSwitch.bindToggle(checked: Boolean, onToggle: (Boolean) -> Unit) {
        setOnCheckedChangeListener(null)
        isChecked = checked
        setOnCheckedChangeListener { _, isChecked -> onToggle(isChecked) }
    }
}
