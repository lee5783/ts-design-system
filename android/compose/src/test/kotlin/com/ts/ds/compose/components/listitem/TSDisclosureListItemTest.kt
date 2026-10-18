package com.ts.ds.compose.components.listitem

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.shared.LocalTSReduceMotion
import com.ts.ds.compose.components.shared.TSListItemDividerKey
import com.ts.ds.compose.components.text.TSText
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.lightTSColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSDisclosureListItemTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val changes = mutableListOf<Boolean>()
    private lateinit var hostView: View

    @Composable
    private fun Disclosure(initial: Boolean = false, enabled: Boolean = true, tag: String = "row") {
        var expanded by remember { mutableStateOf(initial) }
        TSDisclosureListItem(
            title = "Kotlin",
            subtitle = "Apache License 2.0",
            expanded = expanded,
            onExpandedChange = {
                changes += it
                expanded = it
            },
            enabled = enabled,
            modifier = Modifier.testTag(tag),
        ) {
            TSText("Licence text")
        }
    }

    private fun setContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            hostView = LocalView.current
            CompositionLocalProvider(LocalTSReduceMotion provides true) {
                TSTheme(darkTheme = false) {
                    Box(Modifier.width(320.dp)) { content() }
                }
            }
        }
    }

    private fun header() = composeRule.onNodeWithText("Kotlin")

    private fun pixelAt(tag: String, x: Dp, y: Dp): Color {
        val bounds = composeRule.onNodeWithTag(tag).getBoundsInRoot()
        val density = hostView.resources.displayMetrics.density
        val bitmap = hostView.drawToBitmap()
        return Color(bitmap.getPixel(((bounds.left + x).value * density).toInt(), ((bounds.top + y).value * density).toInt()))
    }

    @Test
    fun tapTogglesTheBody() {
        setContent { Disclosure() }
        composeRule.onNodeWithText("Licence text").assertDoesNotExist()
        header().performClick()
        composeRule.onNodeWithText("Licence text").assertExists()
        header().performClick()
        composeRule.onNodeWithText("Licence text").assertDoesNotExist()
        assertEquals(listOf(true, false), changes)
    }

    @Test
    fun disabledRowDoesNotToggle() {
        setContent { Disclosure(enabled = false) }
        header().assertIsNotEnabled()
        header().performClick()
        assertTrue(changes.isEmpty())
    }

    @Test
    fun headerHasTheRowHeightAndTheBodySitsBelowIt() {
        setContent { Disclosure(initial = true) }
        val row = composeRule.onNodeWithTag("row").getBoundsInRoot()
        val body = composeRule.onNodeWithTag(TSListItemTestTags.DISCLOSURE_BODY).getBoundsInRoot()
        assertEquals(row.top + 68.dp, body.top)
        assertEquals(row.left + 16.dp, composeRule.onNodeWithText("Licence text").getBoundsInRoot().left)
        assertEquals(row.bottom, body.bottom)
        composeRule.onNodeWithTag(TSListItemTestTags.DISCLOSURE_BODY).assertHeightIsEqualTo(24.dp + 12.dp)
    }

    @Test
    fun disclosureInAListDrawsTheGroupDivider() {
        setContent {
            Column {
                TSList {
                    TSListItem("First")
                    Disclosure(tag = "second")
                }
            }
        }
        assertTrue(header().fetchSemanticsNode().config[TSListItemDividerKey])
        assertEquals(lightTSColors().borderSubtle, pixelAt("second", 160.dp, 0.5.dp))
    }
}
