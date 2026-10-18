package com.tinysol.designsystem.views

import org.junit.Assert.assertTrue
import org.junit.Test

class ViewsHarnessTest {
    @Test
    fun hostTestsRunOnJava21OrNewer() {
        assertTrue((System.getProperty("java.specification.version")?.toIntOrNull() ?: 0) >= 21)
    }
}
