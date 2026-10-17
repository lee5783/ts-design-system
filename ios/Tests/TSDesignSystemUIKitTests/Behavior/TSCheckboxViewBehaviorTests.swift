import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSCheckboxView behaviour")
struct TSCheckboxViewBehaviorTests {
    private func layout<View: UIView>(_ view: View, width: CGFloat = 320) -> View {
        let size = view.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .fittingSizeLevel,
            verticalFittingPriority: .fittingSizeLevel
        )
        view.frame = CGRect(origin: .zero, size: size)
        view.layoutIfNeeded()
        return view
    }

    @Test("A tap flips isOn, fires .valueChanged once and plays a selection haptic")
    func tapToggles() {
        let counter = TapCounter()
        var haptics: [TSHapticEvent] = []
        let checkbox = TSCheckboxView(label: "Email me about updates")
        checkbox.playHaptic = { haptics.append($0) }
        checkbox.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        checkbox.sendActions(for: .touchUpInside)
        #expect(checkbox.isOn)
        #expect(counter.count == 1)
        #expect(haptics == [.selection])
        checkbox.sendActions(for: .touchUpInside)
        #expect(!checkbox.isOn)
        #expect(counter.count == 2)
    }

    @Test("Setting isOn programmatically does not fire .valueChanged")
    func programmaticSetIsSilent() {
        let counter = TapCounter()
        let checkbox = TSCheckboxView(label: "Remember me")
        checkbox.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        checkbox.isOn = true
        #expect(counter.count == 0)
        #expect(!checkbox.checkView.isHidden)
    }

    @Test("isEnabled = false blocks toggling and adds the not-enabled trait")
    func disabledBlocks() {
        let counter = TapCounter()
        let checkbox = TSCheckboxView(label: "Remember me")
        checkbox.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        checkbox.isEnabled = false
        checkbox.sendActions(for: .touchUpInside)
        #expect(!checkbox.accessibilityActivate())
        #expect(!checkbox.isOn)
        #expect(counter.count == 0)
        #expect(checkbox.accessibilityTraits.contains(.notEnabled))
        #expect(abs(checkbox.alpha - 0.45) < 0.001)
    }

    @Test("VoiceOver activation toggles like a tap")
    func accessibilityActivate() {
        let checkbox = TSCheckboxView(label: "Remember me")
        #expect(checkbox.accessibilityActivate())
        #expect(checkbox.isOn)
    }

    @Test(
        "The row is one element: the label, a checked/unchecked value, and the toggle trait on 17+ or the button trait on 15–16",
        arguments: [TSCompatPath.native, .fallback], [false, true]
    )
    func accessibilityHierarchy(path: TSCompatPath, isOn: Bool) {
        let checkbox = layout(TSCheckboxView(label: "Email me about updates", isOn: isOn))
        checkbox.togglePath = path
        #expect(checkbox.isAccessibilityElement)
        #expect(checkbox.accessibilityLabel == "Email me about updates")
        #expect(checkbox.accessibilityValue == (isOn ? "Checked" : "Unchecked"))
        #expect(!checkbox.accessibilityTraits.contains(.selected))
        if path == .native, #available(iOS 17, *) {
            #expect(checkbox.accessibilityTraits.contains(.toggleButton))
            #expect(!checkbox.accessibilityTraits.contains(.button))
        } else {
            #expect(checkbox.accessibilityTraits.contains(.button))
            if #available(iOS 17, *) {
                #expect(!checkbox.accessibilityTraits.contains(.toggleButton))
            }
        }
        #expect(!checkbox.labelView.isAccessibilityElement)
        #expect(!checkbox.checkView.isAccessibilityElement)
    }

    @Test("The whole row is at least 44 pt high and hit-testable across the label", arguments: [false, true])
    func hitArea(isLabelHidden: Bool) {
        let checkbox = layout(TSCheckboxView(label: "Email me about updates", isLabelHidden: isLabelHidden))
        #expect(checkbox.bounds.height >= 44)
        let hit = TSHitArea.expandedRect(checkbox.bounds)
        #expect(hit.width >= 44)
        #expect(hit.height >= 44)
        #expect(checkbox.point(inside: CGPoint(x: checkbox.bounds.maxX - 1, y: checkbox.bounds.midY), with: nil))
        #expect(checkbox.point(inside: CGPoint(x: hit.minX + 1, y: hit.minY + 1), with: nil))
        #expect(checkbox.labelView.isHidden == isLabelHidden)
    }

    @Test("The row grows with the label at AX5")
    func dynamicType() {
        let checkbox = TSCheckboxView(label: "Email me about updates and product news")
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        checkbox.translatesAutoresizingMaskIntoConstraints = false
        host.content.addSubview(checkbox)
        NSLayoutConstraint.activate([
            checkbox.leadingAnchor.constraint(equalTo: host.content.leadingAnchor),
            checkbox.trailingAnchor.constraint(equalTo: host.content.trailingAnchor),
            checkbox.topAnchor.constraint(equalTo: host.content.topAnchor)
        ])
        host.window.layoutIfNeeded()
        #expect(checkbox.bounds.height > 44)
        #expect(checkbox.markView.bounds.size == CGSize(width: 20, height: 20))
    }
}
