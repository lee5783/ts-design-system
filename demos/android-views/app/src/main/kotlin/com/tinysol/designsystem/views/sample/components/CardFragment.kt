package com.tinysol.designsystem.views.sample.components

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.logic.TSCardAccessory
import com.tinysol.designsystem.core.logic.TSCardAction
import com.tinysol.designsystem.core.style.TSBadgeTone
import com.tinysol.designsystem.core.style.TSButtonVariant
import com.tinysol.designsystem.core.style.TSCardStyleSpec
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment
import com.tinysol.designsystem.views.components.badge.TSBadgeView
import com.tinysol.designsystem.views.components.card.TSCardView
import kotlin.math.roundToInt

class CardFragment : DemoPageFragment(R.layout.page_card) {
    override val page: DemoPageId = DemoPageId.Card

    private var playgroundSubtitle = true
    private var playgroundFooter = false
    private var playgroundInteractive = true

    override fun bindRows(rows: LinearLayout) {
        bindVariants(rows.findViewById(R.id.card_variants))
        rows.findViewById<TSCardView>(R.id.card_invite).footerActions = listOf(
            TSCardAction(getString(DemoR.string.demo_sample_cancel)) {},
            TSCardAction(getString(DemoR.string.demo_sample_send_invite), TSButtonVariant.Primary) {},
        )
        bindStates(rows.findViewById(R.id.card_states))
        bindPlayground(rows.findViewById(R.id.card_playground))
    }

    private fun ViewGroup.addCard(configure: TSCardView.() -> Unit): TSCardView {
        val card = TSCardView(context).apply(configure)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = (TSCardStyleSpec.STACK_SPACING_DP * resources.displayMetrics.density).roundToInt()
        }
        addView(card, params)
        return card
    }

    private fun bindVariants(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_variants)
        section.addCard {
            title = getString(DemoR.string.demo_sample_weekly_report)
            subtitle = getString(DemoR.string.demo_sample_updated_today)
            bodyText = getString(DemoR.string.demo_sample_report_body)
            accessoryView = TSBadgeView(context).apply {
                text = getString(DemoR.string.demo_sample_new)
                tone = TSBadgeTone.Accent
            }
        }
        section.addCard {
            title = getString(DemoR.string.demo_component_raised)
            bodyText = getString(DemoR.string.demo_sample_report_body)
            isRaised = true
            accessory = TSCardAccessory.IconButton(TSIcon.More, getString(DemoR.string.demo_sample_more_options)) {}
        }
    }

    private fun bindStates(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_states)
        lateinit var counter: DemoTapCounter
        lateinit var lastAction: TextView
        val countTap = View.OnClickListener { counter.increment() }
        val share = getString(DemoR.string.demo_sample_share)
        val open = getString(DemoR.string.demo_sample_open)
        section.addCard {
            title = getString(DemoR.string.demo_component_default)
            subtitle = getString(DemoR.string.demo_component_interactive)
            bodyText = getString(DemoR.string.demo_sample_report_body)
            footerActions = listOf(
                TSCardAction(share) { lastAction.text = getString(DemoR.string.demo_component_last_action, share) },
                TSCardAction(open, TSButtonVariant.Primary) { lastAction.text = getString(DemoR.string.demo_component_last_action, open) },
            )
            tag = STATE_DEFAULT
            setOnClickListener(countTap)
        }
        section.addCard {
            title = getString(DemoR.string.demo_component_disabled)
            subtitle = getString(DemoR.string.demo_component_interactive)
            bodyText = getString(DemoR.string.demo_sample_report_body)
            tag = STATE_DISABLED
            setOnClickListener(countTap)
            isEnabled = false
        }
        counter = section.addTapCounter(STATE_COUNT)
        lastAction = section.addCaption(getString(DemoR.string.demo_component_last_action, getString(DemoR.string.demo_component_none))).apply {
            tag = STATE_LAST_ACTION
        }
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        lateinit var card: TSCardView
        lateinit var counter: DemoTapCounter
        section.addToggle(DemoR.string.demo_component_subtitle, checked = true, tag = PLAYGROUND_SUBTITLE) {
            playgroundSubtitle = it
            configurePlayground(card, counter)
        }
        section.addToggle(DemoR.string.demo_component_raised, tag = PLAYGROUND_RAISED) { card.isRaised = it }
        section.addToggle(DemoR.string.demo_component_interactive, checked = true, tag = PLAYGROUND_INTERACTIVE) {
            playgroundInteractive = it
            configurePlayground(card, counter)
        }
        section.addToggle(DemoR.string.demo_component_footer, tag = PLAYGROUND_FOOTER) {
            playgroundFooter = it
            configurePlayground(card, counter)
        }
        section.addToggle(DemoR.string.demo_component_disabled, tag = PLAYGROUND_DISABLED) { card.isEnabled = !it }
        card = section.addCard {
            title = getString(DemoR.string.demo_sample_weekly_report)
            bodyText = getString(DemoR.string.demo_sample_report_body)
            tag = PLAYGROUND_CARD
        }
        counter = section.addTapCounter(PLAYGROUND_COUNT)
        configurePlayground(card, counter)
    }

    private fun configurePlayground(card: TSCardView, counter: DemoTapCounter) {
        card.subtitle = if (playgroundSubtitle) getString(DemoR.string.demo_sample_updated_today) else null
        card.footerActions = if (playgroundFooter) {
            listOf(TSCardAction(getString(DemoR.string.demo_sample_open), TSButtonVariant.Primary) { counter.increment() })
        } else {
            emptyList()
        }
        card.setOnClickListener(if (playgroundInteractive) View.OnClickListener { counter.increment() } else null)
    }

    companion object {
        const val STATE_DEFAULT = "demo.card.state.default"
        const val STATE_DISABLED = "demo.card.state.disabled"
        const val STATE_COUNT = "demo.card.state.count"
        const val STATE_LAST_ACTION = "demo.card.state.last-action"
        const val PLAYGROUND_SUBTITLE = "demo.card.playground.subtitle"
        const val PLAYGROUND_RAISED = "demo.card.playground.raised"
        const val PLAYGROUND_INTERACTIVE = "demo.card.playground.interactive"
        const val PLAYGROUND_FOOTER = "demo.card.playground.footer"
        const val PLAYGROUND_DISABLED = "demo.card.playground.disabled"
        const val PLAYGROUND_CARD = "demo.card.playground.card"
        const val PLAYGROUND_COUNT = "demo.card.playground.count"
    }
}
