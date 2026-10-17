import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSAlertView behaviour")
struct TSAlertViewBehaviorTests {
    private func layout<View: UIView>(_ view: View, width: CGFloat = 358) -> View {
        let size = view.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .required,
            verticalFittingPriority: .fittingSizeLevel
        )
        view.frame = CGRect(origin: .zero, size: size)
        view.layoutIfNeeded()
        return view
    }

    @Test("The message is one element read as tone, title and message; the tone sets fill and icon", arguments: TSAlertTone.allCases)
    func messageElement(tone: TSAlertTone) {
        let alert = layout(TSAlertView(tone: tone, title: "Payment failed", message: "Update your card."))
        let traits = alert.traitCollection
        #expect(!alert.isAccessibilityElement)
        #expect(alert.messageView.isAccessibilityElement)
        #expect(alert.messageView.accessibilityLabel == "\(TSStrings.alertTone(tone)), Payment failed, Update your card.")
        #expect(alert.iconView.name == TSAlertStyleSpec.icon(tone))
        #expect(alert.backgroundColor == TSAlertStyleSpec.resolve(.init(tone: tone)).fill.uiColor.resolvedColor(with: traits))
    }

    @Test("The dismiss button is a Dismiss button that fires the callback, from a tap, activation or escape")
    func dismissFires() {
        let counter = TapCounter()
        let alert = layout(TSAlertView(tone: .warning, title: "Storage almost full", onDismiss: { counter.tap() }))
        #expect(!alert.dismissButton.isHidden)
        #expect(alert.dismissButton.accessibilityLabel == "Dismiss")
        #expect(alert.dismissButton.accessibilityTraits.contains(.button))
        alert.dismissButton.sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
        #expect(alert.dismissButton.accessibilityActivate())
        #expect(counter.count == 2)
        #expect(alert.accessibilityPerformEscape())
        #expect(counter.count == 3)
    }

    @Test("Without onDismiss the dismiss button is hidden and escape is not handled")
    func noDismiss() {
        let alert = layout(TSAlertView(tone: .success, title: "Saved"))
        #expect(alert.dismissButton.isHidden)
        #expect(!alert.accessibilityPerformEscape())
    }

    @Test("The dismiss button's hit area is at least 44 × 44")
    func dismissHitArea() {
        let alert = layout(TSAlertView(tone: .info, title: "Heads up", onDismiss: {}))
        let button = alert.dismissButton
        #expect(button.bounds.width == 20)
        #expect(button.point(inside: CGPoint(x: -11, y: -11), with: nil))
        #expect(button.point(inside: CGPoint(x: 31, y: 31), with: nil))
        #expect(!button.point(inside: CGPoint(x: -13, y: 10), with: nil))
    }

    @Test("An action view stays a separate element between the message and the dismiss button")
    func actionView() {
        let counter = TapCounter()
        let alert = TSAlertView(tone: .danger, title: "Payment failed", message: "Update your card.", onDismiss: {})
        let retry = TSButtonView(title: "Retry", variant: .ghost, primaryAction: UIAction { _ in counter.tap() })
        alert.actionView = retry
        _ = layout(alert)
        #expect(alert.stackView.arrangedSubviews == [alert.messageView, retry, alert.dismissButton])
        #expect(!retry.isDescendant(of: alert.messageView))
        retry.sendActions(for: .primaryActionTriggered)
        #expect(counter.count == 1)
    }

    @Test("An alert that announces posts its label when it appears in a window or is unhidden; a static alert stays quiet")
    func announcement() {
        var announcements: [String] = []
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        let behindModal = UIView(frame: window.bounds)
        window.addSubview(behindModal)
        window.isHidden = false
        let alert = TSAlertView(tone: .danger, title: "Payment failed", message: "Update your card.", announcesWhenShown: true)
        alert.announce = { announcements.append($0) }
        window.addSubview(alert)
        #expect(announcements == ["Error, Payment failed, Update your card."])
        alert.isHidden = true
        alert.isHidden = false
        #expect(announcements.count == 2)
        let quiet = TSAlertView(tone: .info, title: "Heads up")
        quiet.announce = { announcements.append($0) }
        window.addSubview(quiet)
        quiet.isHidden = true
        quiet.isHidden = false
        #expect(announcements.count == 2)
    }

    @Test("Padding 12 + 1 border around 20 pt lines; title and message stack")
    func heights() {
        #expect(abs(layout(TSAlertView(message: "Saved.")).bounds.height - (13 + 20 + 13)) < 1)
        #expect(abs(layout(TSAlertView(title: "Saved", message: "All changes are synced.")).bounds.height - (13 + 40 + 13)) < 1)
        #expect(abs(layout(TSAlertView(title: "Saved", onDismiss: {})).bounds.height - (13 + 20 + 13)) < 1)
    }

    @Test("Tone and dismiss icons keep their colours when a modal dims the window's tint", arguments: TSAlertTone.allCases)
    func iconsIgnoreModalTintDimming(tone: TSAlertTone) {
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        let behindModal = UIView(frame: window.bounds)
        window.addSubview(behindModal)
        window.isHidden = false
        let alert = layout(TSAlertView(tone: tone, title: "Payment failed", message: "Update your card.", onDismiss: {}))
        behindModal.addSubview(alert)
        behindModal.tintAdjustmentMode = .dimmed
        let traits = alert.traitCollection
        let resolved = TSAlertStyleSpec.resolve(.init(tone: tone))
        #expect(alert.iconView.tintColor == resolved.iconColor.uiColor.resolvedColor(with: traits))
        #expect(alert.dismissButton.iconView.tintColor == resolved.dismissColor.uiColor.resolvedColor(with: traits))
    }
}
