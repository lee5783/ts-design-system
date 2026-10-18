package com.tinysol.designsystem.compose.sample.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import com.tinysol.designsystem.core.style.TSColorRole
import com.tinysol.designsystem.compose.sample.shell.DemoText
import com.tinysol.designsystem.sample.support.R as DemoR
import com.tinysol.designsystem.sample.support.catalog.DemoPageId

fun LazyListScope.pendingComponentPage(page: DemoPageId) {
    item(key = page.id) {
        DemoText(text = stringResource(DemoR.string.demo_page_pending), color = TSColorRole.TextSecondary)
    }
}
