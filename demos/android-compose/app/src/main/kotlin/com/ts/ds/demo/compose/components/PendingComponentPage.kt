package com.ts.ds.demo.compose.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import com.ts.ds.core.style.TSColorRole
import com.ts.ds.demo.compose.shell.DemoText
import com.ts.ds.demo.support.R as DemoR
import com.ts.ds.demo.support.catalog.DemoPageId

fun LazyListScope.pendingComponentPage(page: DemoPageId) {
    item(key = page.id) {
        DemoText(text = stringResource(DemoR.string.demo_page_pending), color = TSColorRole.TextSecondary)
    }
}
