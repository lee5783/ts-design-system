import TSDesignSystemUIKit
import UIKit

final class IconGalleryUIView: GalleryStackView {
    static let names: [TSIconName] = [.home, .bell, .chevronRight, .arrowLeft, .logout, .check]

    init() {
        super.init(frame: .zero)
        stack.spacing = TSSpacing.space4
        for size in TSIconSize.allCases {
            addRow(Self.names.map { name in
                let icon = TSIconView(name, size: size)
                icon.tintColor = TSColor.textPrimary.uiColor
                return icon
            }, spacing: TSSpacing.space3)
        }
        let status: [(TSIconName, String, TSColor)] = [
            (.checkCircle, "Success", .success),
            (.alert, "Warning", .warning),
            (.xCircle, "Error", .danger),
            (.info, "Information", .accentText)
        ]
        addRow(status.map { name, label, color in
            let icon = TSIconView(name, size: .lg, label: label)
            icon.tintColor = color.uiColor
            return icon
        }, spacing: TSSpacing.space3)
    }
}
