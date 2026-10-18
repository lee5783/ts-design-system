import SwiftUI

struct TSCheckboxStyle: ButtonStyle {
    let isOn: Bool
    let isLabelHidden: Bool
    let isFocused: Bool
    let stateOverride: TSControlState?

    func makeBody(configuration: Configuration) -> some View {
        TSCheckboxStyleBody(label: configuration.label, isPressed: configuration.isPressed, style: self)
    }
}

struct TSCheckboxStyleBody<Label: View>: View {
    let label: Label
    let isPressed: Bool
    let style: TSCheckboxStyle

    @Environment(\.isEnabled) private var isEnabled

    var body: some View {
        let state = style.stateOverride ?? .make(isPressed: isPressed, isEnabled: isEnabled, isFocused: style.isFocused)
        let resolved = TSCheckboxStyleSpec.resolve(.init(isOn: style.isOn, state: state))
        HStack(spacing: resolved.spacing) {
            TSCheckboxMark(resolved: resolved)
            if !style.isLabelHidden {
                label
                    .foregroundColor(resolved.labelColor.color)
                    .fixedSize(horizontal: false, vertical: true)
                    .tsFont(resolved.labelFont)
            }
        }
        .frame(minHeight: resolved.minHeight)
        .contentShape(Rectangle())
        .tsMinimumHitArea(outset: style.isLabelHidden ? TSHitArea.outset(for: resolved.markSize) : 0)
        .opacity(resolved.opacity)
        .animation(TSAnimation.pressFeedback, value: state)
    }
}

struct TSCheckboxMark: View {
    let resolved: TSCheckboxStyleSpec.Resolved

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: resolved.markCornerRadius, style: .continuous)
        ZStack {
            if resolved.showsCheck {
                TSIcon(.check, size: resolved.checkSize)
                    .foregroundColor(resolved.checkColor.color)
            }
        }
        .frame(width: resolved.markSize, height: resolved.markSize)
        .tsControlChrome(shape, fill: resolved.markFill, border: resolved.markBorder, borderWidth: resolved.markBorderWidth)
        .tsFocusRing(isFocused: resolved.showsFocusRing, cornerRadius: resolved.markCornerRadius)
        .accessibilityHidden(true)
    }
}
