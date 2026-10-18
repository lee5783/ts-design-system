package com.tinysol.designsystem.compose

import android.os.Build
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class ComposeRobolectricHarnessTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersComposeContentOnMinAndTargetSdk() {
        composeRule.setContent {
            BasicText("TS")
        }

        composeRule.onNodeWithText("TS").assertExists()
        assertTrue(Build.VERSION.SDK_INT == 26 || Build.VERSION.SDK_INT == 36)
    }
}
