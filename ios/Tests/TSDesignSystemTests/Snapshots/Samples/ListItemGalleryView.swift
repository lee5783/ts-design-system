import SwiftUI
@testable import TSDesignSystem

enum ListItemGalleryKind: String, CaseIterable {
    case contents
    case states
}

enum ListItemSample {
    static let longTitle = "Download large files over mobile data when Wi-Fi is unavailable"
    static let longSubtitle = "A very long subtitle that wraps onto a second line at larger text sizes"
}

struct ListItemGalleryView: View {
    let kind: ListItemGalleryKind

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space4) {
            switch kind {
            case .contents:
                contents
            case .states:
                states
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var contents: some View {
        TSListSection("Section") {
            TSListItem("Favorites")
            TSListItem("Inbox", subtitle: "12 unread messages", showsChevron: true, action: {}) {
                TSListItemIcon(.mail)
            } trailing: {
                TSBadge("New", tone: .accent)
            }
            TSListItem("Ada Lovelace", subtitle: "Engineer") {
                TSAvatar(name: "Ada Lovelace")
            } trailing: {
                Text("Online")
            }
            TSListItem("Wi-Fi", isOn: .constant(true)) {
                TSListItemIcon(.settings)
            }
            TSListItem(ListItemSample.longTitle, subtitle: ListItemSample.longSubtitle, showsChevron: true, action: {}) {
                TSListItemIcon(.download)
            } trailing: {
                Text("Today")
            }
        }
    }

    private var states: some View {
        TSList {
            TSListItem("Default", showsChevron: true, action: {})
            TSListItem("Pressed", showsChevron: true, action: {}).forcingState(.pressed)
            TSListItem("Focused", showsChevron: true, action: {}).forcingState(.focused)
            TSListItem("Disabled", showsChevron: true, action: {}).disabled(true)
            TSListItem("No action pressed", subtitle: "Not pressable").forcingState(.pressed)
            TSListItem("Switch off", isOn: .constant(false))
            TSListItem("Switch disabled", isOn: .constant(true)).disabled(true)
        }
    }
}

enum ListItemExtrasKind: String, CaseIterable {
    case disclosure
    case divider
}

struct ListItemExtrasGalleryView: View {
    let kind: ListItemExtrasKind

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space6) {
            switch kind {
            case .disclosure:
                disclosure
            case .divider:
                divider
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var disclosure: some View {
        Group {
            TSList {
                TSDisclosureListItem("Collapsed", isExpanded: .constant(false)) {
                    TSText("Hidden body", role: .bodySm, color: .secondary)
                }
                TSDisclosureListItem("Kotlin", subtitle: "Apache License 2.0", isExpanded: .constant(true)) {
                    TSListItemIcon(.info)
                } content: {
                    TSText(ListItemSample.longSubtitle, role: .bodySm, color: .secondary)
                }
                TSDisclosureListItem("Pressed", isExpanded: .constant(false)) {
                    TSText("Hidden body", role: .bodySm)
                }
                .forcingState(.pressed)
                TSDisclosureListItem("Disabled", isExpanded: .constant(true)) {
                    TSText("Dimmed body", role: .bodySm, color: .secondary)
                }
                .disabled(true)
            }
            TSDisclosureListItem("Standalone", isExpanded: .constant(true)) {
                TSText("Outside a list", role: .bodySm, color: .secondary)
            }
        }
    }

    private var divider: some View {
        Group {
            VStack(spacing: 0) {
                TSListItem("First row")
                TSDivider()
                TSListItem("Second row", subtitle: "Divided with TSDivider")
                TSDivider()
                TSListItem("Third row")
            }
            .background(TSColor.surfaceCard.color)
            TSList {
                TSListItem("First row")
                TSListItem("Second row", subtitle: "Divided inside TSList")
                TSListItem("Third row")
            }
        }
    }
}
