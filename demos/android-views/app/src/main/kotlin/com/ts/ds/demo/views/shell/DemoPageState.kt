package com.ts.ds.demo.views.shell

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.ts.ds.demo.views.R
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class DemoPageState {
    private var values = Bundle()

    fun restore(savedInstanceState: Bundle?) {
        savedInstanceState?.getBundle(KEY)?.let { values = it }
    }

    fun save(outState: Bundle) {
        outState.putBundle(KEY, values)
    }

    fun int(default: Int): ReadWriteProperty<Any?, Int> = property({ getInt(it, default) }, Bundle::putInt)

    fun boolean(default: Boolean): ReadWriteProperty<Any?, Boolean> = property({ getBoolean(it, default) }, Bundle::putBoolean)

    fun string(default: String? = null): ReadWriteProperty<Any?, String?> = property({ getString(it, default) }, Bundle::putString)

    private fun <T> property(read: Bundle.(String) -> T, write: Bundle.(String, T) -> Unit): ReadWriteProperty<Any?, T> =
        object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T = values.read(property.name)

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                values.write(property.name, value)
            }
        }

    private companion object {
        const val KEY = "demo.page.state"
    }
}

fun ViewGroup.assignStableIds() {
    val pool = resources.obtainTypedArray(R.array.demo_stable_ids)
    var next = 0
    fun visit(view: View) {
        if (view.id == View.NO_ID) {
            check(next < pool.length()) { "assignStableIds: the page needs more than ${pool.length()} stable ids; add items to demo_stable_ids in ids.xml" }
            view.id = pool.getResourceId(next++, View.NO_ID)
        }
        if (view is ViewGroup && !view.javaClass.name.startsWith(LIBRARY_PACKAGE)) {
            (0 until view.childCount).forEach { visit(view.getChildAt(it)) }
        }
    }
    try {
        visit(this)
    } finally {
        pool.recycle()
    }
}

private const val LIBRARY_PACKAGE = "com.ts.ds.views."
