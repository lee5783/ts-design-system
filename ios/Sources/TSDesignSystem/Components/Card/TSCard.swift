import SwiftUI

public struct TSCard<Accessory: View, Content: View>: View {
    let title: String?
    let subtitle: String?
    let isRaised: Bool
    let footerActions: [TSCardAction]
    let accessibilityActions: [TSCardAction]
    let action: (() -> Void)?
    let accessory: Accessory
    let content: Content
    var cardAccessory: TSCardAccessory?
    var bodyText: String?
    var stateOverride: TSControlState?

    @FocusState private var isFocused: Bool
    @Environment(\.isEnabled) private var isEnabled

    public init(
        title: String? = nil,
        subtitle: String? = nil,
        isRaised: Bool = false,
        footerActions: [TSCardAction] = [],
        accessibilityActions: [TSCardAction] = [],
        action: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content,
        @ViewBuilder accessory: () -> Accessory
    ) {
        self.title = title
        self.subtitle = subtitle
        self.isRaised = isRaised
        self.footerActions = footerActions
        self.accessibilityActions = accessibilityActions
        self.action = action
        self.accessory = accessory()
        self.content = content()
    }

    var isInteractive: Bool {
        action != nil
    }

    var customActions: [TSCardAction] {
        TSCardAccessibility.customActions(footer: footerActions, accessory: cardAccessory, additional: accessibilityActions)
    }

    public var body: some View {
        if isInteractive {
            interactiveCard
        } else {
            staticCard
        }
    }

    func forcingState(_ state: TSControlState) -> Self {
        var copy = self
        copy.stateOverride = state
        return copy
    }

    private var cardContent: TSCardContent<Accessory, Content> {
        TSCardContent(
            title: title,
            subtitle: subtitle,
            footerActions: footerActions,
            isInteractive: isInteractive,
            accessory: accessory,
            content: content
        )
    }

    private var staticCard: some View {
        cardContent
            .modifier(TSCardChrome(state: stateOverride ?? .make(isEnabled: isEnabled), isRaised: isRaised, isInteractive: false))
            .accessibilityElement(children: .contain)
    }

    private var interactiveCard: some View {
        cardContent
            .background(
                Button(action: perform) {
                    Color.clear
                }
                .buttonStyle(TSCardStyle(isRaised: isRaised, isFocused: isFocused, stateOverride: stateOverride))
                .focused($isFocused)
                .tsFocusEffectDisabled()
            )
            .modifier(TSCardChrome(state: interactiveState, isRaised: isRaised, isInteractive: true, drawsFill: false))
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(Text(TSCardAccessibility.label(title: title, subtitle: subtitle, body: bodyText)))
            .accessibilityValue(Text(TSCardAccessibility.value(title: title, subtitle: subtitle, body: bodyText) ?? ""))
            .accessibilityAddTraits(.isButton)
            .accessibilityAction { perform() }
            .modifier(TSAccessibilityCustomActions(actions: customActions, isEnabled: isEnabled))
    }

    private var interactiveState: TSControlState {
        stateOverride ?? .make(isEnabled: isEnabled, isFocused: isFocused)
    }

    func perform() {
        guard isEnabled else { return }
        action?()
    }
}

public extension TSCard where Accessory == EmptyView {
    init(
        title: String? = nil,
        subtitle: String? = nil,
        isRaised: Bool = false,
        footerActions: [TSCardAction] = [],
        accessibilityActions: [TSCardAction] = [],
        action: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.init(
            title: title,
            subtitle: subtitle,
            isRaised: isRaised,
            footerActions: footerActions,
            accessibilityActions: accessibilityActions,
            action: action,
            content: content,
            accessory: { EmptyView() }
        )
    }
}

public extension TSCard where Content == Text {
    init(
        title: String? = nil,
        subtitle: String? = nil,
        text: String,
        isRaised: Bool = false,
        footerActions: [TSCardAction] = [],
        accessibilityActions: [TSCardAction] = [],
        action: (() -> Void)? = nil,
        @ViewBuilder accessory: () -> Accessory
    ) {
        self.init(
            title: title,
            subtitle: subtitle,
            isRaised: isRaised,
            footerActions: footerActions,
            accessibilityActions: accessibilityActions,
            action: action,
            content: { Text(text) },
            accessory: accessory
        )
        bodyText = text
    }
}

public extension TSCard where Accessory == TSCardAccessoryButton {
    init(
        title: String? = nil,
        subtitle: String? = nil,
        isRaised: Bool = false,
        footerActions: [TSCardAction] = [],
        accessibilityActions: [TSCardAction] = [],
        accessory: TSCardAccessory,
        action: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content
    ) {
        self.init(
            title: title,
            subtitle: subtitle,
            isRaised: isRaised,
            footerActions: footerActions,
            accessibilityActions: accessibilityActions,
            action: action,
            content: content,
            accessory: { TSCardAccessoryButton(accessory: accessory) }
        )
        cardAccessory = accessory
    }
}

public extension TSCard where Accessory == TSCardAccessoryButton, Content == Text {
    init(
        title: String? = nil,
        subtitle: String? = nil,
        text: String,
        isRaised: Bool = false,
        footerActions: [TSCardAction] = [],
        accessibilityActions: [TSCardAction] = [],
        accessory: TSCardAccessory,
        action: (() -> Void)? = nil
    ) {
        self.init(
            title: title,
            subtitle: subtitle,
            isRaised: isRaised,
            footerActions: footerActions,
            accessibilityActions: accessibilityActions,
            accessory: accessory,
            action: action,
            content: { Text(text) }
        )
        bodyText = text
    }
}

public extension TSCard where Accessory == EmptyView, Content == Text {
    init(
        title: String? = nil,
        subtitle: String? = nil,
        text: String,
        isRaised: Bool = false,
        footerActions: [TSCardAction] = [],
        accessibilityActions: [TSCardAction] = [],
        action: (() -> Void)? = nil
    ) {
        self.init(
            title: title,
            subtitle: subtitle,
            isRaised: isRaised,
            footerActions: footerActions,
            accessibilityActions: accessibilityActions,
            action: action,
            content: { Text(text) },
            accessory: { EmptyView() }
        )
        bodyText = text
    }
}

public struct TSCardAccessoryButton: View {
    let accessory: TSCardAccessory

    public var body: some View {
        switch accessory {
        case let .iconButton(icon, label, action):
            TSIconButton(icon, label: label) { action() }
        }
    }
}
