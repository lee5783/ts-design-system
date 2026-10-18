import SwiftUI

struct TSTabStyle: ButtonStyle {
    let variant: TSTabsVariant
    let isSelected: Bool
    let isBlock: Bool
    let isFocused: Bool
    let stateOverride: TSControlState?
    let namespace: Namespace.ID

    func makeBody(configuration: Configuration) -> some View {
        TSTabStyleBody(label: configuration.label, isPressed: configuration.isPressed, style: self)
    }
}

struct TSTabStyleBody<Label: View>: View {
    let label: Label
    let isPressed: Bool
    let style: TSTabStyle

    @Environment(\.isEnabled) private var isEnabled

    var body: some View {
        let state = style.stateOverride ?? .make(isPressed: isPressed, isEnabled: isEnabled, isFocused: style.isFocused)
        let resolved = TSTabsStyleSpec.resolve(.init(variant: style.variant, isSelected: style.isSelected, state: state))
        let isUnderline = style.variant == .underline
        label
            .foregroundColor(resolved.labelColor.color)
            .tsFont(resolved.labelFont)
            .lineLimit(1)
            .minimumScaleFactor(isUnderline ? 1 : TSTabsStyleSpec.segmentMinimumScaleFactor)
            .fixedSize(horizontal: isUnderline, vertical: true)
            .padding(.vertical, resolved.paddingVertical)
            .padding(.horizontal, resolved.paddingHorizontal)
            .frame(maxWidth: style.isBlock && !isUnderline ? .infinity : nil, minHeight: resolved.minHeight, maxHeight: .infinity)
            .background(TSTabSelection(resolved: resolved, isSelected: style.isSelected, namespace: style.namespace))
            .tsFocusRing(isFocused: resolved.showsFocusRing, cornerRadius: resolved.cornerRadius)
            .contentShape(Rectangle())
            .tsMinimumHitArea(outset: TSTabsStyleSpec.hitOutset(style.variant), edges: .vertical)
            .opacity(resolved.opacity)
            .animation(TSAnimation.pressFeedback, value: state)
    }
}

struct TSTabSelection: View {
    static let matchID = "ts.tabs.selection"

    let resolved: TSTabsStyleSpec.Resolved
    let isSelected: Bool
    let namespace: Namespace.ID

    var body: some View {
        ZStack(alignment: .bottom) {
            if let fill = resolved.fill {
                let shape = RoundedRectangle(cornerRadius: resolved.cornerRadius, style: .continuous)
                if isSelected {
                    shape
                        .fill(fill.color)
                        .tsShadow(resolved.shadow ?? TSShadowTokens.shadowSm)
                        .matchedGeometryEffect(id: Self.matchID, in: namespace)
                } else {
                    shape.fill(fill.color)
                }
            }
            if let indicator = resolved.indicatorColor {
                Rectangle()
                    .fill(indicator.color)
                    .frame(height: resolved.indicatorHeight)
                    .matchedGeometryEffect(id: Self.matchID, in: namespace)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
        .accessibilityHidden(true)
    }
}
