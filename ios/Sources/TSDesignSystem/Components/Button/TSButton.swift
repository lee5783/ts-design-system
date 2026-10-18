import SwiftUI

public struct TSButton: View {
    let title: String
    let variant: TSButtonVariant
    let size: TSButtonSize
    let leadingIcon: TSIconSource?
    let trailingIcon: TSIconSource?
    let isLoading: Bool
    let isBlock: Bool
    let action: () -> Void
    var stateOverride: TSControlState?

    @FocusState private var isFocused: Bool
    @Environment(\.isEnabled) private var isEnabled

    public init(
        _ title: String,
        variant: TSButtonVariant = .primary,
        size: TSButtonSize = .md,
        leadingIcon: TSIconName? = nil,
        trailingIcon: TSIconName? = nil,
        isLoading: Bool = false,
        isBlock: Bool = false,
        action: @escaping () -> Void
    ) {
        self.init(
            title,
            variant: variant,
            size: size,
            leadingIconSource: leadingIcon.map(TSIconSource.ts),
            trailingIconSource: trailingIcon.map(TSIconSource.ts),
            isLoading: isLoading,
            isBlock: isBlock,
            action: action
        )
    }

    public init(
        _ title: String,
        variant: TSButtonVariant = .primary,
        size: TSButtonSize = .md,
        leadingIconSource: TSIconSource?,
        trailingIconSource: TSIconSource? = nil,
        isLoading: Bool = false,
        isBlock: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.variant = variant
        self.size = size
        self.leadingIcon = leadingIconSource
        self.trailingIcon = trailingIconSource
        self.isLoading = isLoading
        self.isBlock = isBlock
        self.action = action
    }

    public var body: some View {
        Button(action: perform) {
            label
        }
        .buttonStyle(
            TSButtonStyle(
                variant: variant,
                size: size,
                isLoading: isLoading,
                isBlock: isBlock,
                isFocused: isFocused,
                stateOverride: stateOverride
            )
        )
        .disabled(isLoading)
        .focused($isFocused)
        .tsFocusEffectDisabled()
    }

    func forcingState(_ state: TSControlState) -> TSButton {
        var copy = self
        copy.stateOverride = state
        return copy
    }

    private var resolved: TSButtonStyleSpec.Resolved {
        TSButtonStyleSpec.resolve(.init(variant: variant, size: size, state: .make(isEnabled: isEnabled, isLoading: isLoading)))
    }

    private var label: some View {
        let resolved = resolved
        return HStack(spacing: resolved.iconSpacing) {
            if resolved.showsSpinner {
                TSSpinner(size: resolved.spinnerSize, lineWidth: resolved.spinnerStrokeWidth, color: resolved.label.color)
                    .frame(width: resolved.iconSize, height: resolved.iconSize)
            } else if let leadingIcon {
                TSIconSourceView(source: leadingIcon, pointSize: resolved.iconSize)
            }
            Text(title)
                .underline(resolved.isUnderlined)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
                .tsFont(resolved.font)
            if let trailingIcon {
                TSIconSourceView(source: trailingIcon, pointSize: resolved.iconSize)
            }
        }
    }

    private func perform() {
        guard !isLoading else { return }
        action()
    }
}
