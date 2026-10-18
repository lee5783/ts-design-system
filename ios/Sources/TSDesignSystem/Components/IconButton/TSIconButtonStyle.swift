import SwiftUI

struct TSIconButtonStyle: ButtonStyle {
    let variant: TSIconButtonVariant
    let isRound: Bool
    let isFocused: Bool
    let stateOverride: TSControlState?

    func makeBody(configuration: Configuration) -> some View {
        TSIconButtonStyleBody(label: configuration.label, isPressed: configuration.isPressed, style: self)
    }
}

struct TSIconButtonStyleBody<Label: View>: View {
    let label: Label
    let isPressed: Bool
    let style: TSIconButtonStyle

    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.tsDimmedByContainer) private var isDimmedByContainer

    var body: some View {
        let state = style.stateOverride ?? .make(isPressed: isPressed, isEnabled: isEnabled, isFocused: style.isFocused)
        let resolved = TSIconButtonStyleSpec.resolve(.init(variant: style.variant, isRound: style.isRound, state: state))
        label
            .foregroundColor(resolved.icon.color)
            .frame(width: resolved.size, height: resolved.size)
            .tsControlChrome(
                RoundedRectangle(cornerRadius: resolved.cornerRadius, style: style.isRound ? .circular : .continuous),
                fill: resolved.fill,
                border: resolved.border,
                borderWidth: resolved.borderWidth
            )
            .contentShape(Rectangle())
            .tsFocusRing(isFocused: resolved.showsFocusRing, cornerRadius: resolved.cornerRadius, style: style.isRound ? .circular : .continuous)
            .opacity(isDimmedByContainer ? 1 : resolved.opacity)
            .animation(TSAnimation.pressFeedback, value: state)
    }
}
