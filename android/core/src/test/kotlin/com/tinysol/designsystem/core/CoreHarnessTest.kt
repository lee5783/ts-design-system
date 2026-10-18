package com.tinysol.designsystem.core

import org.junit.Assert.assertTrue
import org.junit.Test

class CoreHarnessTest {
    @Test
    fun hostTestsRunOnJava21OrNewer() {
        assertTrue((System.getProperty("java.specification.version")?.toIntOrNull() ?: 0) >= 21)
    }
}
