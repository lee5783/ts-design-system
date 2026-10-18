import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSToastView behaviour")
struct TSToastViewBehaviorTests {
    private func layout<View: UIView>(_ view: View, width: CGFloat = 358) -> View {
        let size = view.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .defaultHigh,
            verticalFittingPriority: .fittingSizeLevel
        )
        view.frame = CGRect(origin: .zero, size: size)
        view.layoutIfNeeded()
        return view
    }

    @Test("The tone sets the on-inverse icon", arguments: TSToastTone.allCases)
    func messageElement(tone: TSToastTone) {
        let toast = layout(TSToastView("Project archived", tone: tone))
        let traits = toast.traitCollection
        let resolved = TSToastStyleSpec.resolve(.init(tone: tone))
        #expect(!toast.isAccessibilityElement)
        #expect(toast.messageView.isAccessibilityElement)
        #expect(toast.backgroundColor == TSColor.surfaceInverse.uiColor.resolvedColor(with: traits))
        #expect(toast.iconView.isHidden == (resolved.icon == nil))
        if let icon = resolved.icon, let color = resolved.iconColor {
            #expect(toast.iconView.name == icon)
            #expect(toast.iconView.tintColor == color.uiColor.resolvedColor(with: traits))
        }
    }

    @Test("The action is a separate, underlined Undo button that fires from a tap")
    func actionFires() {
        let counter = TapCounter()
        let toast = layout(TSToastView("Project archived", actionTitle: "Undo", onAction: { counter.tap() }))
        let button = toast.actionButton
        #expect(!button.isHidden)
        #expect(!button.isDescendant(of: toast.messageView))
        let underline = button.titleLabel.attributedText?.attribute(.underlineStyle, at: 0, effectiveRange: nil) as? Int
        #expect(underline == NSUnderlineStyle.single.rawValue)
        button.sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
    }

    @Test("Without an action the action button is hidden")
    func noAction() {
        #expect(layout(TSToastView("Saved")).actionButton.isHidden)
        #expect(layout(TSToastView("Saved", actionTitle: "Undo")).actionButton.isHidden)
    }

    @Test("The action's hit area is at least 44 pt tall")
    func actionHitArea() {
        let toast = layout(TSToastView("Project archived", actionTitle: "Undo", onAction: {}))
        let button = toast.actionButton
        #expect(button.bounds.height == 20)
        #expect(button.point(inside: CGPoint(x: button.bounds.midX, y: -11), with: nil))
        #expect(button.point(inside: CGPoint(x: button.bounds.midX, y: 31), with: nil))
        #expect(!button.point(inside: CGPoint(x: button.bounds.midX, y: -13), with: nil))
    }

    @Test("One line is 12 + 20 + 12 tall, with or without an action, and the width stops at 420")
    func geometry() {
        #expect(layout(TSToastView("Saved")).bounds.height == 44)
        #expect(layout(TSToastView("Project archived", actionTitle: "Undo", onAction: {})).bounds.height == 44)
        #expect(layout(TSToastView("Saved"), width: 800).bounds.width <= 420)
    }

    @Test("The tone icon keeps its colour when a modal dims the window's tint", arguments: TSToastTone.allCases)
    func iconIgnoresModalTintDimming(tone: TSToastTone) {
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        let behindModal = UIView(frame: window.bounds)
        window.addSubview(behindModal)
        window.isHidden = false
        let toast = layout(TSToastView("Project archived", tone: tone))
        behindModal.addSubview(toast)
        behindModal.tintAdjustmentMode = .dimmed
        guard let color = TSToastStyleSpec.resolve(.init(tone: tone)).iconColor else { return }
        #expect(toast.iconView.tintColor == color.uiColor.resolvedColor(with: toast.traitCollection))
    }
}
