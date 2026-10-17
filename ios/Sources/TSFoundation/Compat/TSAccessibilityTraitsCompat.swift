import SwiftUI
import UIKit

package enum TSToggleKind: String, CaseIterable, Hashable, Sendable {
    case `switch`
    case checkbox

    package func accessibilityValue(isOn: Bool) -> String {
        switch self {
        case .switch: isOn ? TSStrings.toggleOn : TSStrings.toggleOff
        case .checkbox: isOn ? TSStrings.checkboxChecked : TSStrings.checkboxUnchecked
        }
    }
}

package enum TSToggleTrait: String, Hashable, Sendable {
    case toggleButton
    case button
}

package struct TSToggleAccessibility: Hashable, Sendable {
    package let trait: TSToggleTrait
    package let value: String
}

package enum TSAccessibilityTraitsCompat {
    package static func toggle(isOn: Bool, kind: TSToggleKind, path: TSCompatPath = TSCompat.toggleTrait) -> TSToggleAccessibility {
        TSToggleAccessibility(
            trait: path == .native ? .toggleButton : .button,
            value: kind.accessibilityValue(isOn: isOn)
        )
    }

    package static func uiTraits(for trait: TSToggleTrait) -> UIAccessibilityTraits {
        switch trait {
        case .toggleButton:
            if #available(iOS 17, *) { .toggleButton } else { .button }
        case .button:
            .button
        }
    }

    @MainActor
    package static func apply(to view: UIView, isOn: Bool, kind: TSToggleKind, path: TSCompatPath = TSCompat.toggleTrait) {
        let resolved = toggle(isOn: isOn, kind: kind, path: path)
        let toggleTraits = uiTraits(for: .toggleButton).union([.button, .selected])
        view.isAccessibilityElement = true
        view.accessibilityTraits = view.accessibilityTraits.subtracting(toggleTraits).union(uiTraits(for: resolved.trait))
        view.accessibilityValue = resolved.value
    }
}

package extension View {
    func tsToggleAccessibility(isOn: Bool, kind: TSToggleKind, path: TSCompatPath = TSCompat.toggleTrait) -> some View {
        modifier(TSToggleAccessibilityModifier(isOn: isOn, kind: kind, path: path))
    }
}

struct TSToggleAccessibilityModifier: ViewModifier {
    let isOn: Bool
    let kind: TSToggleKind
    var path: TSCompatPath = TSCompat.toggleTrait

    func body(content: Content) -> some View {
        let value = kind.accessibilityValue(isOn: isOn)
        if path == .native, #available(iOS 17, *) {
            content
                .accessibilityAddTraits(.isToggle)
                .accessibilityRemoveTraits(.isSelected)
                .accessibilityValue(Text(value))
        } else {
            content
                .accessibilityAddTraits(.isButton)
                .accessibilityRemoveTraits(.isSelected)
                .accessibilityValue(Text(value))
        }
    }
}
