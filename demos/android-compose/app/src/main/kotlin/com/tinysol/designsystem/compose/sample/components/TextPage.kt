package com.tinysol.designsystem.compose.sample.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.tinysol.designsystem.compose.components.text.TSText
import com.tinysol.designsystem.compose.theme.TSSpacing
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.core.style.TSTextColor
import com.tinysol.designsystem.core.style.TSTypeRole
import com.tinysol.designsystem.sample.support.R as DemoR

object TextPageTags {
    const val TRUNCATED = "demo.text.truncated"

    fun role(role: TSTypeRole): String = "demo.text.role.${role.name.lowercase()}"

    fun color(color: TSTextColor): String = "demo.text.color.${color.name.lowercase()}"
}

fun LazyListScope.textPage() {
    componentSection("text.roles", DemoR.string.demo_component_type_roles) {
        val pangram = stringResource(DemoR.string.demo_sample_pangram)
        TSTypeRole.entries.forEach { role ->
            Column(verticalArrangement = Arrangement.spacedBy(TSSpacing.space1)) {
                DemoCaption(text = role.name.lowercase())
                TSText(pangram, Modifier.testTag(TextPageTags.role(role)), role = role)
            }
        }
    }
    componentSection("text.colors", DemoR.string.demo_component_text_colors) {
        val colors = TSTheme.colors
        TSTextColor.entries.forEach { color ->
            val background = when (color) {
                TSTextColor.Inverse -> colors.surfaceInverse
                TSTextColor.OnAccent -> colors.accentPrimary
                else -> Color.Transparent
            }
            TSText(
                color.name.lowercase(),
                Modifier
                    .testTag(TextPageTags.color(color))
                    .fillMaxWidth()
                    .background(background)
                    .padding(TSSpacing.space2),
                color = color,
            )
        }
    }
    componentSection("text.truncation", DemoR.string.demo_component_truncation) {
        TSText(
            stringResource(DemoR.string.demo_sample_long_setting),
            Modifier.testTag(TextPageTags.TRUNCATED),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
