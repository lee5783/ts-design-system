package com.tinysol.designsystem.compose.sample.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.tinysol.designsystem.compose.components.avatar.TSAvatar
import com.tinysol.designsystem.compose.theme.TSSpacing
import com.tinysol.designsystem.core.style.TSAvatarSize
import com.tinysol.designsystem.compose.sample.shell.OptionChip
import com.tinysol.designsystem.compose.sample.shell.OptionGroup
import com.tinysol.designsystem.compose.sample.shell.ToggleRow
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.sample.SampleData

object AvatarPageTags {
    const val PLAYGROUND_AVATAR = "demo.avatar.playground.avatar"
    const val PLAYGROUND_PHOTO = "demo.avatar.playground.photo"
}

fun LazyListScope.avatarPage() {
    componentSection("avatar.sizes", DemoR.string.demo_component_sizes) {
        val photo = rememberDemoSamplePhoto()
        val name = SampleData.people[0].name
        TSAvatarSize.entries.forEach { size ->
            DemoComponentRow {
                TSAvatar(name, size = size)
                TSAvatar(name, image = photo, size = size)
                DemoCaption(text = size.name.lowercase())
            }
        }
    }
    componentSection("avatar.contents", DemoR.string.demo_component_contents) {
        val photo = rememberDemoSamplePhoto()
        val name = SampleData.people[1].name
        TSAvatarSize.entries.forEach { size ->
            Row(horizontalArrangement = Arrangement.spacedBy(TSSpacing.space3), verticalAlignment = Alignment.CenterVertically) {
                TSAvatar(name, size = size)
                TSAvatar(name, image = photo, size = size)
                TSAvatar("", size = size)
            }
        }
        DemoCaption(
            text = listOf(
                stringResource(DemoR.string.demo_component_initials),
                stringResource(DemoR.string.demo_component_photo),
                stringResource(DemoR.string.demo_component_no_name),
            ).joinToString(" · "),
        )
    }
    componentSection("avatar.initials", DemoR.string.demo_component_initials) {
        SampleData.people.forEach { person ->
            DemoComponentRow {
                TSAvatar(person.name)
                DemoCaption(text = person.name)
            }
        }
    }
    componentSection("avatar.playground", DemoR.string.demo_component_playground) { AvatarPlayground() }
}

@Composable
private fun AvatarPlayground() {
    var size by rememberSaveable { mutableStateOf(TSAvatarSize.Md) }
    var personIndex by rememberSaveable { mutableIntStateOf(0) }
    var showsPhoto by rememberSaveable { mutableStateOf(false) }
    val photo = rememberDemoSamplePhoto()

    DemoCaption(text = stringResource(DemoR.string.demo_component_size))
    OptionGroup {
        TSAvatarSize.entries.forEach { option ->
            OptionChip(label = option.name.lowercase(), selected = size == option, onClick = { size = option })
        }
    }
    DemoCaption(text = stringResource(DemoR.string.demo_component_person))
    OptionGroup {
        SampleData.people.forEachIndexed { index, person ->
            OptionChip(label = person.name, selected = personIndex == index, onClick = { personIndex = index })
        }
    }
    ToggleRow(
        label = stringResource(DemoR.string.demo_component_photo),
        checked = showsPhoto,
        onCheckedChange = { showsPhoto = it },
        modifier = Modifier.testTag(AvatarPageTags.PLAYGROUND_PHOTO),
    )
    TSAvatar(
        name = SampleData.people[personIndex].name,
        image = if (showsPhoto) photo else null,
        size = size,
        modifier = Modifier.testTag(AvatarPageTags.PLAYGROUND_AVATAR),
    )
}
