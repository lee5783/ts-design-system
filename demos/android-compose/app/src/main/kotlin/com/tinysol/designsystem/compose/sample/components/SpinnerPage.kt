package com.tinysol.designsystem.compose.sample.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import com.tinysol.designsystem.compose.components.spinner.TSSpinner
import com.tinysol.designsystem.compose.components.text.TSText
import com.tinysol.designsystem.compose.theme.TSSpacing
import com.tinysol.designsystem.core.style.TSSpinnerSize
import com.tinysol.designsystem.core.style.TSSpinnerTone
import com.tinysol.designsystem.core.style.TSTextColor
import com.tinysol.designsystem.core.style.TSTypeRole
import com.tinysol.designsystem.sample.support.R as DemoR

object SpinnerPageTags {
    const val IN_CONTEXT = "demo.spinner.in-context"
    const val LABELLED = "demo.spinner.labelled"

    fun size(size: TSSpinnerSize): String = "demo.spinner.size.${size.name.lowercase()}"

    fun tone(tone: TSSpinnerTone): String = "demo.spinner.tone.${tone.name.lowercase()}"
}

fun LazyListScope.spinnerPage() {
    componentSection("spinner.sizes", DemoR.string.demo_component_sizes) {
        DemoComponentRow {
            TSSpinnerSize.entries.forEach { size -> TSSpinner(Modifier.testTag(SpinnerPageTags.size(size)), size = size) }
        }
    }
    componentSection("spinner.tones", DemoR.string.demo_component_tones) {
        TSSpinnerTone.entries.forEach { tone ->
            DemoComponentRow {
                TSSpinner(Modifier.testTag(SpinnerPageTags.tone(tone)), tone = tone)
                DemoCaption(text = tone.name.lowercase())
            }
        }
    }
    componentSection("spinner.in-context", DemoR.string.demo_component_in_context) {
        val loading = stringResource(DemoR.string.demo_sample_loading_messages)
        Row(
            modifier = Modifier.testTag(SpinnerPageTags.IN_CONTEXT).semantics(mergeDescendants = true) {},
            horizontalArrangement = Arrangement.spacedBy(TSSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TSSpinner(size = TSSpinnerSize.Sm, tone = TSSpinnerTone.Secondary)
            TSText(loading, role = TSTypeRole.BodySm, color = TSTextColor.Secondary)
        }
        TSSpinner(Modifier.testTag(SpinnerPageTags.LABELLED), size = TSSpinnerSize.Lg, tone = TSSpinnerTone.Accent)
    }
}
