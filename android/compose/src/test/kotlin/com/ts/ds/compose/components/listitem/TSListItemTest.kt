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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.avatar.TSAvatar
import com.ts.ds.compose.components.shared.LocalTSForcedControlState
import com.ts.ds.compose.components.shared.LocalTSReduceMotion
import com.ts.ds.compose.components.shared.TSListItemDividerKey
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.compose.theme.lightTSColors
import com.ts.ds.core.haptics.DsHapticEvent
import com.ts.ds.core.haptics.DsHaptics
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.style.TSControlState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@Composable
private fun SwitchRow(
    onChange: (Boolean) -> Unit,
    initial: Boolean = false,
    enabled: Boolean = true,
    subtitle: String? = "Home",
    tag: String = "row",
) {
    var checked by remember { mutableStateOf(initial) }
    TSListItem(
        title = "Wi-Fi",
        subtitle = subtitle,
        checked = checked,
        onCheckedChange = {
            onChange(it)
            checked = it
        },
        enabled = enabled,
        modifier = Modifier.testTag(tag),
    )
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSListItemTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var taps = 0
    private val changes = mutableListOf<Boolean>()
    private lateinit var hostView: View

    private fun setContent(fontScale: Float = 1f, forced: TSControlState? = null, content: @Composable () -> Unit) {
        composeRule.setContent {
            hostView = LocalView.current
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalTSForcedControlState provides forced,
                LocalTSReduceMotion provides true,
            ) {
                TSTheme(darkTheme = false) {
                    Box(Modifier.width(320.dp)) { content() }
                }
            }
        }
    }

    private fun pixelAt(tag: String, x: Dp, y: Dp): Color {
        val bounds = composeRule.onNodeWithTag(tag).getBoundsInRoot()
        val density = hostView.resources.displayMetrics.density
        val bitmap = hostView.drawToBitmap()
        return Color(bitmap.getPixel(((bounds.left + x).value * density).toInt(), ((bounds.top + y).value * density).toInt()))
    }

    @Test
    fun actionRowFires() {
        setContent { TSListItem("Inbox", onClick = { taps++ }, modifier = Modifier.testTag("row")) }

        composeRule.onNodeWithTag("row").assertIsEnabled().performClick()

        assertEquals(1, taps)
    }

    @Test
    fun disabledRowBlocksItsAction() {
        setContent { TSListItem("Inbox", onClick = { taps++ }, enabled = false, modifier = Modifier.testTag("row")) }

        composeRule.onNodeWithTag("row").assertIsNotEnabled().performClick()

        assertEquals(0, taps)
    }

    @Test
    fun plainRowHasNoClickAction() {
        setContent {
            TSListItem("Storage", subtitle = "12 GB used", modifier = Modifier.testTag("row"), trailing = { TSListItemValue("80%") })
        }

        assertNull(composeRule.onNodeWithTag("row").fetchSemanticsNode().config.getOrNull(SemanticsActions.OnClick))
    }

    @Test
    fun chevronRowShowsTheChevron() {
        setContent { TSListItem("Settings", onClick = {}, showsChevron = true, modifier = Modifier.testTag("row")) }

        composeRule.onNodeWithTag(TSListItemTestTags.CHEVRON, useUnmergedTree = true).assertHeightIsEqualTo(20.dp)
    }

    @Test
    fun pressedDrawsSurfacePressedOnlyWithAnAction() {
        setContent(forced = TSControlState.Pressed) {
            Column {
                TSListItem("Inbox", onClick = {}, modifier = Modifier.testTag("action"))
                TSListItem("Storage", modifier = Modifier.testTag("plain"))
                SwitchRow({ changes += it })
            }
        }

        val pressed = pixelAt("action", 4.dp, 4.dp)
        val plain = pixelAt("plain", 4.dp, 4.dp)
        assertEquals(lightTSColors().surfacePressed, pressed)
        assertNotEquals(pressed, plain)
        assertEquals(0f, plain.alpha, 0f)
        assertEquals(0f, pixelAt("row", 4.dp, 4.dp).alpha, 0f)
    }

    @Test
    fun focusedActionRowUsesSurfaceHover() {
        setContent(forced = TSControlState.Focused) {
            TSListItem("Inbox", onClick = {}, modifier = Modifier.testTag("row"))
        }

        assertEquals(lightTSColors().surfaceHover, pixelAt("row", 4.dp, 4.dp))
    }

    @Test
    fun switchRowTogglesFromItsSwitch() {
        setContent { SwitchRow({ changes += it }) }

        composeRule.onNodeWithTag(TSListItemTestTags.SWITCH, useUnmergedTree = true).assertIsOff().performClick()
        composeRule.onNodeWithTag(TSListItemTestTags.SWITCH, useUnmergedTree = true).assertIsOn()

        assertEquals(listOf(true), changes)
        assertEquals(DsHaptics.constantFor(DsHapticEvent.ToggleOn), shadowOf(hostView).lastHapticFeedbackPerformed())
    }

    @Test
    fun onlyTheSwitchTakesTapsOnASwitchRow() {
        setContent { SwitchRow({ changes += it }) }

        composeRule.onNodeWithTag("row").performTouchInput { click(Offset(24.dp.toPx(), centerY)) }
        assertEquals(emptyList<Boolean>(), changes)

        composeRule.onNodeWithTag(TSListItemTestTags.SWITCH, useUnmergedTree = true).performClick()
        assertEquals(listOf(true), changes)
    }

    @Test
    fun switchHitAreaReachesTheTouchTargetOutsideTheTrack() {
        setContent { SwitchRow({ changes += it }) }
        val row = composeRule.onNodeWithTag("row").getBoundsInRoot()
        val track = composeRule.onNodeWithTag(TSListItemTestTags.SWITCH, useUnmergedTree = true).getBoundsInRoot()

        composeRule.onNodeWithTag("row").performTouchInput {
            click(Offset(((track.left + track.right) / 2f - row.left).toPx(), (track.top - row.top - 10.dp).toPx()))
        }

        assertEquals(listOf(true), changes)
    }

    @Test
    fun disabledSwitchRowBlocksTheSwitch() {
        setContent { SwitchRow({ changes += it }, enabled = false) }

        composeRule.onNodeWithTag(TSListItemTestTags.SWITCH, useUnmergedTree = true).assertIsNotEnabled().performClick()

        assertEquals(emptyList<Boolean>(), changes)
    }

    @Test
    fun heightsMatchTheDesignAtTheDefaultSize() {
        setContent {
            Column {
                TSListItem("Title", modifier = Modifier.testTag("title"))
                TSListItem("Title", subtitle = "Subtitle", modifier = Modifier.testTag("subtitle"))
                TSListItem("Mail", modifier = Modifier.testTag("tile"), leading = { TSListItemIcon(TSIcon.Mail) })
                TSListItem("Ada", modifier = Modifier.testTag("avatar"), leading = { TSAvatar("Ada Lovelace") })
                SwitchRow({ changes += it })
                SwitchRow({ changes += it }, subtitle = null, tag = "switch")
            }
        }

        composeRule.onNodeWithTag("title").assertHeightIsEqualTo(48.dp)
        composeRule.onNodeWithTag("subtitle").assertHeightIsEqualTo(68.dp)
        composeRule.onNodeWithTag("tile").assertHeightIsEqualTo(60.dp)
        composeRule.onNodeWithTag("avatar").assertHeightIsEqualTo(64.dp)
        composeRule.onNodeWithTag("row").assertHeightIsEqualTo(68.dp)
        composeRule.onNodeWithTag("switch").assertHeightIsEqualTo(50.dp)
    }

    @Test
    fun largeFontStacksLeadingTextAndTrailing() {
        setContent(fontScale = 2f) {
            TSListItem(
                "A long title that has to wrap at large text",
                subtitle = "Subtitle",
                onClick = {},
                showsChevron = true,
                modifier = Modifier.testTag("row"),
                leading = { TSListItemIcon(TSIcon.Mail, Modifier.testTag("leading")) },
                trailing = { TSListItemValue("Meta", Modifier.testTag("trailing")) },
            )
        }

        val leading = composeRule.onNodeWithTag("leading", useUnmergedTree = true).getBoundsInRoot()
        val trailing = composeRule.onNodeWithTag("trailing", useUnmergedTree = true).getBoundsInRoot()
        val chevron = composeRule.onNodeWithTag(TSListItemTestTags.CHEVRON, useUnmergedTree = true).getBoundsInRoot()
        assertTrue(trailing.top > leading.bottom + 48.dp)
        assertEquals(leading.left.value, trailing.left.value, 0.5f)
        assertTrue(chevron.left > trailing.right)
    }

    @Test
    fun rowsInsideAListDrawTheGroupDivider() {
        setContent {
            Column {
                TSList {
                    TSListItem("First", modifier = Modifier.testTag("first"))
                    TSListItem("Second", modifier = Modifier.testTag("second"))
                }
                TSListItem("Alone", modifier = Modifier.testTag("alone"))
            }
        }

        assertTrue(composeRule.onNodeWithTag("second").fetchSemanticsNode().config[TSListItemDividerKey])
        assertFalse(composeRule.onNodeWithTag("alone").fetchSemanticsNode().config[TSListItemDividerKey])
        assertEquals(lightTSColors().borderSubtle, pixelAt("second", 160.dp, 0.5.dp))
    }
}
