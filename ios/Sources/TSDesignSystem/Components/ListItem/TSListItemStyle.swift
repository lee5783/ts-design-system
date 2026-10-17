import SwiftUI

struct TSListItemStyle: ButtonStyle {
    let isFocused: Bool
    let stateOverride: TSControlState?

    func makeBody(configuration: Configuration) -> some View {
        TSListItemStyleBody(label: configuration.label, isPressed: configuration.isPressed, style: self)
    }
}

struct TSListItemStyleBody<Label: View>: View {
    let label: Label
    let isPressed: Bool
    let style: TSListItemStyle

    @Environment(\.isEnabled) private var isEnabled

    var body: some View {
        let state = style.stateOverride ?? .make(isPressed: isPressed, isEnabled: isEnabled, isFocused: style.isFocused)
        label.modifier(TSListItemChrome(state: state, isInteractive: true))
    }
}

struct TSListItemChrome: ViewModifier {
    let state: TSControlState
    let isInteractive: Bool

    @Environment(\.tsListShowsDividers) private var showsDivider
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    func body(content: Content) -> some View {
        let resolved = TSListItemStyleSpec.resolve(.init(isInteractive: isInteractive, state: state))
        let group = TSListItemStyleSpec.group
        content
            .padding(.horizontal, resolved.horizontalPadding)
            .padding(.vertical, resolved.verticalPadding)
            .frame(maxWidth: .infinity, minHeight: resolved.minHeight, alignment: .leading)
            .background(resolved.background?.color ?? Color.clear)
            .contentShape(Rectangle())
            .opacity(resolved.opacity)
            .overlay(
                Rectangle()
                    .fill(showsDivider ? group.divider.color : Color.clear)
                    .frame(height: group.dividerThickness)
                    .padding(.leading, group.dividerInset)
                    .accessibilityHidden(true),
                alignment: .top
            )
            .animation(TSPressFeedback.animation(reduceMotion: reduceMotion), value: state)
    }
}

struct TSListItemContent<Leading: View, Trailing: View>: View {
    let title: String
    let subtitle: String?
    let showsChevron: Bool
    let leading: Leading
    let trailing: Trailing

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private var hasLeading: Bool {
        Leading.self != EmptyView.self
    }

    private var hasTrailing: Bool {
        Trailing.self != EmptyView.self
    }

    var body: some View {
        let layout = TSListItemLayout.make(isAccessibilitySize: dynamicTypeSize.isAccessibilitySize)
        let resolved = TSListItemStyleSpec.resolve(.init(hasSubtitle: subtitle != nil, layout: layout))
        HStack(spacing: resolved.spacing) {
            if resolved.isStacked {
                VStack(alignment: .leading, spacing: resolved.stackedSpacing) {
                    leadingSlot(resolved)
                    text(resolved)
                    if hasTrailing {
                        trailingSlot(resolved)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                if showsChevron {
                    chevron(resolved)
                }
            } else {
                leadingSlot(resolved)
                text(resolved)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if hasTrailing || showsChevron {
                    HStack(spacing: resolved.trailingSpacing) {
                        if hasTrailing {
                            trailingSlot(resolved)
                        }
                        if showsChevron {
                            chevron(resolved)
                        }
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func leadingSlot(_ resolved: TSListItemStyleSpec.Resolved) -> some View {
        if hasLeading {
            leading
                .foregroundColor(resolved.leadingColor.color)
                .accessibilityHidden(true)
        }
    }

    private func text(_ resolved: TSListItemStyleSpec.Resolved) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .foregroundColor(resolved.titleColor.color)
                .lineLimit(resolved.titleLineLimit)
                .multilineTextAlignment(.leading)
                .fixedSize(horizontal: false, vertical: true)
                .tsFont(resolved.titleFont)
            if let subtitle, resolved.showsSubtitle {
                Text(subtitle)
                    .foregroundColor(resolved.subtitleColor.color)
                    .lineLimit(resolved.subtitleLineLimit)
                    .multilineTextAlignment(.leading)
                    .fixedSize(horizontal: false, vertical: true)
                    .tsFont(resolved.subtitleFont)
            }
        }
    }

    private func trailingSlot(_ resolved: TSListItemStyleSpec.Resolved) -> some View {
        trailing
            .foregroundColor(resolved.trailingColor.color)
            .font(resolved.trailingFont.font(for: UIContentSizeCategory(dynamicTypeSize)))
            .lineLimit(resolved.isStacked ? nil : 1)
    }

    private func chevron(_ resolved: TSListItemStyleSpec.Resolved) -> some View {
        TSIcon(resolved.chevron, size: resolved.chevronSize)
            .foregroundColor(resolved.chevronColor.color)
            .accessibilityHidden(true)
    }
}
