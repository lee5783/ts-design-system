import SwiftUI

public struct TSIconButton: View {
    let icon: TSIconSource
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
        self.init(TSIconSource.ts(icon), label: label, variant: variant, isRound: isRound, action: action)
    }

    public init(
        _ icon: TSIconSource,
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
            TSIconSourceView(source: icon, pointSize: iconSize)
        }
        .buttonStyle(TSIconButtonStyle(variant: variant, isRound: isRound, isFocused: isFocused, stateOverride: stateOverride))
        .focused($isFocused)
        .tsFocusEffectDisabled()
        .accessibilityLabel(Text(label))
        .accessibilityShowsLargeContentViewer {
            TSIconSourceView(source: icon, pointSize: iconSize)
            Text(label)
        }
    }

    private var iconSize: CGFloat {
        TSIconButtonStyleSpec.resolve(.init(variant: variant, isRound: isRound)).iconSize
    }

    func forcingState(_ state: TSControlState) -> TSIconButton {
        var copy = self
        copy.stateOverride = state
        return copy
    }
}
