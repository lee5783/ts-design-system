import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSAlert behaviour")
struct TSAlertBehaviorTests {
    private func height(of view: some View) -> CGFloat {
        let host = UIHostingController(rootView: view)
        return host.sizeThatFits(in: CGSize(width: 358, height: CGFloat.greatestFiniteMagnitude)).height
    }

    @Test("The message is one element read as tone, title and message", arguments: TSAlertTone.allCases)
    func messageElement(tone: TSAlertTone) throws {
        let sut = TSAlert(tone: tone, title: "Payment failed", message: "Update your card.")
        let message = try sut.inspect().find(TSAlertMessage.self)
        let expected = "\(TSStrings.alertTone(tone)), Payment failed, Update your card."
        #expect(try message.accessibilityLabel().string() == expected)
        #expect(try message.actualView().icon == TSAlertStyleSpec.icon(tone))
    }

    @Test("A custom icon overrides the tone icon")
    func iconOverride() throws {
        let sut = TSAlert(tone: .info, message: "New sign-in.", icon: .lock)
        #expect(try sut.inspect().find(TSAlertMessage.self).actualView().icon == .lock)
    }

    @Test("The dismiss button is labelled Dismiss and fires the callback")
    func dismissFires() throws {
        let counter = TapCounter()
        let sut = TSAlert(tone: .warning, title: "Storage almost full", onDismiss: { counter.tap() })
        let button = try sut.inspect().find(TSAlertDismissButton.self).find(ViewType.Button.self)
        #expect(try button.accessibilityLabel().string() == "Dismiss")
        try button.tap()
        #expect(counter.count == 1)
        let escape = try sut.inspect().hStack().modifier(TSAlertEscape.self).actualView()
        escape.onDismiss?()
        #expect(counter.count == 2)
    }

    @Test("Without onDismiss there is no dismiss button")
    func noDismiss() throws {
        let sut = TSAlert(tone: .success, title: "Saved")
        #expect(throws: (any Error).self) {
            try sut.inspect().find(TSAlertDismissButton.self)
        }
    }

    @Test("The action slot stays a separate, focusable element")
    func actionIsSeparate() throws {
        let counter = TapCounter()
        let sut = TSAlert(tone: .danger, title: "Payment failed", message: "Update your card.") {
            TSButton("Retry", variant: .ghost) { counter.tap() }
        }
        try sut.inspect().find(TSButton.self).find(ViewType.Button.self).tap()
        #expect(counter.count == 1)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(TSAlertMessage.self).find(TSButton.self)
        }
    }

    @Test("An alert shown dynamically posts its announcement when it appears; a static alert does not")
    func announcement() throws {
        let log = AnnouncementLog()
        let dynamic = TSAlert(tone: .danger, title: "Payment failed", message: "Update your card.", announcesOnAppear: true)
            .announcing { log.record($0) }
        try dynamic.inspect().hStack().callOnAppear()
        #expect(log.messages == ["Error, Payment failed, Update your card."])
        let quiet = TSAlert(tone: .info, title: "Heads up").announcing { log.record($0) }
        try quiet.inspect().hStack().callOnAppear()
        #expect(log.messages.count == 1)
    }

    @Test("Padding 12 + 1 border around 20 pt lines; title and message stack")
    func heights() {
        #expect(abs(height(of: TSAlert(message: "Saved.")) - (13 + 20 + 13)) < 1)
        #expect(abs(height(of: TSAlert(title: "Saved", message: "All changes are synced.")) - (13 + 40 + 13)) < 1)
        #expect(abs(height(of: TSAlert(title: "Saved", onDismiss: {})) - (13 + 20 + 13)) < 1)
    }
}
