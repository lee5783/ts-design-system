import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSListItem behaviour")
struct TSListItemBehaviorTests {
    private func height(of view: some View, dynamicTypeSize: DynamicTypeSize = .large) -> CGFloat {
        let host = UIHostingController(rootView: view.environment(\.dynamicTypeSize, dynamicTypeSize))
        return host.sizeThatFits(in: CGSize(width: 390, height: CGFloat.greatestFiniteMagnitude)).height
    }

    @Test("A row with an action is a button that fires the action")
    func actionFires() throws {
        let counter = TapCounter()
        let sut = TSListItem("Inbox", showsChevron: true, action: { counter.tap() })
        try sut.inspect().find(ViewType.Button.self).tap()
        #expect(counter.count == 1)
    }

    @Test("A disabled row does not fire its action")
    func disabledBlocks() throws {
        let counter = TapCounter()
        let sut = TSListItem("Inbox", action: { counter.tap() }).disabled(true)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self).tap()
        }
        #expect(counter.count == 0)
    }

    @Test("Pressed draws surface-pressed only when the row has an action")
    func pressedOnlyWithAction() throws {
        let pressable = TSListItem("Inbox", action: {})
        let button = try pressable.inspect().find(ViewType.Button.self)
        let style = try #require(try button.buttonStyle() as? TSListItemStyle)
        let pressed = try style.inspect(isPressed: true).find(TSListItemStyleBody<ButtonStyleConfiguration.Label>.self)
        let chrome = try pressed.actualView().body.inspect().modifier(TSListItemChrome.self).actualView()
        #expect(chrome.isInteractive)
        #expect(chrome.state == .pressed)

        let plain = TSListItem("Favorites")
        #expect(throws: (any Error).self) {
            try plain.inspect().find(ViewType.Button.self)
        }
        let plainChrome = try plain.inspect().find(TSListItemContent<EmptyView, EmptyView>.self).modifier(TSListItemChrome.self).actualView()
        #expect(!plainChrome.isInteractive)
        #expect(TSListItemStyleSpec.resolve(.init(isInteractive: plainChrome.isInteractive, state: .pressed)).background == nil)
    }

    @Test("A chevron row with an action has the button trait and the navigates hint; an action row has no hint")
    func navigationAccessibility() throws {
        let navigation = try TSListItem("Settings", showsChevron: true, action: {}).inspect().find(ViewType.Button.self)
        #expect(try navigation.accessibilityHint().string() == "Opens another screen")
        let action = try TSListItem("Settings", action: {}).inspect().find(ViewType.Button.self)
        #expect(try action.accessibilityHint().string() == "")
    }

    @Test(
        "A trailing-switch row is one element with switch semantics: label, On/Off, toggle or button trait",
        arguments: [TSCompatPath.native, .fallback], [false, true]
    )
    func switchSemantics(path: TSCompatPath, isOn: Bool) throws {
        let sut = TSListItem("Wi-Fi", subtitle: "Home", isOn: .constant(isOn)).usingTogglePath(path)
        let row = try sut.inspect().find(TSListItemContent<EmptyView, TSListItemSwitch>.self)
        #expect(try row.accessibilityLabel().string() == "Wi-Fi, Home")
        #expect(try row.accessibilityValue().string() == (isOn ? "On" : "Off"))
        let traits = try row.accessibilityTraits()
        #expect(!traits.contains(.isSelected))
        if path == .native, #available(iOS 17, *) {
            #expect(traits.contains(.isToggle))
        } else {
            #expect(traits.contains(.isButton))
        }
    }

    @Test("Activating a switch row flips the binding and plays a selection haptic; the switch itself also toggles")
    func switchActivation() throws {
        let box = ToggleBox()
        var haptics: [TSHapticEvent] = []
        TSListItem("Wi-Fi", isOn: box.binding).playingHaptics { haptics.append($0) }.flipToggle()
        #expect(box.value)
        #expect(haptics == [.selection])
        try TSListItem("Wi-Fi", isOn: box.binding).inspect().find(TSSwitch.self).find(ViewType.Button.self).tap()
        #expect(!box.value)
    }

    @Test("A disabled switch row does not toggle through its accessibility action")
    func switchDisabled() throws {
        let box = ToggleBox()
        let sut = TSListItem("Wi-Fi", isOn: box.binding)
        sut.flipToggle()
        #expect(box.value)
        let disabled = TSListItem("Wi-Fi", isOn: box.binding).disabled(true)
        #expect(throws: (any Error).self) {
            try disabled.inspect().find(TSSwitch.self).find(ViewType.Button.self).tap()
        }
        #expect(box.value)
    }

    @Test("Rows are 48 pt with a title and 68 pt with a subtitle at the default text size")
    func heights() {
        #expect(abs(height(of: TSListItem("Inbox")) - 48) < 1)
        #expect(abs(height(of: TSListItem("Inbox", subtitle: "12 unread messages")) - 68) < 1)
        #expect(abs(height(of: TSListItem("Inbox", subtitle: "12 unread messages", showsChevron: true, action: {})) - 68) < 1)
        #expect(abs(height(of: TSListItem("Wi-Fi", isOn: .constant(true))) - 50) < 1)
    }

    @Test("At AX5 the row reflows vertically: the leading tile sits above the wrapping title")
    func accessibilityReflow() {
        let row = TSListItem("Inbox", leading: { TSListItemIcon(.mail) })
        let defaultHeight = height(of: row)
        #expect(abs(defaultHeight - 60) < 1)
        let titleHeight = TSFont.body.lineHeight(for: .accessibilityExtraExtraExtraLarge)
        let stackedHeight = height(of: row, dynamicTypeSize: .accessibility5)
        #expect(stackedHeight >= 24 + 36 + 8 + titleHeight - 0.5)
        let long = TSListItem(String(repeating: "Downloads over mobile data ", count: 3))
        #expect(height(of: long, dynamicTypeSize: .accessibility5) > 24 + titleHeight * 2)
        #expect(abs(height(of: long) - 48) < 1)
    }

    @Test("A list draws a full-width divider between rows; a standalone row has none")
    func dividers() {
        let list = TSList {
            TSListItem("Inbox")
            TSListItem("Calendar")
        }
        .environment(\.colorScheme, .light)
        let image = RenderedPixels.image(of: list, size: CGSize(width: 390, height: 96))
        #expect(RenderedPixels.isLight(image, at: CGPoint(x: 195, y: 24)))
        let band = Array(stride(from: CGFloat(40), to: 56, by: 0.25))
        #expect(band.contains { !RenderedPixels.isLight(image, at: CGPoint(x: 195, y: $0)) })
        #expect(band.contains { !RenderedPixels.isLight(image, at: CGPoint(x: 3, y: $0)) })
        let standalone = TSListItem("Inbox").environment(\.colorScheme, .light)
        let single = RenderedPixels.image(of: standalone, size: CGSize(width: 390, height: 48))
        #expect((0..<48).allSatisfy { RenderedPixels.isLight(single, at: CGPoint(x: 195, y: CGFloat($0) + 0.5)) })
    }
}
