import SwiftUI
@testable import TSDesignSystem

enum TabsSample {
    static let items = ["Day", "Week", "Month"].map { TSTabItem(id: $0.lowercased(), label: $0) }
    static let fourItems = ["Day", "Week", "Month", "Year"].map { TSTabItem(id: $0.lowercased(), label: $0) }
    static let twoItems = ["Grid", "List"].map { TSTabItem(id: $0.lowercased(), label: $0) }
    static let manyItems = ["Overview", "Activity", "Members", "Billing", "Settings", "Integrations", "Security", "Notifications"]
        .map { TSTabItem(id: $0.lowercased(), label: $0) }
}

struct TabsGalleryView: View {
    let variant: TSTabsVariant

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            tabs(TabsSample.items)
            tabs(TabsSample.items).forcingState(.pressed, for: "week")
            tabs(TabsSample.items).forcingState(.focused, for: "week")
            tabs(TabsSample.items, selection: "week").disabled(true)
            switch variant {
            case .underline:
                tabs(TabsSample.manyItems)
            case .segmented:
                tabs(TabsSample.twoItems, isBlock: false)
                tabs(TabsSample.fourItems, selection: "year")
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func tabs(_ items: [TSTabItem], selection: String? = nil, isBlock: Bool = true) -> TSTabs {
        TSTabs(items: items, selection: .constant(selection ?? items[0].id), variant: variant, isBlock: isBlock)
    }
}
