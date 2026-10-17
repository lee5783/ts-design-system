package com.ts.ds.compose.snapshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.components.badge.TSBadge
import com.ts.ds.compose.components.card.TSCard
import com.ts.ds.compose.components.iconbutton.TSIconButton
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSCardAction
import com.ts.ds.core.style.TSBadgeTone
import com.ts.ds.core.style.TSButtonVariant
import com.ts.ds.core.style.TSCardStyleSpec
import com.ts.ds.core.style.TSControlState

private val sampleFooter = listOf(
    TSCardAction("Share") {},
    TSCardAction("Open", TSButtonVariant.Primary) {},
)

@Composable
private fun CardSampleColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TSTheme.colors.backgroundPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(TSCardStyleSpec.STACK_SPACING_DP.dp),
    ) { content() }
}

@Composable
private fun ForcedCard(state: TSControlState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTSForcedControlState provides state, content = content)
}

@Composable
internal fun CardVariantsSample() {
    CardSampleColumn {
        TSCard(text = "Usage resets on the first of every month.", title = "Pro plan", subtitle = "Billed monthly", accessory = { TSBadge("Active", tone = TSBadgeTone.Success) })
        TSCard(
            text = "Ada shared 3 files with you.",
            title = "Shared folder",
            raised = true,
            accessory = { TSIconButton(TSIcon.More, "More options", onClick = {}) },
        )
        TSCard(text = "Your trial ends in 3 days.", title = "Trial", footerActions = sampleFooter)
        TSCard(text = "A card with a body only.")
    }
}

@Composable
internal fun CardStatesSample() {
    CardSampleColumn {
        TSCard(text = "Tap to open the project.", title = "Default", subtitle = "Interactive", onClick = {}, footerActions = sampleFooter)
        ForcedCard(TSControlState.Pressed) {
            TSCard(text = "Pressed fill on an interactive card.", title = "Pressed", onClick = {})
        }
        ForcedCard(TSControlState.Focused) {
            TSCard(text = "Keyboard focus ring.", title = "Focused", onClick = {})
        }
        TSCard(text = "Not available right now.", title = "Disabled", onClick = {}, enabled = false, footerActions = sampleFooter)
        ForcedCard(TSControlState.Pressed) {
            TSCard(text = "Raised and pressed.", title = "Raised pressed", raised = true, onClick = {})
        }
    }
}
