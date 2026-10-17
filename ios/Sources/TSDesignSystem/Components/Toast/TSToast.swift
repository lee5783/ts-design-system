import SwiftUI

public struct TSToast: View {
    let message: String
    let tone: TSToastTone
    let actionTitle: String?
    let onAction: (() -> Void)?

    public init(_ message: String, tone: TSToastTone = .neutral, actionTitle: String? = nil, onAction: (() -> Void)? = nil) {
        self.message = message
        self.tone = tone
        self.actionTitle = actionTitle
        self.onAction = onAction
    }

    public init(item: TSToastItem, onAction: (() -> Void)? = nil) {
        self.init(item.message, tone: item.tone, actionTitle: item.action?.title, onAction: onAction ?? item.action.map { action in { action.handler() } })
    }

    var accessibilityLabel: String {
        TSToastAccessibility.label(tone: tone, message: message)
    }

    public var body: some View {
        let resolved = TSToastStyleSpec.resolve(.init(tone: tone, hasAction: actionTitle != nil && onAction != nil))
        HStack(spacing: resolved.spacing) {
            TSToastMessage(message: message, resolved: resolved)
                .accessibilityElement(children: .ignore)
                .accessibilityLabel(Text(accessibilityLabel))
                .accessibilityAddTraits(.isStaticText)
            if let actionTitle, let onAction, resolved.showsAction {
                TSToastActionButton(title: actionTitle, resolved: resolved, action: onAction)
                    .padding(.trailing, -resolved.actionHorizontalPadding)
            }
        }
        .padding(.horizontal, resolved.horizontalPadding)
        .padding(.vertical, resolved.verticalPadding)
        .frame(maxWidth: resolved.maxWidth)
        .background(
            RoundedRectangle(cornerRadius: resolved.cornerRadius, style: .continuous)
                .fill(resolved.fill.color)
                .tsShadow(resolved.shadow)
        )
        .accessibilityElement(children: .contain)
    }
}

struct TSToastMessage: View {
    let message: String
    let resolved: TSToastStyleSpec.Resolved

    var body: some View {
        HStack(spacing: resolved.spacing) {
            if let icon = resolved.icon, let iconColor = resolved.iconColor {
                TSIcon(icon, size: resolved.iconSize)
                    .foregroundColor(iconColor.color)
            }
            Text(message)
                .foregroundColor(resolved.messageColor.color)
                .fixedSize(horizontal: false, vertical: true)
                .tsFont(resolved.messageFont)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

struct TSToastActionButton: View {
    let title: String
    let resolved: TSToastStyleSpec.Resolved
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .underline(resolved.actionIsUnderlined)
                .tsFont(resolved.actionFont)
        }
        .buttonStyle(TSToastActionStyle(resolved: resolved))
    }
}

struct TSToastActionStyle: ButtonStyle {
    let resolved: TSToastStyleSpec.Resolved

    func makeBody(configuration: Configuration) -> some View {
        TSToastActionLabel(configuration: configuration, resolved: resolved)
    }
}

struct TSToastActionLabel: View {
    let configuration: ButtonStyleConfiguration
    let resolved: TSToastStyleSpec.Resolved

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        let lineHeight = resolved.actionFont.lineHeight(for: UIContentSizeCategory(dynamicTypeSize))
        configuration.label
            .foregroundColor(resolved.actionColor.color)
            .fixedSize()
            .padding(.horizontal, resolved.actionHorizontalPadding)
            .tsPressFeedback(.opacity, isPressed: configuration.isPressed)
            .tsMinimumHitArea(outset: TSHitArea.outset(for: lineHeight))
    }
}
