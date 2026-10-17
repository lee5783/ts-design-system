import TSDesignSystemUIKit
import UIKit

final class ButtonGalleryUIView: GalleryStackView {
    init(variant: TSButtonVariant) {
        super.init(frame: .zero)
        for size in TSButtonSize.allCases {
            stack.addArrangedSubview(TSButtonView(title: "Default", variant: variant, size: size))
            let pressed = TSButtonView(title: "Pressed", variant: variant, size: size)
            pressed.isHighlighted = true
            stack.addArrangedSubview(pressed)
            let disabled = TSButtonView(title: "Disabled", variant: variant, size: size)
            disabled.isEnabled = false
            stack.addArrangedSubview(disabled)
            let loading = TSButtonView(title: "Loading", variant: variant, size: size)
            loading.isLoading = true
            stack.addArrangedSubview(loading)
        }
        stack.addArrangedSubview(TSButtonView(title: "Leading icon", variant: variant, leadingIcon: .plus))
        stack.addArrangedSubview(TSButtonView(title: "Trailing icon", variant: variant, trailingIcon: .arrowRight))
        stack.addArrangedSubview(TSButtonView(title: "Both icons", variant: variant, leadingIcon: .download, trailingIcon: .chevronRight))
        let block = TSButtonView(title: "Block", variant: variant, size: .lg)
        block.isBlock = true
        stack.addArrangedSubview(block)
        block.widthAnchor.constraint(equalTo: stack.layoutMarginsGuide.widthAnchor).isActive = true
    }
}
