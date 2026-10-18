package com.ts.ds.views.snapshots

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.core.style.TSTypeRole
import com.ts.ds.views.theme.textAppearance
import com.ts.ds.views.theme.tsColor
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.views.components.listitem.TSDisclosureListItemView
import com.ts.ds.views.components.listitem.TSDividerView
import com.ts.ds.views.components.listitem.TSListItemLeading
import com.ts.ds.views.components.listitem.TSListItemTrailing
import com.ts.ds.views.components.listitem.TSListItemView
import com.ts.ds.views.components.listitem.TSListSectionHeaderView
import com.ts.ds.views.components.listitem.TSListView

private const val SAMPLE_LONG_TITLE = "Automatically download updates over mobile data"
private const val SAMPLE_LONG_SUBTITLE = "Only when Wi-Fi has not been available for more than a day"

private val fillWidth: LinearLayout.LayoutParams
    get() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

private fun Context.listColumn(): LinearLayout = galleryColumn().apply {
    dividerDrawable = GradientDrawable().apply { setSize(0, dp(24f)) }
}

private fun Context.listItem(title: String, configure: TSListItemView.() -> Unit = {}): TSListItemView =
    TSListItemView(this).apply {
        this.title = title
        configure()
    }

private fun Context.list(vararg rows: View): TSListView = TSListView(this).apply {
    rows.forEach { addView(it, fillWidth) }
}

private val action = View.OnClickListener {}

internal fun listItemContentsSample(context: Context): View = context.listColumn().apply {
    addView(
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(TSListSectionHeaderView(context).apply { text = "Contents" }, fillWidth)
            addView(
                context.list(
                    context.listItem("Title only"),
                    context.listItem("Inbox") {
                        subtitle = "3 unread"
                        leading = TSListItemLeading.Icon(TSIcon.Mail)
                        trailing = TSListItemTrailing.Badge("New", TSBadgeTone.Accent)
                        showsChevron = true
                        setOnClickListener(action)
                    },
                    context.listItem("Ada Lovelace") {
                        subtitle = "Engineer"
                        leading = TSListItemLeading.Avatar("Ada Lovelace")
                        trailing = TSListItemTrailing.Text("Admin")
                    },
                    context.listItem("Wi-Fi") {
                        subtitle = "Home"
                        trailing = TSListItemTrailing.Switch
                        isChecked = true
                    },
                ),
                fillWidth,
            )
        },
        fillWidth,
    )
    addView(
        context.list(
            context.listItem(SAMPLE_LONG_TITLE) {
                subtitle = SAMPLE_LONG_SUBTITLE
                trailing = TSListItemTrailing.Text("Off")
                showsChevron = true
                setOnClickListener(action)
            },
        ),
        fillWidth,
    )
}

internal fun listItemStatesSample(context: Context): View = context.listColumn().apply {
    addView(
        context.list(
            context.listItem("Default") {
                showsChevron = true
                setOnClickListener(action)
            },
            context.listItem("Pressed") {
                showsChevron = true
                setOnClickListener(action)
                isPressed = true
            },
            context.listItem("Focused") {
                showsChevron = true
                setOnClickListener(action)
                isFocusableInTouchMode = true
                post { requestFocus() }
            },
            context.listItem("Disabled") {
                subtitle = "Not available"
                leading = TSListItemLeading.Icon(TSIcon.Lock)
                showsChevron = true
                setOnClickListener(action)
                isEnabled = false
            },
            context.listItem("Static pressed") {
                subtitle = "No action, no highlight"
                isPressed = true
            },
            context.listItem("Switch off") { trailing = TSListItemTrailing.Switch },
            context.listItem("Switch disabled") {
                trailing = TSListItemTrailing.Switch
                isChecked = true
                isEnabled = false
            },
        ),
        fillWidth,
    )
    addView(
        context.listItem("Standalone row") {
            subtitle = "Outside a list"
            setOnClickListener(action)
        },
        fillWidth,
    )
}

private fun Context.body(text: String): MaterialTextView = MaterialTextView(this).apply {
    setTextAppearance(TSTypeRole.BodySm.textAppearance)
    setTextColor(tsColor(TSColorRole.TextSecondary))
    this.text = text
}

private fun Context.disclosure(title: String, body: String, configure: TSDisclosureListItemView.() -> Unit = {}): TSDisclosureListItemView =
    TSDisclosureListItemView(this).apply {
        this.title = title
        addView(body(body))
        configure()
    }

internal fun listItemDisclosureSample(context: Context): View = context.listColumn().apply {
    addView(
        context.list(
            context.disclosure("Collapsed", "Hidden body"),
            context.disclosure("Kotlin", SAMPLE_LONG_SUBTITLE) {
                subtitle = "Apache License 2.0"
                leading = TSListItemLeading.Icon(TSIcon.Info)
                isExpanded = true
            },
            context.disclosure("Pressed", "Hidden body") { headerView.isPressed = true },
            context.disclosure("Disabled", "Dimmed body") {
                isExpanded = true
                isEnabled = false
            },
        ),
        fillWidth,
    )
    addView(context.disclosure("Standalone", "Outside a list") { isExpanded = true }, fillWidth)
}

internal fun listItemDividerSample(context: Context): View = context.listColumn().apply {
    addView(
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(context.tsColor(TSColorRole.SurfaceCard))
            addView(context.listItem("First row"), fillWidth)
            addView(TSDividerView(context), fillWidth)
            addView(context.listItem("Second row") { subtitle = "Divided with TSDividerView" }, fillWidth)
            addView(TSDividerView(context), fillWidth)
            addView(context.listItem("Third row"), fillWidth)
        },
        fillWidth,
    )
    addView(
        context.list(
            context.listItem("First row"),
            context.listItem("Second row") { subtitle = "Divided inside TSListView" },
            context.listItem("Third row"),
        ),
        fillWidth,
    )
}
