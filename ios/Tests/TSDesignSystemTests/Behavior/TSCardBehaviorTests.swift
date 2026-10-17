import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSCard behaviour")
struct TSCardBehaviorTests {
    private typealias TextCardContent = TSCardContent<EmptyView, Text>

    private func height(of view: some View, dynamicTypeSize: DynamicTypeSize = .large) -> CGFloat {
        let host = UIHostingController(rootView: view.environment(\.dynamicTypeSize, dynamicTypeSize))
        return host.sizeThatFits(in: CGSize(width: 358, height: CGFloat.greatestFiniteMagnitude)).height
    }

    private func callCustomAction(_ name: String, on card: some View) throws {
        let modifier = try card.inspect().find(TextCardContent.self).modifier(TSAccessibilityCustomActions.self).actualView()
        let actions = modifier.isEnabled ? modifier.actions : []
        guard let action = actions.first(where: { $0.label == name }) else {
            throw InspectionError.notSupported("No accessibility action named \(name)")
        }
        action.handler()
    }

    @Test("An interactive card is one button element labelled by title and subtitle")
    func interactiveIsOneElement() throws {
        let sut = TSCard(title: "Weekly report", subtitle: "Updated today", text: "Revenue is up 12%.", action: {})
        let element = try sut.inspect().find(TextCardContent.self)
        #expect(try element.accessibilityLabel().string() == "Weekly report, Updated today")
        #expect(try element.accessibilityTraits().contains(.isButton))
    }

    @Test("A typed accessory renders an icon button and is also an accessibility custom action")
    func typedAccessory() throws {
        var fired = 0
        let sut = TSCard(
            title: "Weekly report",
            text: "Revenue is up 12%.",
            accessory: .iconButton(icon: .more, label: "More options") { fired += 1 },
            action: {}
        )
        #expect(sut.customActions.map(\.label) == ["More options"])
        let content = try sut.inspect().find(TSCardContent<TSCardAccessoryButton, Text>.self)
        let modifier = try content.modifier(TSAccessibilityCustomActions.self).actualView()
        #expect(modifier.isEnabled)
        try #require(modifier.actions.first).handler()
        #expect(fired == 1)
        try sut.inspect().find(TSIconButton.self).button().tap()
        #expect(fired == 2)
    }

    @Test("The body text is read: as the value under a heading, or as the label when there is no heading")
    func bodyIsRead() throws {
        let headed = try TSCard(title: "Weekly report", text: "Revenue is up 12%.", action: {}).inspect().find(TextCardContent.self)
        #expect(try headed.accessibilityLabel().string() == "Weekly report")
        #expect(try headed.accessibilityValue().string() == "Revenue is up 12%.")
        let textOnly = try TSCard(text: "Revenue is up 12%.", action: {}).inspect().find(TextCardContent.self)
        #expect(try textOnly.accessibilityLabel().string() == "Revenue is up 12%.")
    }

    @Test("An interactive card fires its action from a tap and from accessibility activation")
    func actionFires() throws {
        let counter = TapCounter()
        let sut = TSCard(title: "Weekly report", text: "Revenue is up 12%.", action: { counter.tap() })
        try sut.inspect().find(ViewType.Button.self).tap()
        #expect(counter.count == 1)
        sut.perform()
        #expect(counter.count == 2)
    }

    @Test("An interactive card exposes its footer and extra actions as accessibility custom actions")
    func customActions() throws {
        var fired: [String] = []
        let sut = TSCard(
            title: "Weekly report",
            text: "Revenue is up 12%.",
            footerActions: [TSCardAction("Share") { fired.append("Share") }, TSCardAction("Open", variant: .primary) { fired.append("Open") }],
            accessibilityActions: [TSCardAction("More options") { fired.append("More options") }],
            action: {}
        )
        #expect(sut.customActions.map(\.label) == ["Share", "Open", "More options"])
        try callCustomAction("Open", on: sut)
        try callCustomAction("More options", on: sut)
        try callCustomAction("Share", on: sut)
        #expect(fired == ["Open", "More options", "Share"])
    }

    @Test("Footer actions render as buttons that fire their handlers")
    func footerButtonsFire() throws {
        var fired: [String] = []
        let sut = TSCard(title: "Invite", text: "Invite your team.", footerActions: [TSCardAction("Cancel") { fired.append("Cancel") }, TSCardAction("Send", variant: .primary) { fired.append("Send") }])
        let buttons = try sut.inspect().findAll(TSButton.self)
        #expect(buttons.count == 2)
        try buttons[1].find(ViewType.Button.self).tap()
        #expect(fired == ["Send"])
    }

    @Test("A static card is not a button: its children stay separate elements")
    func staticCardContainsChildren() throws {
        let sut = TSCard(title: "Weekly report", subtitle: "Updated today", text: "Revenue is up 12%.")
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self)
        }
        let element = try sut.inspect().find(TextCardContent.self)
        #expect((try? element.accessibilityTraits().contains(.isButton)) != true)
        #expect(try sut.inspect().find(text: "Weekly report").accessibilityTraits().contains(.isHeader))
    }

    @Test("A disabled interactive card blocks its action and drops its custom actions")
    func disabledBlocks() throws {
        let counter = TapCounter()
        let card = TSCard(title: "Weekly report", text: "Body", footerActions: [TSCardAction("Share") { counter.tap() }], action: { counter.tap() })
        #expect(throws: (any Error).self) {
            try card.disabled(true).inspect().find(ViewType.Button.self).tap()
        }
        #expect(throws: (any Error).self) {
            try callCustomAction("Share", on: card.environment(\.isEnabled, false))
        }
        #expect(counter.count == 0)
    }

    @Test("Pressed draws surface-pressed only on an interactive card")
    func pressedTreatment() throws {
        let sut = TSCard(title: "Weekly report", text: "Body", action: {})
        let button = try sut.inspect().find(ViewType.Button.self)
        let style = try #require(try button.buttonStyle() as? TSCardStyle)
        let pressed = try style.inspect(isPressed: true).find(TSCardStyleBody<ButtonStyleConfiguration.Label>.self).actualView()
        #expect(pressed.state == .pressed)
        #expect(TSCardStyleSpec.resolve(.init(isInteractive: true, state: pressed.state)).fill == .surfacePressed)
        let released = try style.inspect(isPressed: false).find(TSCardStyleBody<ButtonStyleConfiguration.Label>.self).actualView()
        #expect(released.state == .normal)
        let chrome = try TSCard(title: "Static", text: "Body").forcingState(.pressed).inspect().find(TextCardContent.self).modifier(TSCardChrome.self).actualView()
        #expect(!chrome.isInteractive)
        #expect(TSCardStyleSpec.resolve(.init(isInteractive: chrome.isInteractive, state: chrome.state)).fill == .surfaceCard)
    }

    @Test("Header, body and footer stack with the DS paddings")
    func heights() {
        let titleOnly = height(of: TSCard(title: "Weekly report") { EmptyView() })
        #expect(abs(titleOnly - (16 + 24 + 16)) < 1)
        let withBody = height(of: TSCard(title: "Weekly report", text: "Revenue is up."))
        #expect(abs(withBody - (16 + 24 + 16 + 20 + 16)) < 1)
        let withFooter = height(of: TSCard(title: "Weekly report", text: "Revenue is up.", footerActions: [TSCardAction("Open") {}]))
        #expect(abs(withFooter - (withBody + 1 + 12 + 44 + 12)) < 1)
    }

    @Test("At AX5 footer actions stack vertically")
    func accessibilityFooterReflow() {
        let card = TSCard(title: "Weekly report", text: "Body", footerActions: [TSCardAction("Cancel") {}, TSCardAction("Open") {}])
        let regular = height(of: card)
        let large = height(of: card, dynamicTypeSize: .accessibility5)
        let buttonHeight = TSFont.label.lineHeight(for: .accessibilityExtraExtraExtraLarge)
        #expect(large > regular)
        #expect(large >= buttonHeight * 2 + 8)
    }

    @Test("A disabled card owns the dimming: its footer and accessory buttons do not dim a second time", arguments: [true, false])
    func containerOwnsDimming(isEnabled: Bool) throws {
        let probe = DimmingProbe()
        let card = TSCard(title: "Weekly report", footerActions: [TSCardAction("Share") {}]) {
            DimmingProbeView(probe: probe)
        }
        let host = UIHostingController(rootView: card.disabled(!isEnabled))
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        window.rootViewController = host
        window.isHidden = false
        host.view.layoutIfNeeded()
        #expect(probe.isDimmedByContainer == !isEnabled)

        let style = TSButtonStyle(variant: .primary, size: .md, isLoading: false, isBlock: false, isFocused: false, stateOverride: .disabled)
        let button = TSButtonStyleBody(label: Text("Share"), isPressed: false, style: style)
            .environment(\.tsDimmedByContainer, !isEnabled)
        #expect(try button.inspect().find(text: "Share").opacity() == (isEnabled ? TSOpacity.opacityDisabled : 1))
        let iconStyle = TSIconButtonStyle(variant: .ghost, isRound: false, isFocused: false, stateOverride: .disabled)
        let icon = TSIconButtonStyleBody(label: Text("More"), isPressed: false, style: iconStyle)
            .environment(\.tsDimmedByContainer, !isEnabled)
        #expect(try icon.inspect().find(text: "More").opacity() == (isEnabled ? TSOpacity.opacityDisabled : 1))
    }
}

@MainActor
private final class DimmingProbe {
    var isDimmedByContainer: Bool?
}

private struct DimmingProbeView: View {
    let probe: DimmingProbe

    @Environment(\.tsDimmedByContainer) private var isDimmedByContainer

    var body: some View {
        probe.isDimmedByContainer = isDimmedByContainer
        return Text("Body")
    }
}
