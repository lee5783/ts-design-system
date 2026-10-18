import Testing
import UIKit
@testable import TSDesignSystemUIKit
import TSTestSupport

@MainActor
@Suite("TSIconButtonView behaviour")
struct TSIconButtonViewBehaviorTests {
    @Test(".touchUpInside fires the primary action", arguments: TSIconButtonVariant.allCases)
    func touchUpInsideFires(variant: TSIconButtonVariant) {
        let counter = TapCounter()
        let button = TSIconButtonView(icon: .search, label: "Search", variant: variant, primaryAction: UIAction { _ in counter.tap() })
        button.sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
    }

    @Test("isEnabled = false blocks actions")
    func disabledBlocks() {
        let counter = TapCounter()
        let button = TSIconButtonView(icon: .close, label: "Close", primaryAction: UIAction { _ in counter.tap() })
        button.isEnabled = false
        button.sendActions(for: .touchUpInside)
        #expect(counter.count == 0)
        #expect(button.accessibilityTraits.contains(.notEnabled))
    }

    @Test("The label is the accessibility label, with the button trait and large content title")
    func accessibility() {
        let button = TSIconButtonView(icon: .more, label: "More options", isRound: true)
        button.updateConfiguration()
        #expect(button.accessibilityLabel == "More options")
        #expect(button.accessibilityTraits.contains(.button))
        #expect(button.showsLargeContentViewer)
        #expect(button.largeContentTitle == "More options")
    }

    @Test("The control stays 44 × 44, even at AX5")
    func size() {
        let button = TSIconButtonView(icon: .plus, label: "Add")
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        button.translatesAutoresizingMaskIntoConstraints = false
        host.content.addSubview(button)
        host.window.layoutIfNeeded()
        #expect(button.intrinsicContentSize == CGSize(width: 44, height: 44))
        #expect(button.bounds.size == CGSize(width: 44, height: 44))
    }

    @Test("Square and round buttons keep their spec radius at AX5, so a square button never turns into a circle", arguments: [false, true])
    func fixedCorners(isRound: Bool) {
        let button = TSIconButtonView(icon: .plus, label: "Add", isRound: isRound)
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        button.translatesAutoresizingMaskIntoConstraints = false
        host.content.addSubview(button)
        host.window.layoutIfNeeded()
        button.updateConfiguration()
        #expect(button.configuration?.cornerStyle == .fixed)
        #expect(button.configuration?.background.cornerRadius == button.resolved.cornerRadius)
        #expect(isRound || button.resolved.cornerRadius < button.resolved.size / 2)
    }

    @Test("A disabled icon renders like the enabled one; the 0.45 view alpha is the only dimming", arguments: TSIconButtonVariant.allCases)
    func disabledIconDimmedOnce(variant: TSIconButtonVariant) throws {
        let enabled = try iconInk(variant: variant, isEnabled: true)
        let disabled = try iconInk(variant: variant, isEnabled: false)
        #expect(enabled.alpha == 255)
        #expect(RenderedInk.isClose(disabled, enabled))
    }

    private func iconInk(variant: TSIconButtonVariant, isEnabled: Bool) throws -> RenderedInk.Pixel {
        let button = TSIconButtonView(icon: .trash, label: "Delete", variant: variant)
        button.isEnabled = isEnabled
        button.frame = CGRect(x: 0, y: 0, width: 44, height: 44)
        button.updateConfiguration()
        button.layoutIfNeeded()
        #expect(abs(button.alpha - (isEnabled ? 1 : TSOpacity.opacityDisabled)) < 0.001)
        let imageView = try #require(button.imageView)
        return try #require(RenderedInk.strongest(in: imageView))
    }

    @Test("A consumer image keeps the label, the large content title and the resolved icon size")
    func consumerImage() {
        let button = TSIconButtonView(iconImage: TSSampleIconImage.controller(side: 48), label: "Controller", variant: .secondary)
        button.updateConfiguration()
        let image = button.configuration?.image
        #expect(image?.size == CGSize(width: button.resolved.iconSize, height: button.resolved.iconSize))
        #expect(image?.renderingMode == .alwaysTemplate)
        #expect(button.accessibilityLabel == "Controller")
        #expect(button.largeContentTitle == "Controller")
        #expect(button.largeContentImage?.renderingMode == .alwaysTemplate)
        #expect(button.intrinsicContentSize == CGSize(width: 44, height: 44))
    }
}
