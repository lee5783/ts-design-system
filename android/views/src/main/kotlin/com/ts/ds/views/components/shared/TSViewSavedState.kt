package com.ts.ds.views.components.shared

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class TSViewSavedState : View.BaseSavedState {
    val values: Bundle

    constructor(superState: Parcelable?, values: Bundle) : super(superState) {
        this.values = values
    }

    private constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
        values = source.readBundle(loader ?: TSViewSavedState::class.java.classLoader) ?: Bundle()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeBundle(values)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.ClassLoaderCreator<TSViewSavedState> = object : Parcelable.ClassLoaderCreator<TSViewSavedState> {
            override fun createFromParcel(source: Parcel, loader: ClassLoader?): TSViewSavedState = TSViewSavedState(source, loader)

            override fun createFromParcel(source: Parcel): TSViewSavedState = TSViewSavedState(source, null)

            override fun newArray(size: Int): Array<TSViewSavedState?> = arrayOfNulls(size)
        }
    }
}

internal inline fun restoreTSViewState(state: Parcelable?, restoreSuper: (Parcelable?) -> Unit, restore: (Bundle) -> Unit) {
    if (state !is TSViewSavedState) {
        restoreSuper(state)
        return
    }
    restoreSuper(state.superState)
    restore(state.values)
}
