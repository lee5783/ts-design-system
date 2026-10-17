import SwiftUI

struct TSSwitchStyle: ButtonStyle {
    let isOn: Bool
    let isLabelHidden: Bool
    let isFocused: Bool
    let stateOverride: TSControlState?

    func makeBody(configuration: Configuration) -> some View {
        TSSwitchStyleBody(label: configuration.label, isPressed: configuration.isPressed, style: self)
    }
}

struct TSSwitchStyleBody<Label: View>: View {
    let label: Label
    let isPressed: Bool
    let style: TSSwitchStyle

    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let state = style.stateOverride ?? .make(isPressed: isPressed, isEnabled: isEnabled, isFocused: style.isFocused)
        let resolved = TSSwitchStyleSpec.resolve(.init(isOn: style.isOn, state: state))
        HStack(spacing: resolved.spacing) {
            TSSwitchTrack(resolved: resolved)
                .animation(TSReduceMotion.animation(duration: resolved.animationDuration, reduceMotion: reduceMotion), value: style.isOn)
            if !style.isLabelHidden {
                label
                    .foregroundColor(resolved.labelColor.color)
                    .fixedSize(horizontal: false, vertical: true)
                    .tsFont(resolved.labelFont)
            }
        }
        .frame(minHeight: resolved.minHeight)
        .contentShape(Rectangle())
        .opacity(resolved.opacity)
        .animation(TSPressFeedback.animation(reduceMotion: reduceMotion), value: state)
    }
}

struct TSSwitchTrack: View {
    let resolved: TSSwitchStyleSpec.Resolved

    var body: some View {
        ZStack(alignment: .leading) {
            Capsule(style: .continuous)
                .fill(resolved.trackColor.color)
                .opacity(resolved.trackOpacity)
            Circle()
                .fill(resolved.thumbColor.color)
                .frame(width: resolved.thumbSize, height: resolved.thumbSize)
                .tsShadow(resolved.thumbShadow)
                .padding(.leading, resolved.thumbLeadingOffset)
        }
        .frame(width: resolved.trackWidth, height: resolved.trackHeight, alignment: .leading)
        .tsFocusRing(isFocused: resolved.showsFocusRing, cornerRadius: resolved.trackCornerRadius, style: .circular)
        .accessibilityHidden(true)
    }
}
