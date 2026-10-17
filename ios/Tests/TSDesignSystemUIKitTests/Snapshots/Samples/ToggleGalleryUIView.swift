import TSDesignSystemUIKit
import UIKit

enum ToggleGalleryUIKind: String, CaseIterable {
    case checkbox
    case `switch`
}

final class ToggleGalleryUIView: GalleryStackView {
    private static let longLabel = "Email me about product updates, tips and occasional offers"

    init(kind: ToggleGalleryUIKind) {
        super.init(frame: .zero)
        stack.spacing = TSSpacing.space2
        stack.alignment = .fill
        for isOn in [false, true] {
            let suffix = isOn ? "on" : "off"
            stack.addArrangedSubview(Self.control(kind, label: "Default \(suffix)", isOn: isOn))
            let pressed = Self.control(kind, label: "Pressed \(suffix)", isOn: isOn)
            pressed.isHighlighted = true
            stack.addArrangedSubview(pressed)
            let disabled = Self.control(kind, label: "Disabled \(suffix)", isOn: isOn)
            disabled.isEnabled = false
            stack.addArrangedSubview(disabled)
        }
        stack.addArrangedSubview(Self.control(kind, label: Self.longLabel, isOn: true))
        let hidden = UIStackView(arrangedSubviews: [
            Self.control(kind, label: "Hidden off", isOn: false, isLabelHidden: true),
            Self.control(kind, label: "Hidden on", isOn: true, isLabelHidden: true),
            UIView()
        ])
        hidden.axis = .horizontal
        hidden.alignment = .center
        hidden.spacing = TSSpacing.space4
        stack.addArrangedSubview(hidden)
    }

    private static func control(_ kind: ToggleGalleryUIKind, label: String, isOn: Bool, isLabelHidden: Bool = false) -> UIControl {
        switch kind {
        case .checkbox: TSCheckboxView(label: label, isOn: isOn, isLabelHidden: isLabelHidden)
        case .switch: TSSwitchView(label: label, isOn: isOn, isLabelHidden: isLabelHidden)
        }
    }
}
