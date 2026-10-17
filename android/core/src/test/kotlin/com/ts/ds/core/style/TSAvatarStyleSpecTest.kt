package com.ts.ds.core.style

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class TSAvatarStyleSpecTest(
    private val configuration: TSAvatarStyleSpec.Configuration,
) {
    private val resolved = TSAvatarStyleSpec.resolve(configuration)

    @Test
    fun geometryFollowsTheSizeTable() {
        val (diameter, fontSize, iconSize) = when (configuration.size) {
            TSAvatarSize.Xs -> Triple(24f, 10f, 16f)
            TSAvatarSize.Sm -> Triple(32f, 12f, 16f)
            TSAvatarSize.Md -> Triple(40f, 14f, 20f)
            TSAvatarSize.Lg -> Triple(56f, 20f, 24f)
        }
        assertEquals(diameter, resolved.diameterDp, 0f)
        assertEquals(diameter / 2f, resolved.cornerRadiusDp, 0f)
        assertEquals(fontSize, resolved.initialsFontSizeDp, 0f)
        assertEquals(iconSize, resolved.placeholderIconSizeDp, 0f)
    }

    @Test
    fun colorsAndInitialsType() {
        assertEquals(TSColorRole.AccentSubtle, resolved.fill)
        assertEquals(TSColorRole.AccentText, resolved.foreground)
        assertEquals(600, resolved.initialsFontWeight)
        assertEquals(0.5f, resolved.initialsMinimumScaleFactor, 0f)
        assertEquals(2f, resolved.contentInsetDp, 0f)
    }

    @Test
    fun exactlyOneContentIsVisible() {
        val visible = listOf(resolved.showsInitials, resolved.showsImage, resolved.showsPlaceholder)
        assertEquals(1, visible.count { it })
        assertEquals(configuration.content == TSAvatarContent.Initials, resolved.showsInitials)
        assertEquals(configuration.content == TSAvatarContent.Image, resolved.showsImage)
        assertEquals(configuration.content == TSAvatarContent.Placeholder, resolved.showsPlaceholder)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters(): List<Array<Any>> = TSAvatarStyleSpec.configurations.map { arrayOf<Any>(it) }
    }
}

class TSAvatarContentTest {
    @Test
    fun matrixHas12Configurations() {
        assertEquals(12, TSAvatarStyleSpec.configurations.toSet().size)
    }

    @Test
    fun aUsableImageWins() {
        assertEquals(TSAvatarContent.Image, TSAvatarContent.make(initials = "AL", hasImage = true))
        assertEquals(TSAvatarContent.Image, TSAvatarContent.make(initials = "", hasImage = true))
    }

    @Test
    fun initialsWhenThereIsNoImage() {
        assertEquals(TSAvatarContent.Initials, TSAvatarContent.make(initials = "AL", hasImage = false))
    }

    @Test
    fun placeholderWhenThereAreNoInitials() {
        assertEquals(TSAvatarContent.Placeholder, TSAvatarContent.make(initials = "", hasImage = false))
    }
}
