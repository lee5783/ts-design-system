package com.ts.ds.demo.compose

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ts.ds.demo.compose.shell.DemoTags
import com.ts.ds.demo.support.catalog.DemoCatalog
import com.ts.ds.demo.support.catalog.DemoPageId
import com.ts.ds.demo.support.settings.DemoLaunchOptions
import com.ts.ds.demo.support.settings.DemoSettings
import com.ts.ds.demo.support.settings.DemoTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogSmokeTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun resetSettings() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { settingsStore.reset() }
    }

    @Test
    fun launchListsEveryCatalogPage() {
        launchDemo().use {
            composeRule.onNodeWithTag(DemoTags.CATALOG).assertIsDisplayed()
            DemoCatalog.allPages.forEach { page ->
                composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag(page.testTag))
                composeRule.onNodeWithTag(page.testTag).assertIsDisplayed()
            }
        }
    }

    @Test
    fun everyFoundationPageOpensAndReturnsToTheCatalog() {
        launchDemo().use {
            DemoCatalog.foundationPages.forEach { page ->
                composeRule.openPage(page).assertIsDisplayed()
                composeRule.onNodeWithTag(DemoTags.SCREEN_TITLE).assertTextEquals(context.getString(page.titleRes))
                Espresso.pressBack()
                composeRule.onNodeWithTag(DemoTags.CATALOG).assertIsDisplayed()
            }
        }
    }

    @Test
    fun everyComponentPageOpensFromTheCatalog() {
        launchDemo().use {
            DemoCatalog.componentPages.forEach { page ->
                composeRule.openPage(page).assertIsDisplayed()
                composeRule.onNodeWithTag(DemoTags.ACTION_BACK).performClick()
                composeRule.onNodeWithTag(DemoTags.CATALOG).assertIsDisplayed()
            }
        }
    }

    @Test
    fun themeToggleSwitchesBetweenDarkAndLight() {
        launchDemo().use {
            composeRule.onNodeWithTag(DemoTags.ACTION_SETTINGS).performClick()
            composeRule.onNodeWithTag(DemoTags.SETTINGS).assertIsDisplayed()
            composeRule.onNodeWithTag(DemoTags.theme(DemoTheme.System)).assertIsSelected()

            composeRule.onNodeWithTag(DemoTags.theme(DemoTheme.Dark)).performClick()
            composeRule.onNodeWithTag(DemoTags.theme(DemoTheme.Dark)).assertIsSelected()
            composeRule.onNodeWithTag(DemoTags.theme(DemoTheme.System)).assertIsNotSelected()
            assertEquals(DemoTheme.Dark, settingsStore.settings.theme)

            composeRule.onNodeWithTag(DemoTags.theme(DemoTheme.Light)).performClick()
            composeRule.onNodeWithTag(DemoTags.theme(DemoTheme.Light)).assertIsSelected()
            assertEquals(DemoTheme.Light, settingsStore.settings.theme)

            composeRule.onNodeWithTag(DemoTags.ACTION_CLOSE_SETTINGS).performClick()
            composeRule.onNodeWithTag(DemoTags.SETTINGS).assertDoesNotExist()
        }
    }

    @Test
    fun settingsApplyFontScaleRtlAndReduceMotion() {
        launchDemo().use {
            composeRule.onNodeWithTag(DemoTags.ACTION_SETTINGS).performClick()
            composeRule.onNodeWithTag(DemoTags.fontScale(2f)).performClick()
            composeRule.onNodeWithTag(DemoTags.fontScale(2f)).assertIsSelected()
            composeRule.onNodeWithTag(DemoTags.SETTING_RTL).performScrollTo().assertIsOff().performClick()
            composeRule.onNodeWithTag(DemoTags.SETTING_RTL).assertIsOn()
            composeRule.onNodeWithTag(DemoTags.SETTING_REDUCE_MOTION).performScrollTo().performClick()
            composeRule.onNodeWithTag(DemoTags.SETTING_REDUCE_MOTION).assertIsOn()
            assertEquals(DemoSettings(fontScale = 2f, rtl = true, reduceMotion = true), settingsStore.settings)

            composeRule.onNodeWithTag(DemoTags.ACTION_RESET_SETTINGS).performScrollTo().performClick()
            assertEquals(DemoSettings(), settingsStore.settings)
        }
    }

    @Test
    fun backClosesTheSettingsSheetBeforeLeavingThePage() {
        launchDemo().use {
            composeRule.openPage(DemoCatalog.foundationPages.first())
            composeRule.onNodeWithTag(DemoTags.ACTION_SETTINGS).performClick()
            Espresso.pressBack()
            composeRule.onNodeWithTag(DemoTags.SETTINGS).assertDoesNotExist()
            composeRule.onNodeWithTag(DemoTags.screen(DemoCatalog.foundationPages.first())).assertIsDisplayed()
        }
    }

    @Test
    fun launchOptionsOpenAPageWithTheGivenSettings() {
        launchDemo().close()
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(DemoLaunchOptions.PAGE, DemoPageId.Typography.id)
            .putExtra(DemoLaunchOptions.THEME, DemoTheme.Dark.id)
            .putExtra(DemoLaunchOptions.FONT_SCALE, "2")
            .putExtra(DemoLaunchOptions.RIGHT_TO_LEFT, "YES")
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.onNodeWithTag(DemoTags.screen(DemoPageId.Typography)).assertIsDisplayed()
            assertEquals(DemoSettings(theme = DemoTheme.Dark, fontScale = 2f, rtl = true), settingsStore.settings)
        }
    }

    @Test
    fun aboutShowsTheLibraryAndDesignVersions() {
        launchDemo().use {
            composeRule.onNodeWithTag(DemoTags.ACTION_ABOUT).performClick()
            composeRule.onNodeWithTag(DemoTags.ABOUT).assertIsDisplayed()
            composeRule.onAllNodesWithText(BuildConfig.DS_LIBRARY_VERSION).onFirst().assertIsDisplayed()
            composeRule.onNodeWithText(BuildConfig.DS_DESIGN_VERSION).assertIsDisplayed()
        }
    }
}
