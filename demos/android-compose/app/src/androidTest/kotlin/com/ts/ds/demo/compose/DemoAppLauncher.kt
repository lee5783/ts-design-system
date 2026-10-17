package com.ts.ds.demo.compose

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.compose.shell.DemoTags
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoSettingsStore

val settingsStore: DemoSettingsStore
    get() = DemoSettingsStore.from(InstrumentationRegistry.getInstrumentation().targetContext)

fun launchDemo(settings: DemoSettings = DemoSettings()): ActivityScenario<MainActivity> {
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        settingsStore.update { settings }
    }
    return ActivityScenario.launch(MainActivity::class.java)
}

fun ComposeTestRule.openPage(page: DemoPageId): SemanticsNodeInteraction {
    onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(page.testTag))
    onNodeWithTag(page.testTag).performClick()
    return onNodeWithTag(DemoTags.screen(page))
}
