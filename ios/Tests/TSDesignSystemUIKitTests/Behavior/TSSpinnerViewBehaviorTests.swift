import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSSpinnerView behaviour")
struct TSSpinnerViewBehaviorTests {
    @Test("Intrinsic size follows the icon tokens", arguments: zip(TSSpinnerSize.allCases, [CGFloat(16), 20, 24]))
    func intrinsicSize(size: TSSpinnerSize, expected: CGFloat) {
        #expect(TSSpinnerView(size: size).intrinsicContentSize == CGSize(width: expected, height: expected))
    }

    @Test("Stroke and tint come from the spec", arguments: TSSpinnerStyleSpec.allConfigurations)
    func style(configuration: TSSpinnerStyleSpec.Configuration) {
        let view = TSSpinnerView(size: configuration.size, tone: configuration.tone)
        let resolved = TSSpinnerStyleSpec.resolve(configuration)
        #expect(view.lineWidth == resolved.strokeWidth)
        #expect(view.tintColor.resolvedColor(with: view.traitCollection) == resolved.color.uiColor.resolvedColor(with: view.traitCollection))
    }

    @Test("Changing size updates the intrinsic size")
    func sizeChange() {
        let view = TSSpinnerView()
        view.size = .lg
        #expect(view.intrinsicContentSize == CGSize(width: 24, height: 24))
        #expect(view.lineWidth == 3)
    }

    @Test("A public spinner is one element labelled Loading; the Button's internal spinner is hidden")
    func accessibility() {
        let view = TSSpinnerView()
        #expect(view.isAccessibilityElement)
        #expect(view.accessibilityLabel == "Loading")
        #expect(view.accessibilityTraits.contains(.updatesFrequently))
        #expect(!TSSpinnerView(frame: .zero).isAccessibilityElement)
    }

    @Test("It rotates only while in a window and not hidden")
    func rotation() {
        let view = TSSpinnerView()
        #expect(view.layer.animation(forKey: "ts.spinner.rotation") == nil)
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 100, height: 100))
        window.addSubview(view)
        window.isHidden = false
        #expect(view.layer.animation(forKey: "ts.spinner.rotation") != nil)
        view.isHidden = true
        #expect(view.layer.animation(forKey: "ts.spinner.rotation") == nil)
    }
}
