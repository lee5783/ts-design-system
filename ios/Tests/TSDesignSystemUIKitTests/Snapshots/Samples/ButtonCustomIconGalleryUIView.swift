import TSDesignSystemUIKit
import TSTestSupport
import UIKit

final class ButtonCustomIconGalleryUIView: GalleryStackView {
    init(variant: TSButtonVariant) {
        super.init(frame: .zero)
        let controller = TSSampleIconImage.controller()
        for size in TSButtonSize.allCases {
            let custom = TSButtonView(title: size.rawValue, variant: variant, size: size)
            custom.leadingIconImage = controller
            addRow([TSButtonView(title: size.rawValue, variant: variant, size: size, leadingIcon: .plus), custom], spacing: TSSpacing.space2)
        }
        let pressed = customButton("Pressed", variant: variant, image: controller)
        pressed.isHighlighted = true
        stack.addArrangedSubview(pressed)
        let disabled = customButton("Disabled", variant: variant, image: controller)
        disabled.isEnabled = false
        stack.addArrangedSubview(disabled)
        let loading = customButton("Loading", variant: variant, image: controller)
        loading.isLoading = true
        stack.addArrangedSubview(loading)
        let large = TSButtonView(title: "Large image", variant: variant)
        large.leadingIconImage = TSSampleIconImage.controller(side: 48)
        stack.addArrangedSubview(large)
        let limited = TSButtonView(title: "Join the session with a very long label", variant: variant)
        limited.leadingIconImage = controller
        limited.numberOfLines = 1
        limited.lineBreakMode = .byTruncatingTail
        stack.addArrangedSubview(limited)
        limited.widthAnchor.constraint(equalToConstant: 220).isActive = true
        let wrapping = TSButtonView(title: "Join the session with a very long label", variant: variant)
        wrapping.leadingIconImage = controller
        stack.addArrangedSubview(wrapping)
        wrapping.widthAnchor.constraint(equalToConstant: 220).isActive = true
    }

    private func customButton(_ title: String, variant: TSButtonVariant, image: UIImage) -> TSButtonView {
        let button = TSButtonView(title: title, variant: variant)
        button.leadingIconImage = image
        button.trailingIconImage = image
        return button
    }
}

final class IconButtonCustomIconGalleryUIView: GalleryStackView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        stack.spacing = TSSpacing.space4
        let controller = TSSampleIconImage.controller()
        for variant in TSIconButtonVariant.allCases {
            let pressed = TSIconButtonView(iconImage: controller, label: "Controller", variant: variant)
            pressed.isHighlighted = true
            let disabled = TSIconButtonView(iconImage: controller, label: "Controller", variant: variant)
            disabled.isEnabled = false
            addRow([
                TSIconButtonView(icon: .plus, label: "Add", variant: variant),
                TSIconButtonView(iconImage: controller, label: "Controller", variant: variant),
                pressed,
                disabled,
                TSIconButtonView(iconImage: controller, label: "Controller", variant: variant, isRound: true)
            ])
        }
    }
}
