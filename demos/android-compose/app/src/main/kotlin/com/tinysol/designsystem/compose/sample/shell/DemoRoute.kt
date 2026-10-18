package com.tinysol.designsystem.compose.sample.shell

import com.tinysol.designsystem.sample.support.catalog.DemoPageId

sealed interface DemoRoute {
    val key: String

    data object Catalog : DemoRoute {
        override val key: String = "catalog"
    }

    data object About : DemoRoute {
        override val key: String = "about"
    }

    data object BottomNavShell : DemoRoute {
        override val key: String = "bottom-nav-shell"
    }

    data class Page(val page: DemoPageId) : DemoRoute {
        override val key: String = "$PAGE_PREFIX${page.id}"
    }

    companion object {
        private const val PAGE_PREFIX = "page:"

        fun fromKey(key: String): DemoRoute = when {
            key == About.key -> About
            key == BottomNavShell.key -> BottomNavShell
            key.startsWith(PAGE_PREFIX) -> DemoPageId.fromId(key.removePrefix(PAGE_PREFIX))?.let(::Page) ?: Catalog
            else -> Catalog
        }
    }
}
