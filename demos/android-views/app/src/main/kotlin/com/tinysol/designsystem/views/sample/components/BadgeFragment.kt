package com.tinysol.designsystem.views.sample.components

import android.widget.LinearLayout
import com.tinysol.designsystem.core.icon.TSIcon
import com.tinysol.designsystem.core.style.TSBadgeTone
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment
import com.tinysol.designsystem.views.components.badge.TSBadgeView

class BadgeFragment : DemoPageFragment(R.layout.page_badge) {
    override val page: DemoPageId = DemoPageId.Badge

    private var playgroundTone = TSBadgeTone.Neutral
    private var playgroundShowsIcon = false

    override fun bindRows(rows: LinearLayout) {
        bindTones(rows.findViewById(R.id.badge_tones))
        bindWithDot(rows.findViewById(R.id.badge_with_dot))
        bindWithIcons(rows.findViewById(R.id.badge_with_icons))
        bindPlayground(rows.findViewById(R.id.badge_playground))
    }

    private fun bindTones(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_tones)
        TSBadgeTone.entries.forEach { tone ->
            val row = section.addFlowRow()
            row.addView(badge(section, tone))
            row.addCaption(tone.name.lowercase())
        }
    }

    private fun bindWithDot(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_with_dot)
        val row = section.addFlowRow()
        TSBadgeTone.entries.forEach { tone -> row.addView(badge(section, tone).apply { showsDot = true }) }
    }

    private fun bindWithIcons(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_with_icons)
        val row = section.addFlowRow()
        TSBadgeTone.entries.forEach { tone -> row.addView(badge(section, tone).apply { icon = sampleIcon(tone) }) }
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        val badge = badge(section, playgroundTone).apply { tag = PLAYGROUND_BADGE }
        section.addCaption(getString(DemoR.string.demo_component_tone))
        section.addChoiceGroup(TSBadgeTone.entries, { it.name.lowercase() }, playgroundTone) {
            playgroundTone = it
            badge.tone = it
            badge.text = getString(sampleText(it))
            badge.icon = if (playgroundShowsIcon) sampleIcon(it) else null
        }
        section.addToggle(DemoR.string.demo_component_dot, tag = PLAYGROUND_DOT) { badge.showsDot = it }
        section.addToggle(DemoR.string.demo_component_icon, tag = PLAYGROUND_ICON) {
            playgroundShowsIcon = it
            badge.icon = if (it) sampleIcon(playgroundTone) else null
        }
        section.addSpaced(badge)
    }

    private fun badge(section: LinearLayout, tone: TSBadgeTone) = TSBadgeView(section.context).apply {
        text = getString(sampleText(tone))
        this.tone = tone
    }

    private fun sampleText(tone: TSBadgeTone): Int = when (tone) {
        TSBadgeTone.Neutral -> DemoR.string.demo_sample_draft
        TSBadgeTone.Accent -> DemoR.string.demo_sample_new
        TSBadgeTone.Success -> DemoR.string.demo_sample_active
        TSBadgeTone.Warning -> DemoR.string.demo_sample_pending
        TSBadgeTone.Danger -> DemoR.string.demo_sample_failed
        TSBadgeTone.Info -> DemoR.string.demo_sample_beta
    }

    private fun sampleIcon(tone: TSBadgeTone): TSIcon = when (tone) {
        TSBadgeTone.Neutral -> TSIcon.Edit
        TSBadgeTone.Accent -> TSIcon.Star
        TSBadgeTone.Success -> TSIcon.CheckCircle
        TSBadgeTone.Warning -> TSIcon.Alert
        TSBadgeTone.Danger -> TSIcon.XCircle
        TSBadgeTone.Info -> TSIcon.Info
    }

    companion object {
        const val PLAYGROUND_BADGE = "demo.badge.playground.badge"
        const val PLAYGROUND_DOT = "demo.badge.playground.dot"
        const val PLAYGROUND_ICON = "demo.badge.playground.icon"
    }
}
