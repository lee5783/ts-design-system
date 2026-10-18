package com.tinysol.designsystem.sample.support.sample

import com.tinysol.designsystem.sample.support.R

public data class SamplePerson(
    val name: String,
    val email: String,
)

public data class SampleTab(
    val id: String,
    val labelRes: Int,
)

public data class SampleListRow(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int?,
    val iconName: String,
)

public data class SampleNavItem(
    val id: String,
    val labelRes: Int,
    val iconName: String,
)

public data class SampleMessage(
    val id: String,
    val titleRes: Int,
    val bodyRes: Int,
    val actionRes: Int?,
)

public object SampleData {
    public val pangramRes: Int = R.string.demo_sample_pangram

    public val people: List<SamplePerson> = listOf(
        SamplePerson("Ada Lovelace", "ada@example.com"),
        SamplePerson("Grace Brewster Hopper", "grace@example.com"),
        SamplePerson("Alan Turing", "alan@example.com"),
        SamplePerson("Hedy", "hedy@example.com"),
        SamplePerson("山田太郎", "taro@example.com"),
        SamplePerson("ليلى أحمد", "layla@example.com"),
    )

    public val badgeCounts: List<Int> = listOf(0, 1, 9, 99, 100)

    public val tabSections: List<SampleTab> = listOf(
        SampleTab("overview", R.string.demo_sample_overview),
        SampleTab("activity", R.string.demo_sample_activity),
        SampleTab("settings", R.string.demo_sample_settings),
    )

    public val tabRanges: List<SampleTab> = listOf(
        SampleTab("day", R.string.demo_sample_day),
        SampleTab("week", R.string.demo_sample_week),
        SampleTab("month", R.string.demo_sample_month),
        SampleTab("year", R.string.demo_sample_year),
    )

    public val tabLayouts: List<SampleTab> = listOf(
        SampleTab("grid", R.string.demo_sample_grid),
        SampleTab("list", R.string.demo_sample_list),
    )

    public val tabManySections: List<SampleTab> = listOf(
        SampleTab("overview", R.string.demo_sample_overview),
        SampleTab("activity", R.string.demo_sample_activity),
        SampleTab("members", R.string.demo_sample_members),
        SampleTab("billing", R.string.demo_sample_billing),
        SampleTab("settings", R.string.demo_sample_settings),
        SampleTab("integrations", R.string.demo_sample_integrations),
        SampleTab("security", R.string.demo_sample_security),
        SampleTab("notifications", R.string.demo_sample_notifications),
        SampleTab("reports", R.string.demo_sample_reports),
    )

    public val listRows: List<SampleListRow> = listOf(
        SampleListRow("inbox", R.string.demo_sample_inbox, R.string.demo_sample_inbox_subtitle, "mail"),
        SampleListRow("calendar", R.string.demo_sample_calendar, R.string.demo_sample_calendar_subtitle, "calendar"),
        SampleListRow("favorites", R.string.demo_sample_favorites, null, "star"),
        SampleListRow("downloads", R.string.demo_sample_downloads, R.string.demo_sample_downloads_subtitle, "download"),
        SampleListRow("settings", R.string.demo_sample_settings, null, "settings"),
    )

    public fun listRow(id: String): SampleListRow = listRows.firstOrNull { it.id == id } ?: listRows.first()

    public const val NAV_INBOX: String = "inbox"
    public const val NAV_ALERTS: String = "alerts"

    public val navCounts: List<Int> = listOf(3, 4, 5)

    public val navItems: List<SampleNavItem> = listOf(
        SampleNavItem("home", R.string.demo_sample_home, "home"),
        SampleNavItem(NAV_INBOX, R.string.demo_sample_inbox, "mail"),
        SampleNavItem("search", R.string.demo_sample_search, "search"),
        SampleNavItem(NAV_ALERTS, R.string.demo_sample_alerts, "bell"),
        SampleNavItem("profile", R.string.demo_sample_profile, "user"),
    )

    public fun navItems(count: Int): List<SampleNavItem> = when (count) {
        3 -> listOf(navItems[0], navItems[1], navItems[4])
        4 -> navItems.take(4)
        else -> navItems
    }

    public val messages: List<SampleMessage> = listOf(
        SampleMessage("saved", R.string.demo_message_saved_title, R.string.demo_message_saved_body, null),
        SampleMessage("deleted", R.string.demo_message_deleted_title, R.string.demo_message_deleted_body, R.string.demo_message_deleted_action),
        SampleMessage("offline", R.string.demo_message_offline_title, R.string.demo_message_offline_body, R.string.demo_message_offline_action),
        SampleMessage("confirm-delete", R.string.demo_message_confirm_delete_title, R.string.demo_message_confirm_delete_body, R.string.demo_message_confirm_delete_action),
    )

    public fun message(id: String): SampleMessage = messages.firstOrNull { it.id == id } ?: messages.first()
}
