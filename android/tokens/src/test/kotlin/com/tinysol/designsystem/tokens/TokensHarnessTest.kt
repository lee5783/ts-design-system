package com.tinysol.designsystem.tokens

import org.junit.Assert.assertTrue
import org.junit.Test

class TokensHarnessTest {
    @Test
    fun hostTestsRunOnJava21OrNewer() {
        assertTrue((System.getProperty("java.specification.version")?.toIntOrNull() ?: 0) >= 21)
    }
}
