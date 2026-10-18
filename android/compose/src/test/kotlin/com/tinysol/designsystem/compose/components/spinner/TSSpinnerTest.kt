package com.tinysol.designsystem.compose.components.spinner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tinysol.designsystem.compose.components.shared.LocalTSForcedControlState
import com.tinysol.designsystem.compose.theme.TSTheme
import com.tinysol.designsystem.core.style.TSControlState
import com.tinysol.designsystem.core.style.TSSpinnerSize
import com.tinysol.designsystem.core.style.TSSpinnerStyleSpec
import com.tinysol.designsystem.core.style.resolve
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class TSSpinnerTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            CompositionLocalProvider(LocalTSForcedControlState provides TSControlState.Default) {
                TSTheme(darkTheme = false) { content() }
            }
        }
    }

    @Test
    fun sizesFollowTheIconTokens() {
        setContent {
            TSSpinnerSize.entries.forEach { TSSpinner(Modifier.testTag(it.name), size = it) }
        }
        TSSpinnerSize.entries.forEach { size ->
            val dp = TSSpinnerStyleSpec.resolve(TSSpinnerStyleSpec.Configuration(size)).sizeDp.dp
            composeRule.onNodeWithTag(size.name).assertWidthIsEqualTo(dp).assertHeightIsEqualTo(dp)
        }
        composeRule.onNodeWithTag(TSSpinnerSize.Md.name).assertWidthIsEqualTo(20.dp)
    }
}
