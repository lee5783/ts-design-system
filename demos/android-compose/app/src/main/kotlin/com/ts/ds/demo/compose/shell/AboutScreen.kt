package com.ts.ds.demo.compose.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ts.ds.compose.theme.TSShapes
import com.ts.ds.compose.theme.TSSpacing
import com.ts.ds.demo.compose.BuildConfig
import com.ts.ds.demo.compose.R
import com.ts.ds.demo.support.R as DemoR

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        DemoR.string.demo_about_toolkit to stringResource(R.string.toolkit_name),
        DemoR.string.demo_about_app_version to BuildConfig.VERSION_NAME,
        DemoR.string.demo_about_library_version to BuildConfig.DS_LIBRARY_VERSION,
        DemoR.string.demo_about_design_version to BuildConfig.DS_DESIGN_VERSION,
    )
    DemoScreen(
        title = stringResource(DemoR.string.demo_about_title),
        onBack = onBack,
        modifier = modifier.testTag(DemoTags.ABOUT),
    ) {
        item(key = "logo") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TSSpacing.space6),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .background(colorResource(R.color.ic_launcher_background), TSShapes.radiusXl),
                )
            }
        }
        rows.forEach { (label, value) ->
            item(key = label) {
                DemoTokenRow(name = stringResource(label), value = value) {}
            }
        }
    }
}
