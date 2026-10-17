import SwiftUI

struct TSCardStyle: ButtonStyle {
    let isRaised: Bool
    let isFocused: Bool
    let stateOverride: TSControlState?

    func makeBody(configuration: Configuration) -> some View {
        TSCardStyleBody(label: configuration.label, isPressed: configuration.isPressed, style: self)
    }
}

struct TSCardStyleBody<Label: View>: View {
    let label: Label
    let isPressed: Bool
    let style: TSCardStyle

    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var state: TSControlState {
        style.stateOverride ?? .make(isPressed: isPressed, isEnabled: isEnabled, isFocused: style.isFocused)
    }

    var body: some View {
        let resolved = TSCardStyleSpec.resolve(.init(isRaised: style.isRaised, isInteractive: true, state: state))
        label
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(resolved.fill.color)
            .contentShape(Rectangle())
            .animation(TSPressFeedback.animation(reduceMotion: reduceMotion), value: state)
    }
}

struct TSCardChrome: ViewModifier {
    let state: TSControlState
    let isRaised: Bool
    let isInteractive: Bool
    var drawsFill = true

    func body(content: Content) -> some View {
        let resolved = TSCardStyleSpec.resolve(.init(isRaised: isRaised, isInteractive: isInteractive, state: state))
        let shape = RoundedRectangle(cornerRadius: resolved.cornerRadius, style: .continuous)
        content
            .environment(\.tsDimmedByContainer, state.contains(.disabled))
            .background(drawsFill ? resolved.fill.color : Color.clear)
            .clipShape(shape)
            .overlay(shape.strokeBorder(resolved.border.color, lineWidth: resolved.borderWidth).allowsHitTesting(false))
            .background(shadowCaster(shape, resolved: resolved))
            .tsFocusRing(isFocused: resolved.showsFocusRing, cornerRadius: resolved.cornerRadius)
            .opacity(resolved.opacity)
    }

    @ViewBuilder
    private func shadowCaster(_ shape: RoundedRectangle, resolved: TSCardStyleSpec.Resolved) -> some View {
        if let shadow = resolved.shadow {
            shape
                .fill(TSColor.surfaceCard.color)
                .tsShadow(shadow)
        }
    }
}

struct TSCardContent<Accessory: View, Content: View>: View {
    let title: String?
    let subtitle: String?
    let footerActions: [TSCardAction]
    let isInteractive: Bool
    let accessory: Accessory
    let content: Content

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private var hasAccessory: Bool {
        Accessory.self != EmptyView.self
    }

    private var hasBody: Bool {
        Content.self != EmptyView.self
    }

    private var hasHeader: Bool {
        title != nil || subtitle != nil || hasAccessory
    }

    var body: some View {
        let resolved = TSCardStyleSpec.resolve(.init(isInteractive: isInteractive))
        VStack(alignment: .leading, spacing: 0) {
            if hasHeader {
                header(resolved)
            }
            if hasBody {
                content
                    .foregroundColor(resolved.bodyColor.color)
                    .multilineTextAlignment(.leading)
                    .fixedSize(horizontal: false, vertical: true)
                    .tsFont(resolved.bodyFont)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(resolved.padding)
                    .allowsHitTesting(!isInteractive)
            }
            if !footerActions.isEmpty {
                footer(resolved)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func header(_ resolved: TSCardStyleSpec.Resolved) -> some View {
        HStack(alignment: .top, spacing: resolved.headerSpacing) {
            VStack(alignment: .leading, spacing: resolved.subtitleSpacing) {
                if let title {
                    Text(title)
                        .foregroundColor(resolved.titleColor.color)
                        .fixedSize(horizontal: false, vertical: true)
                        .tsFont(resolved.titleFont)
                        .accessibilityAddTraits(.isHeader)
                }
                if let subtitle {
                    Text(subtitle)
                        .foregroundColor(resolved.subtitleColor.color)
                        .fixedSize(horizontal: false, vertical: true)
                        .tsFont(resolved.subtitleFont)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .allowsHitTesting(!isInteractive)
            if hasAccessory {
                accessory
            }
        }
        .padding(.horizontal, resolved.padding)
        .padding(.top, resolved.padding)
        .padding(.bottom, hasBody ? 0 : resolved.padding)
    }

    private func footer(_ resolved: TSCardStyleSpec.Resolved) -> some View {
        VStack(spacing: 0) {
            Rectangle()
                .fill(resolved.divider.color)
                .frame(height: resolved.dividerThickness)
                .accessibilityHidden(true)
            footerButtons(resolved)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.vertical, resolved.footerVerticalPadding)
                .padding(.horizontal, resolved.footerHorizontalPadding)
        }
    }

    @ViewBuilder
    private func footerButtons(_ resolved: TSCardStyleSpec.Resolved) -> some View {
        if dynamicTypeSize.isAccessibilitySize {
            VStack(alignment: .trailing, spacing: resolved.footerSpacing) {
                buttons
            }
        } else {
            HStack(spacing: resolved.footerSpacing) {
                buttons
            }
        }
    }

    private var buttons: some View {
        ForEach(footerActions.indices, id: \.self) { index in
            let action = footerActions[index]
            TSButton(action.label, variant: action.variant) {
                action.handler()
            }
        }
    }
}
