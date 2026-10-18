import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSSwitchView behaviour")
struct TSSwitchViewBehaviorTests {
    private func layout<View: UIView>(_ view: View) -> View {
        let size = view.systemLayoutSizeFitting(
            CGSize(width: 320, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .fittingSizeLevel,
            verticalFittingPriority: .fittingSizeLevel
        )
        view.frame = CGRect(origin: .zero, size: size)
        view.layoutIfNeeded()
        return view
    }

    private func makeSwitch(isOn: Bool = false) -> TSSwitchView {
        let view = TSSwitchView(label: "Wi-Fi", isOn: isOn)
        view.reduceMotion = { true }
        return layout(view)
    }

    @Test("A tap flips isOn, fires .valueChanged once and plays a selection haptic")
    func tapToggles() {
        let counter = TapCounter()
        var haptics: [TSHapticEvent] = []
        let view = makeSwitch()
        view.playHaptic = { haptics.append($0) }
        view.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        view.sendActions(for: .touchUpInside)
        #expect(view.isOn)
        #expect(counter.count == 1)
        #expect(haptics == [.selection])
        view.sendActions(for: .touchUpInside)
        #expect(!view.isOn)
        #expect(counter.count == 2)
    }

    @Test("setOn(_:animated:) is silent and lands immediately when animations are off")
    func setOnAnimated() {
        let counter = TapCounter()
        let view = makeSwitch()
        view.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        view.setOn(true, animated: true)
        #expect(view.isOn)
        #expect(counter.count == 0)
        #expect(view.thumbView.layer.animationKeys() == nil)
        #expect(abs(view.thumbView.frame.minX - 21) < 0.001)
    }

    @Test("isEnabled = false blocks toggling")
    func disabledBlocks() {
        let counter = TapCounter()
        let view = makeSwitch(isOn: true)
        view.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        view.isEnabled = false
        view.sendActions(for: .touchUpInside)
        #expect(view.isOn)
        #expect(counter.count == 0)
        #expect(abs(view.alpha - 0.45) < 0.001)
    }

    @Test(
        "The thumb sits on the trailing side when on, in LTR and RTL",
        arguments: [(false, UISemanticContentAttribute.forceLeftToRight, 3.0), (true, .forceLeftToRight, 21.0), (false, .forceRightToLeft, 21.0), (true, .forceRightToLeft, 3.0)]
    )
    func thumbSide(isOn: Bool, direction: UISemanticContentAttribute, originX: Double) {
        let view = TSSwitchView(label: "Wi-Fi", isOn: isOn)
        view.semanticContentAttribute = direction
        _ = layout(view)
        #expect(abs(view.thumbView.frame.minX - originX) < 0.001)
        #expect(view.thumbView.frame.size == CGSize(width: 20, height: 20))
        #expect(view.trackView.bounds.size == CGSize(width: 44, height: 26))
    }

    @Test("The row is at least 44 pt high, even with the label hidden")
    func hitArea() {
        let view = layout(TSSwitchView(label: "Wi-Fi", isLabelHidden: true))
        #expect(view.bounds.height >= 44)
        #expect(view.bounds.width == 44)
        #expect(view.point(inside: CGPoint(x: 1, y: 1), with: nil))
        #expect(view.labelView.isHidden)
    }

    @Test("The track uses switch-thumb on accent-primary when on and border-strong when off")
    func colors() {
        let view = makeSwitch(isOn: true)
        let traits = view.traitCollection
        #expect(view.trackView.backgroundColor == TSColor.accentPrimary.uiColor.resolvedColor(with: traits))
        #expect(view.thumbView.backgroundColor == TSColor.switchThumb.uiColor.resolvedColor(with: traits))
        view.isOn = false
        #expect(view.trackView.backgroundColor == TSColor.borderStrong.uiColor.resolvedColor(with: traits))
    }
}
