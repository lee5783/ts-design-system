package com.ts.ds.core

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [26, 36])
class CoreRobolectricHarnessTest {
    @Test
    fun runsOnMinAndTargetSdk() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertTrue(Build.VERSION.SDK_INT == 26 || Build.VERSION.SDK_INT == 36)
        assertEquals(26, context.applicationInfo.minSdkVersion)
    }
}
