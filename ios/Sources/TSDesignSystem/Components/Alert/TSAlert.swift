import SwiftUI

public struct TSAlert<Action: View>: View {
    let tone: TSAlertTone
    let title: String?
    let message: String?
    let icon: TSIconName?
    let onDismiss: (() -> Void)?
    let action: Action

    public init(
        tone: TSAlertTone = .info,
        title: String? = nil,
        message: String? = nil,
        icon: TSIconName? = nil,
        onDismiss: (() -> Void)? = nil,
        @ViewBuilder action: () -> Action
    ) {
        self.tone = tone
        self.title = title
        self.message = message
        self.icon = icon
        self.onDismiss = onDismiss
        self.action = action()
    }

    private var hasAction: Bool {
        Action.self != EmptyView.self
    }

    public var body: some View {
        let resolved = TSAlertStyleSpec.resolve(.init(tone: tone, hasTitle: title != nil, isDismissible: onDismiss != nil))
        HStack(alignment: .top, spacing: resolved.spacing) {
            TSAlertMessage(title: title, message: message, icon: icon ?? resolved.icon, resolved: resolved)
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
    }
}

public extension TSAlert where Action == EmptyView {
    init(
        tone: TSAlertTone = .info,
        title: String? = nil,
        message: String? = nil,
        icon: TSIconName? = nil,
        onDismiss: (() -> Void)? = nil
    ) {
        self.init(
            tone: tone,
            title: title,
            message: message,
            icon: icon,
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

struct TSAlertDismissButton: View {
    let resolved: TSAlertStyleSpec.Resolved
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            TSIcon(resolved.dismissIcon, size: resolved.dismissIconSize)
        }
        .buttonStyle(TSAlertDismissStyle(resolved: resolved))
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
