package com.ts.ds.demo.views.shell

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.settings.DemoSettingsListener
import com.ts.ds.demo.support.settings.DemoSettingsStore
import com.ts.ds.demo.support.settings.DemoTheme
import com.ts.ds.demo.views.R
import com.ts.ds.views.foundation.applySystemBarInsetsAsPadding

fun Fragment.setUpDemoScreen(
    root: View,
    title: String,
    @MenuRes menu: Int,
    content: View,
    showBack: Boolean = true,
) {
    setUpDemoToolbar(root.findViewById(R.id.demo_toolbar), title, menu, showBack)
    content.applySystemBarInsetsAsPadding(start = true, end = true, bottom = true)
}

fun Fragment.setUpDemoToolbar(
    toolbar: MaterialToolbar,
    title: String,
    @MenuRes menu: Int,
    showBack: Boolean = true,
) {
    toolbar.title = title
    if (showBack) {
        toolbar.setNavigationIcon(TSIcon.ChevronLeft.drawableRes)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }
    toolbar.inflateMenu(menu)
    toolbar.setOnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_theme -> {
                DemoSettingsStore.from(requireContext()).update { it.copy(theme = it.theme.next()) }
                true
            }
            R.id.action_settings -> {
                findNavController().navigate(R.id.settings)
                true
            }
            R.id.action_about -> {
                findNavController().openDestination(requireContext(), R.id.about)
                true
            }
            else -> false
        }
    }
    bindThemeAction(toolbar)
    toolbar.applySystemBarInsetsAsPadding(start = true, top = true, end = true)
}

private fun bindThemeAction(toolbar: MaterialToolbar) {
    val item = toolbar.menu.findItem(R.id.action_theme) ?: return
    val context = toolbar.context
    val store = DemoSettingsStore.from(context)
    val render = { theme: DemoTheme ->
        val label = context.getString(DemoR.string.demo_action_theme, context.getString(theme.labelRes))
        item.setIcon(theme.markRes)
        item.title = label
        item.contentDescription = label
        item.tooltipText = label
    }
    val listener = DemoSettingsListener { render(it.theme) }
    render(store.settings.theme)
    toolbar.addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {
                render(store.settings.theme)
                store.addListener(listener)
            }

            override fun onViewDetachedFromWindow(view: View) {
                store.removeListener(listener)
            }
        },
    )
    toolbar.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        view.findViewById<View>(R.id.action_theme)?.tag = DemoTags.ACTION_THEME
    }
}

fun ViewGroup.addTokenRow(
    name: String,
    value: String,
    preview: ((FrameLayout) -> Unit)? = null,
): View {
    val row = LayoutInflater.from(context).inflate(R.layout.item_token_row, this, false)
    row.findViewById<TextView>(R.id.token_name).text = name
    row.findViewById<TextView>(R.id.token_value).text = value
    val previewHost = row.findViewById<FrameLayout>(R.id.token_preview)
    if (preview == null) {
        previewHost.visibility = View.GONE
    } else {
        preview(previewHost)
    }
    addView(row)
    return row
}

fun Context.dpToPx(dp: Float): Int = (dp * resources.displayMetrics.density).toInt()

fun Context.formatPx(px: Float): String {
    val dp = px / resources.displayMetrics.density
    val rounded = Math.round(dp * 2) / 2f
    return if (rounded % 1f == 0f) "${rounded.toInt()}dp" else "${rounded}dp"
}
