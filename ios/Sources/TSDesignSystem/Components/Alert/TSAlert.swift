import SwiftUI

public struct TSAlert<Action: View>: View {
    let tone: TSAlertTone
    let title: String?
    let message: String?
    let icon: TSIconName?
    let announcesOnAppear: Bool
    let onDismiss: (() -> Void)?
    let action: Action
    var announce: @MainActor (String) -> Void = { TSAlertAccessibility.post($0) }

    public init(
        tone: TSAlertTone = .info,
        title: String? = nil,
        message: String? = nil,
        icon: TSIconName? = nil,
        announcesOnAppear: Bool = false,
        onDismiss: (() -> Void)? = nil,
        @ViewBuilder action: () -> Action
    ) {
        self.tone = tone
        self.title = title
        self.message = message
        self.icon = icon
        self.announcesOnAppear = announcesOnAppear
        self.onDismiss = onDismiss
        self.action = action()
    }

    var accessibilityLabel: String {
        TSAlertAccessibility.label(tone: tone, title: title, message: message)
    }

    private var hasAction: Bool {
        Action.self != EmptyView.self
    }

    public var body: some View {
        let resolved = TSAlertStyleSpec.resolve(.init(tone: tone, hasTitle: title != nil, isDismissible: onDismiss != nil))
        HStack(alignment: .top, spacing: resolved.spacing) {
            TSAlertMessage(title: title, message: message, icon: icon ?? resolved.icon, resolved: resolved)
                .accessibilityElement(children: .ignore)
                .accessibilityLabel(Text(accessibilityLabel))
                .accessibilityAddTraits(.isStaticText)
            if hasAction {
                action
            }
            if let onDismiss, resolved.showsDismiss {
                TSAlertDismissButton(resolved: resolved, action: onDismiss)
            }
        }
        .padding(.horizontal, resolved.horizontalPadding + resolved.borderWidth)
        .padding(.vertical, resolved.verticalPadding + resolved.borderWidth)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: resolved.cornerRadius, style: .continuous)
                .fill(resolved.fill.color)
        )
        .accessibilityElement(children: .contain)
        .modifier(TSAlertEscape(onDismiss: onDismiss))
        .onAppear {
            if announcesOnAppear {
                announce(accessibilityLabel)
            }
        }
    }

    func announcing(with announce: @escaping @MainActor (String) -> Void) -> Self {
        var copy = self
        copy.announce = announce
        return copy
    }
}

public extension TSAlert where Action == EmptyView {
    init(
        tone: TSAlertTone = .info,
        title: String? = nil,
        message: String? = nil,
        icon: TSIconName? = nil,
        announcesOnAppear: Bool = false,
        onDismiss: (() -> Void)? = nil
    ) {
        self.init(
            tone: tone,
            title: title,
            message: message,
            icon: icon,
            announcesOnAppear: announcesOnAppear,
            onDismiss: onDismiss,
            action: { EmptyView() }
        )
    }
}

struct TSAlertMessage: View {
    let title: String?
    let message: String?
    let icon: TSIconName
    let resolved: TSAlertStyleSpec.Resolved

    var body: some View {
        HStack(alignment: .top, spacing: resolved.spacing) {
            TSIcon(icon, size: resolved.iconSize)
                .foregroundColor(resolved.iconColor.color)
            VStack(alignment: .leading, spacing: 0) {
                if let title, resolved.showsTitle {
                    Text(title)
                        .foregroundColor(resolved.titleColor.color)
                        .fixedSize(horizontal: false, vertical: true)
                        .tsFont(resolved.titleFont)
                }
                if let message {
                    Text(message)
                        .foregroundColor(resolved.messageColor.color)
                        .fixedSize(horizontal: false, vertical: true)
                        .tsFont(resolved.messageFont)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

struct TSAlertEscape: ViewModifier {
    let onDismiss: (() -> Void)?

    @ViewBuilder
    func body(content: Content) -> some View {
        if let onDismiss {
            content.accessibilityAction(.escape, onDismiss)
        } else {
            content
        }
    }
}

struct TSAlertDismissButton: View {
    let resolved: TSAlertStyleSpec.Resolved
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            TSIcon(resolved.dismissIcon, size: resolved.dismissIconSize)
        }
        .buttonStyle(TSAlertDismissStyle(resolved: resolved))
        .accessibilityLabel(Text(TSStrings.dismiss))
    }
}

struct TSAlertDismissStyle: ButtonStyle {
    let resolved: TSAlertStyleSpec.Resolved

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .foregroundColor(resolved.dismissColor.color)
            .tsPressFeedback(.opacity, isPressed: configuration.isPressed)
            .tsMinimumHitArea(outset: TSHitArea.outset(for: resolved.dismissIconSize.pointSize))
    }
}
