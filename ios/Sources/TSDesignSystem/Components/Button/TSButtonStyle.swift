import SwiftUI

struct TSButtonStyle: ButtonStyle {
    let variant: TSButtonVariant
    let size: TSButtonSize
    let isLoading: Bool
    let isBlock: Bool
    let isFocused: Bool
    let stateOverride: TSControlState?

    func makeBody(configuration: Configuration) -> some View {
        TSButtonStyleBody(label: configuration.label, isPressed: configuration.isPressed, style: self)
    }
}

struct TSButtonStyleBody<Label: View>: View {
    let label: Label
    let isPressed: Bool
    let style: TSButtonStyle

    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.tsDimmedByContainer) private var isDimmedByContainer
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        let state = style.stateOverride ?? .make(isPressed: isPressed, isEnabled: isEnabled, isLoading: style.isLoading, isFocused: style.isFocused)
        let resolved = TSButtonStyleSpec.resolve(.init(variant: style.variant, size: style.size, state: state))
        let lineHeight = resolved.font.lineHeight(for: UIContentSizeCategory(dynamicTypeSize))
        label
            .foregroundColor(resolved.label.color)
            .padding(.horizontal, resolved.horizontalPadding)
            .padding(.vertical, resolved.verticalPadding)
            .frame(maxWidth: style.isBlock ? .infinity : nil, minHeight: resolved.minHeight)
            .tsControlChrome(
                RoundedRectangle(cornerRadius: resolved.cornerRadius, style: .continuous),
                fill: resolved.fill,
                border: resolved.border,
                borderWidth: resolved.borderWidth,
                shadow: resolved.shadow
            )
            .tsFocusRing(isFocused: resolved.showsFocusRing, cornerRadius: resolved.cornerRadius)
            .opacity(isDimmedByContainer ? 1 : resolved.opacity)
            .tsMinimumHitArea(outset: TSHitArea.outset(for: max(resolved.minHeight, lineHeight), minimum: resolved.minimumHitSize))
            .animation(TSAnimation.pressFeedback, value: state)
    }
}
