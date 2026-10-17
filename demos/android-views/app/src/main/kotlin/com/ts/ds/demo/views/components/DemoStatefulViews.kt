package com.ts.ds.demo.views.components

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.BundleCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ts.ds.demo.support.R as DemoR

class DemoChoiceGroup(context: Context) : ChipGroup(context) {
    private val selectedIndex: Int
        get() = (0 until childCount).indexOfFirst { (getChildAt(it) as? Chip)?.isChecked == true }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable(KEY_SUPER, super.onSaveInstanceState())
        putInt(KEY_SELECTED, selectedIndex)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(BundleCompat.getParcelable(state, KEY_SUPER, Parcelable::class.java))
        (getChildAt(state.getInt(KEY_SELECTED, -1)) as? Chip)?.isChecked = true
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    private companion object {
        const val KEY_SUPER = "super"
        const val KEY_SELECTED = "selected"
    }
}

class DemoTapCounter(context: Context) : AppCompatTextView(context) {
    var count: Int = 0
        set(value) {
            field = value
            text = resources.getQuantityString(DemoR.plurals.demo_component_tap_count, value, value)
        }

    fun increment() {
        count += 1
    }

    override fun onSaveInstanceState(): Parcelable = Bundle().apply {
        putParcelable(KEY_SUPER, super.onSaveInstanceState())
        putInt(KEY_COUNT, count)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is Bundle) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(BundleCompat.getParcelable(state, KEY_SUPER, Parcelable::class.java))
        count = state.getInt(KEY_COUNT)
    }

    private companion object {
        const val KEY_SUPER = "super"
        const val KEY_COUNT = "count"
    }
}
