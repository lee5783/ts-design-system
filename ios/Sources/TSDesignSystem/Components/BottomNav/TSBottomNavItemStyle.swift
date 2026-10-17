import SwiftUI

struct TSBottomNavItemStyle: ButtonStyle {
    let item: TSBottomNavItem
    let isSelected: Bool
    let isFocused: Bool
    let stateOverride: TSControlState?

    func makeBody(configuration: Configuration) -> some View {
        TSBottomNavItemBody(label: configuration.label, isPressed: configuration.isPressed, style: self)
    }
}

struct TSBottomNavItemBody<Label: View>: View {
    let label: Label
    let isPressed: Bool
    let style: TSBottomNavItemStyle

    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let state = style.stateOverride ?? .make(isPressed: isPressed, isEnabled: isEnabled, isFocused: style.isFocused)
        let resolved = TSBottomNavStyleSpec.resolve(.init(isSelected: style.isSelected, state: state))
        VStack(spacing: resolved.spacing) {
            TSBottomNavPill(item: style.item, resolved: resolved)
            label
                .tsFont(resolved.labelFont)
                .lineLimit(1)
                .dynamicTypeSize(...labelMaximumSize)
        }
        .foregroundColor(resolved.foreground.color)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .contentShape(Rectangle())
        .opacity(resolved.opacity)
        .animation(TSPressFeedback.animation(reduceMotion: reduceMotion), value: state)
    }

    private var labelMaximumSize: DynamicTypeSize {
        DynamicTypeSize(TSBottomNavStyleSpec.labelMaximumSizeCategory) ?? .xxxLarge
    }
}

struct TSBottomNavPill: View {
    let item: TSBottomNavItem
    let resolved: TSBottomNavStyleSpec.Resolved

    var body: some View {
        TSIcon(item.icon, size: resolved.iconSize)
            .frame(width: resolved.pillWidth, height: resolved.pillHeight)
            .background(
                Capsule(style: .continuous)
                    .fill(resolved.pillFill?.color ?? .clear)
            )
            .tsFocusRing(isFocused: resolved.showsFocusRing, cornerRadius: resolved.pillCornerRadius)
            .overlay(alignment: .topLeading) {
                if let text = item.badgeText {
                    TSBottomNavBadge(text: text)
                        .padding(.leading, resolved.pillWidth / 2 + TSBottomNavStyleSpec.badge.offsetStart)
                        .padding(.top, TSBottomNavStyleSpec.badge.topFromPill)
                        .fixedSize()
                }
            }
    }
}

struct TSBottomNavBadge: View {
    let text: String

    var body: some View {
        let badge = TSBottomNavStyleSpec.badge
        Text(text)
            .font(Font(TSBottomNavStyleSpec.badgeFont() as CTFont))
            .foregroundColor(badge.foreground.color)
            .lineLimit(1)
            .fixedSize()
            .padding(.horizontal, badge.horizontalPadding)
            .frame(minWidth: badge.minWidth, minHeight: badge.height, maxHeight: badge.height)
            .background(Capsule(style: .continuous).fill(badge.fill.color))
            .accessibilityHidden(true)
    }
}
