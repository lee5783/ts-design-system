import UIKit
@testable import TSDesignSystemUIKit

final class TabsGalleryUIView: GalleryStackView {
    private static let items = ["Day", "Week", "Month"].map { TSTabItem(id: $0.lowercased(), label: $0) }
    private static let fourItems = ["Day", "Week", "Month", "Year"].map { TSTabItem(id: $0.lowercased(), label: $0) }
    private static let twoItems = ["Grid", "List"].map { TSTabItem(id: $0.lowercased(), label: $0) }
    private static let manyItems = ["Overview", "Activity", "Members", "Billing", "Settings", "Integrations", "Security", "Notifications"]
        .map { TSTabItem(id: $0.lowercased(), label: $0) }

    init(variant: TSTabsVariant) {
        super.init(frame: .zero)
        stack.spacing = TSSpacing.space3
        stack.alignment = .fill
        stack.addArrangedSubview(Self.tabs(Self.items, variant: variant))
        let pressed = Self.tabs(Self.items, variant: variant)
        pressed.tabViews[1].isHighlighted = true
        stack.addArrangedSubview(pressed)
        let disabled = Self.tabs(Self.items, selectedID: "week", variant: variant)
        disabled.isEnabled = false
        stack.addArrangedSubview(disabled)
        switch variant {
        case .underline:
            stack.addArrangedSubview(Self.tabs(Self.manyItems, variant: variant))
        case .segmented:
            stack.addArrangedSubview(Self.tabs(Self.twoItems, variant: variant, isBlock: false))
            stack.addArrangedSubview(Self.tabs(Self.fourItems, selectedID: "year", variant: variant))
        }
    }

    private static func tabs(_ items: [TSTabItem], selectedID: String? = nil, variant: TSTabsVariant, isBlock: Bool = true) -> TSTabsView {
        TSTabsView(items: items, selectedID: selectedID, variant: variant, isBlock: isBlock)
    }
}
