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

    @Test("The tone sets the message icon", arguments: TSAlertTone.allCases)
    func toneIcon(tone: TSAlertTone) throws {
        let sut = TSAlert(tone: tone, title: "Payment failed", message: "Update your card.")
        let message = try sut.inspect().find(TSAlertMessage.self)
        #expect(try message.actualView().icon == TSAlertStyleSpec.icon(tone))
    }

    @Test("A custom icon overrides the tone icon")
    func iconOverride() throws {
        let sut = TSAlert(tone: .info, message: "New sign-in.", icon: .lock)
        #expect(try sut.inspect().find(TSAlertMessage.self).actualView().icon == .lock)
    }

    @Test("The dismiss button fires the callback")
    func dismissFires() throws {
        let counter = TapCounter()
        let sut = TSAlert(tone: .warning, title: "Storage almost full", onDismiss: { counter.tap() })
        let button = try sut.inspect().find(TSAlertDismissButton.self).find(ViewType.Button.self)
        try button.tap()
        #expect(counter.count == 1)
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

    @Test("Padding 12 + 1 border around 20 pt lines; title and message stack")
    func heights() {
        #expect(abs(height(of: TSAlert(message: "Saved.")) - (13 + 20 + 13)) < 1)
        #expect(abs(height(of: TSAlert(title: "Saved", message: "All changes are synced.")) - (13 + 40 + 13)) < 1)
        #expect(abs(height(of: TSAlert(title: "Saved", onDismiss: {})) - (13 + 20 + 13)) < 1)
    }
}
