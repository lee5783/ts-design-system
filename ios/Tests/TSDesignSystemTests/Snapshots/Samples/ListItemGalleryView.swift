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
