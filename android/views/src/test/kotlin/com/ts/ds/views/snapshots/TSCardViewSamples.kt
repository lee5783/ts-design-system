package com.ts.ds.views.snapshots

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSCardAction
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSCardStyleSpec
import com.ts.ds.views.components.badge.TSBadgeView
import com.ts.ds.views.components.card.TSCardView
import com.ts.ds.views.components.iconbutton.TSIconButtonView

private val sampleFooter = listOf(
    TSCardAction("Share") {},
    TSCardAction("Open", TSButtonVariant.Primary) {},
)

private val fillWidth: LinearLayout.LayoutParams
    get() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

private val action = View.OnClickListener {}

private fun Context.cardColumn(): LinearLayout = galleryColumn().apply {
    dividerDrawable = GradientDrawable().apply { setSize(0, dp(TSCardStyleSpec.STACK_SPACING_DP)) }
}

private fun Context.card(configure: TSCardView.() -> Unit): TSCardView = TSCardView(this).apply(configure)

internal fun cardVariantsSample(context: Context): View = context.cardColumn().apply {
    addView(
        context.card {
            title = "Pro plan"
            subtitle = "Billed monthly"
            bodyText = "Usage resets on the first of every month."
            accessoryView = TSBadgeView(context).apply {
                text = "Active"
                tone = TSBadgeTone.Success
            }
        },
        fillWidth,
    )
    addView(
        context.card {
            title = "Shared folder"
            bodyText = "Ada shared 3 files with you."
            isRaised = true
            accessoryView = TSIconButtonView(context).apply {
                icon = TSIcon.More
                label = "More options"
                setOnClickListener(action)
            }
        },
        fillWidth,
    )
    addView(
        context.card {
            title = "Trial"
            bodyText = "Your trial ends in 3 days."
            footerActions = sampleFooter
        },
        fillWidth,
    )
    addView(context.card { bodyText = "A card with a body only." }, fillWidth)
}

internal fun cardStatesSample(context: Context): View = context.cardColumn().apply {
    addView(
        context.card {
            title = "Default"
            subtitle = "Interactive"
            bodyText = "Tap to open the project."
            footerActions = sampleFooter
            setOnClickListener(action)
        },
        fillWidth,
    )
    addView(
        context.card {
            title = "Pressed"
            bodyText = "Pressed fill on an interactive card."
            setOnClickListener(action)
            isPressed = true
        },
        fillWidth,
    )
    addView(
        context.card {
            title = "Focused"
            bodyText = "Keyboard focus ring."
            setOnClickListener(action)
            isFocusableInTouchMode = true
            post { requestFocus() }
        },
        fillWidth,
    )
    addView(
        context.card {
            title = "Disabled"
            bodyText = "Not available right now."
            footerActions = sampleFooter
            setOnClickListener(action)
            isEnabled = false
        },
        fillWidth,
    )
    addView(
        context.card {
            title = "Raised pressed"
            bodyText = "Raised and pressed."
            isRaised = true
            setOnClickListener(action)
            isPressed = true
        },
        fillWidth,
    )
}
