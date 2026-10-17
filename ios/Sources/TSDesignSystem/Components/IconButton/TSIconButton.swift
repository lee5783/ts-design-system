import SwiftUI

public struct TSIconButton: View {
    let icon: TSIconName
    let label: String
    let variant: TSIconButtonVariant
    let isRound: Bool
    let action: () -> Void
    var stateOverride: TSControlState?

    @FocusState private var isFocused: Bool

    public init(
        _ icon: TSIconName,
        label: String,
        variant: TSIconButtonVariant = .ghost,
        isRound: Bool = false,
        action: @escaping () -> Void
    ) {
        self.icon = icon
        self.label = label
        self.variant = variant
        self.isRound = isRound
        self.action = action
    }

    public var body: some View {
        Button(action: action) {
            TSIcon(icon, size: .lg)
        }
        .buttonStyle(TSIconButtonStyle(variant: variant, isRound: isRound, isFocused: isFocused, stateOverride: stateOverride))
        .focused($isFocused)
        .tsFocusEffectDisabled()
        .accessibilityLabel(Text(label))
        .accessibilityShowsLargeContentViewer {
            TSIcon(icon, size: .lg)
            Text(label)
        }
    }

    func forcingState(_ state: TSControlState) -> TSIconButton {
        var copy = self
        copy.stateOverride = state
        return copy
    }
}
