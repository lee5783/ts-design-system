package com.ts.ds.compose.components.avatar

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ts.ds.compose.components.shared.TSAvatarContentKey
import com.ts.ds.compose.theme.TSTheme
import com.ts.ds.core.style.TSAvatarContent
import com.ts.ds.core.style.TSAvatarSize
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TSAvatarTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val photo: Painter
        get() = BitmapPainter(Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).asImageBitmap())

    private val emptyPainter = object : Painter() {
        override val intrinsicSize: Size = Size.Zero

        override fun DrawScope.onDraw() = Unit
    }

    private fun setAvatar(fontScale: Float = 1f, content: @Composable () -> Unit) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                TSTheme(darkTheme = false) { content() }
            }
        }
    }

    private fun assertContent(expected: TSAvatarContent) {
        composeRule.onNodeWithTag("avatar").assert(SemanticsMatcher.expectValue(TSAvatarContentKey, expected))
    }

    @Test
    fun labelIsTheNameWithTheImageRoleAndNoClick() {
        setAvatar { TSAvatar("Ada Lovelace", modifier = Modifier.testTag("avatar")) }

        composeRule.onNodeWithContentDescription("Ada Lovelace")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image))
            .assert(!hasClickAction())
        composeRule.onNodeWithTag("avatar").assertContentDescriptionEquals("Ada Lovelace")
    }

    @Test
    fun initialsWhenThereIsNoImage() {
        setAvatar { TSAvatar("Ada Lovelace", modifier = Modifier.testTag("avatar")) }
        assertContent(TSAvatarContent.Initials)
    }

    @Test
    fun aUsableImageReplacesTheInitials() {
        setAvatar { TSAvatar("Ada Lovelace", modifier = Modifier.testTag("avatar"), image = photo) }
        assertContent(TSAvatarContent.Image)
    }

    @Test
    fun aFailedImageFallsBackToInitials() {
        setAvatar { TSAvatar("Ada Lovelace", modifier = Modifier.testTag("avatar"), image = emptyPainter) }
        assertContent(TSAvatarContent.Initials)
    }

    @Test
    fun blankNameWithoutImageShowsThePlaceholder() {
        setAvatar { TSAvatar("  ", modifier = Modifier.testTag("avatar")) }
        assertContent(TSAvatarContent.Placeholder)
        composeRule.onNodeWithTag("avatar")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.HideFromAccessibility))
    }

    @Test
    fun circleIsFixedChromeAt200PercentText() {
        setAvatar(fontScale = 2f) {
            TSAvatarSize.entries.forEach { size ->
                TSAvatar("Ada Lovelace", modifier = Modifier.testTag(size.name), size = size)
            }
        }
        listOf(TSAvatarSize.Xs to 24.dp, TSAvatarSize.Sm to 32.dp, TSAvatarSize.Md to 40.dp, TSAvatarSize.Lg to 56.dp).forEach { (size, dp) ->
            composeRule.onNodeWithTag(size.name).assertWidthIsEqualTo(dp).assertHeightIsEqualTo(dp)
        }
    }

    @Test
    fun painterUsabilityRule() {
        assertTrue(photo.isUsableAvatarImage())
        assertTrue(ColorPainter(Color.Red).isUsableAvatarImage())
        assertFalse(emptyPainter.isUsableAvatarImage())
    }
}
