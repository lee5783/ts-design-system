package com.tinysol.designsystem.compose

import org.junit.Assert.assertTrue
import org.junit.Test

class ComposeHarnessTest {
    @Test
    fun hostTestsRunOnJava21OrNewer() {
        assertTrue((System.getProperty("java.specification.version")?.toIntOrNull() ?: 0) >= 21)
    }
}
