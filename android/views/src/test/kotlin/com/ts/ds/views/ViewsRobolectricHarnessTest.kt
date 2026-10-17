package com.ts.ds.views

import android.content.Context
import android.os.Build
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textview.MaterialTextView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class ViewsRobolectricHarnessTest {
    @Test
    fun inflatesMaterialViewOnMinAndTargetSdk() {
        val context = ContextThemeWrapper(
            ApplicationProvider.getApplicationContext<Context>(),
            com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar,
        )

        val textView = MaterialTextView(context).apply { text = "TS" }

        assertEquals("TS", textView.text.toString())
        assertTrue(Build.VERSION.SDK_INT == 26 || Build.VERSION.SDK_INT == 36)
    }
}
