import TSDesignSystemUIKit
import UIKit

@MainActor
enum DemoIcon {
    static func barButtonItem(_ name: TSIconName, label: String, identifier: String, action: UIAction) -> UIBarButtonItem {
        let item = UIBarButtonItem(title: nil, image: name.image(), primaryAction: action, menu: nil)
        item.accessibilityLabel = label
        item.accessibilityIdentifier = identifier
        return item
    }
}
