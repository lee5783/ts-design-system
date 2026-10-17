package com.ts.ds.testsupport

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class TestSupportRobolectricHarnessTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersComposeContentOnMinAndTargetSdk() {
        composeRule.setContent {
            Layout(content = {}, modifier = Modifier.testTag("ts")) { _, _ ->
                layout(1, 1) {}
            }
        }

        composeRule.onNodeWithTag("ts").assertExists()
        assertTrue(Build.VERSION.SDK_INT == 26 || Build.VERSION.SDK_INT == 36)
    }
}
