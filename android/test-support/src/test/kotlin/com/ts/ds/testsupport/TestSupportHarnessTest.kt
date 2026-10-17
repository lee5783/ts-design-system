package com.ts.ds.testsupport

import org.junit.Assert.assertTrue
import org.junit.Test

class TestSupportHarnessTest {
    @Test
    fun hostTestsRunOnJava21OrNewer() {
        assertTrue((System.getProperty("java.specification.version")?.toIntOrNull() ?: 0) >= 21)
    }
}
