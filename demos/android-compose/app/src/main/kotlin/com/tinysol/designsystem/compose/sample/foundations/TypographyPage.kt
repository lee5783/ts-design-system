package com.tinysol.designsystem.compose.sample.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tinysol.designsystem.compose.theme.TSShapes
import com.tinysol.designsystem.compose.theme.TSSpacing
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.style
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.core.style.TSTypeRole
import com.tinysol.designsystem.compose.sample.shell.DemoText
import com.tinysol.designsystem.sample.support.sample.SampleData

fun LazyListScope.typographyPage() {
    items(TSTypeRole.entries, key = { it.name }) { role ->
        val style = TSTheme.typography.style(role)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TSTheme.colors.surfaceCard, TSShapes.radiusMd)
                .padding(TSSpacing.space3),
            verticalArrangement = Arrangement.spacedBy(TSSpacing.space1),
        ) {
            DemoText(
                text = "${role.name} · ${style.fontSize.value.toInt()}/${style.lineHeight.value.toInt()} · ${style.fontWeight?.weight ?: 400}",
                role = TSTypeRole.Caption,
                color = TSColorRole.TextSecondary,
            )
            val sample = stringResource(SampleData.pangramRes)
            DemoText(text = if (role == TSTypeRole.Overline) sample.uppercase() else sample, role = role)
        }
    }
}
