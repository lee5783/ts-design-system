package com.tinysol.designsystem.views.sample.components

import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.tinysol.designsystem.core.style.TSAvatarSize
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId
import com.tinysol.designsystem.sample.support.sample.SampleData
import com.tinysol.designsystem.views.sample.R
import com.tinysol.designsystem.views.sample.shell.DemoPageFragment
import com.tinysol.designsystem.tokens.R as TokensR
import com.tinysol.designsystem.views.components.avatar.TSAvatarView

class AvatarFragment : DemoPageFragment(R.layout.page_avatar) {
    override val page: DemoPageId = DemoPageId.Avatar

    private var playgroundSize = TSAvatarSize.Md
    private var playgroundPerson = 0

    override fun bindRows(rows: LinearLayout) {
        bindSizes(rows.findViewById(R.id.avatar_sizes))
        bindContents(rows.findViewById(R.id.avatar_contents))
        bindInitials(rows.findViewById(R.id.avatar_initials))
        bindPlayground(rows.findViewById(R.id.avatar_playground))
    }

    private fun bindSizes(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_sizes)
        val name = SampleData.people[0].name
        TSAvatarSize.entries.forEach { size ->
            val row = section.addFlowRow()
            row.addView(avatar(section, name, size))
            row.addView(avatar(section, name, size).apply { image = demoSamplePhoto(context) })
            row.addCaption(size.name.lowercase())
        }
    }

    private fun bindContents(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_contents)
        val name = SampleData.people[1].name
        TSAvatarSize.entries.forEach { size ->
            val row = LinearLayout(section.context).apply { gravity = Gravity.CENTER_VERTICAL }
            listOf(
                avatar(section, name, size),
                avatar(section, name, size).apply { image = demoSamplePhoto(context) },
                avatar(section, "", size),
            ).forEach { row.addView(it, spacedParams(section)) }
            section.addSpaced(row)
        }
        section.addCaption(
            listOf(DemoR.string.demo_component_initials, DemoR.string.demo_component_photo, DemoR.string.demo_component_no_name)
                .joinToString(" · ") { getString(it) },
        )
    }

    private fun bindInitials(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_initials)
        SampleData.people.forEach { person ->
            val row = section.addFlowRow()
            row.addView(avatar(section, person.name, TSAvatarSize.Md))
            row.addCaption(person.name)
        }
    }

    private fun bindPlayground(section: LinearLayout) {
        section.addSectionHeader(DemoR.string.demo_component_playground)
        val avatar = avatar(section, SampleData.people[playgroundPerson].name, playgroundSize).apply { tag = PLAYGROUND_AVATAR }
        section.addCaption(getString(DemoR.string.demo_component_size))
        section.addChoiceGroup(TSAvatarSize.entries, { it.name.lowercase() }, playgroundSize) {
            playgroundSize = it
            avatar.size = it
        }
        section.addCaption(getString(DemoR.string.demo_component_person))
        section.addChoiceGroup(SampleData.people.indices.toList(), { SampleData.people[it].name }, playgroundPerson) {
            playgroundPerson = it
            avatar.name = SampleData.people[it].name
        }
        section.addToggle(DemoR.string.demo_component_photo, tag = PLAYGROUND_PHOTO) {
            avatar.image = if (it) demoSamplePhoto(section.context) else null
        }
        section.addSpaced(avatar)
    }

    private fun avatar(section: LinearLayout, name: String, size: TSAvatarSize) = TSAvatarView(section.context).apply {
        this.name = name
        this.size = size
    }

    private fun spacedParams(section: LinearLayout) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    ).apply { marginEnd = section.resources.getDimensionPixelSize(TokensR.dimen.ts_space_3) }

    companion object {
        const val PLAYGROUND_AVATAR = "demo.avatar.playground.avatar"
        const val PLAYGROUND_PHOTO = "demo.avatar.playground.photo"
    }
}
