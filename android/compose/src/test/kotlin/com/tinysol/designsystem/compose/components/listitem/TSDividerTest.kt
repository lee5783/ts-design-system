package com.tinysol.designsystem.compose.components.listitem

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.drawToBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.compose.theme.darkTSColors
import com.tinysol.designsystem.compose.theme.lightTSColors
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSDividerTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var hostView: View

    private fun setContent(dark: Boolean = false) {
        composeRule.setContent {
            hostView = LocalView.current
            TSTheme(darkTheme = dark) {
                Box(Modifier.width(320.dp)) {
                    Column {
                        TSListItem("Above")
                        TSDivider(Modifier.testTag("divider"))
                        TSListItem("Below")
                        TSList {
                            TSListItem("First")
                            TSListItem("Second", modifier = Modifier.testTag("second"))
                        }
                    }
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
    fun dividerIsFullWidthAndOneDpThick() {
        setContent()
        composeRule.onNodeWithTag("divider").assertWidthIsEqualTo(320.dp).assertHeightIsEqualTo(1.dp)
    }

    @Test
    fun dividerHasTheListDividerColour() {
        setContent()
        assertEquals(lightTSColors().borderSubtle, pixelAt("divider", 1.dp, 0.5.dp))
        assertEquals(pixelAt("second", 160.dp, 0.5.dp), pixelAt("divider", 160.dp, 0.5.dp))
    }

    @Test
    fun dividerFollowsTheDarkTheme() {
        setContent(dark = true)
        assertEquals(darkTSColors().borderSubtle, pixelAt("divider", 160.dp, 0.5.dp))
    }
}
