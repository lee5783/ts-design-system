import TSDesignSystemUIKit
import UIKit

final class IconButtonGalleryUIView: GalleryStackView {
    init() {
        super.init(frame: .zero)
        stack.spacing = TSSpacing.space4
        for variant in TSIconButtonVariant.allCases {
            for isRound in [false, true] {
                let pressed = TSIconButtonView(icon: .plus, label: "Add", variant: variant, isRound: isRound)
                pressed.isHighlighted = true
                let disabled = TSIconButtonView(icon: .trash, label: "Delete", variant: variant, isRound: isRound)
                disabled.isEnabled = false
                addRow([
                    TSIconButtonView(icon: .search, label: "Search", variant: variant, isRound: isRound),
                    pressed,
                    disabled,
                    TSIconButtonView(icon: .chevronRight, label: "Next", variant: variant, isRound: isRound)
                ])
            }
        }
    }
}
