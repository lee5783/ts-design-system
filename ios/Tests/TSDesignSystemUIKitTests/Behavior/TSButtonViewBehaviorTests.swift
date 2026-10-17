import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSButtonView behaviour")
struct TSButtonViewBehaviorTests {
    private func makeButton(variant: TSButtonVariant = .primary, counter: TapCounter) -> TSButtonView {
        let button = TSButtonView(title: "Save changes", variant: variant, primaryAction: UIAction { _ in counter.tap() })
        button.frame = CGRect(origin: .zero, size: button.systemLayoutSizeFitting(UIView.layoutFittingCompressedSize))
        button.layoutIfNeeded()
        return button
    }

    @Test(".touchUpInside fires the primary action", arguments: TSButtonVariant.allCases)
    func touchUpInsideFires(variant: TSButtonVariant) {
        let counter = TapCounter()
        let button = makeButton(variant: variant, counter: counter)
        button.sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
    }

    @Test("isEnabled = false blocks actions and adds the not-enabled trait")
    func disabledBlocks() {
        let counter = TapCounter()
        let button = makeButton(counter: counter)
        button.isEnabled = false
        button.layoutIfNeeded()
        button.sendActions(for: .touchUpInside)
        #expect(counter.count == 0)
        #expect(button.accessibilityTraits.contains(.notEnabled))
        #expect(button.accessibilityTraits.contains(.button))
        #expect(abs(button.alpha - 0.45) < 0.001)
    }

    @Test("Loading blocks actions and touches, keeps full opacity and reads Loading")
    func loadingBlocks() {
        let counter = TapCounter()
        let button = makeButton(counter: counter)
        button.isLoading = true
        button.layoutIfNeeded()
        button.sendActions(for: .touchUpInside)
        #expect(counter.count == 0)
        #expect(!button.isUserInteractionEnabled)
        #expect(button.accessibilityTraits.contains(.notEnabled))
        #expect(button.accessibilityValue == "Loading")
        #expect(button.accessibilityLabel == "Save changes")
        #expect(button.alpha == 1)
        button.isLoading = false
        button.layoutIfNeeded()
        button.sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
        #expect(button.accessibilityValue == nil)
        #expect(!button.accessibilityTraits.contains(.notEnabled))
    }

    @Test("Traits and label at rest")
    func accessibilityAtRest() {
        let button = makeButton(counter: TapCounter())
        #expect(button.accessibilityTraits.contains(.button))
        #expect(!button.accessibilityTraits.contains(.notEnabled))
        #expect(button.accessibilityLabel == "Save changes")
        #expect(button.accessibilityValue == nil)
    }

    @Test("Height is at least the control height for the size", arguments: TSButtonSize.allCases)
    func minimumHeight(size: TSButtonSize) {
        let button = TSButtonView(title: "Get started", size: size)
        button.layoutIfNeeded()
        #expect(button.intrinsicContentSize.height >= (size == .md ? 44 : 52))
    }

    @Test("A link keeps a hit area of at least 44 pt around its text")
    func linkHitArea() {
        let button = TSButtonView(title: "Learn more", variant: .link)
        let fitting = button.systemLayoutSizeFitting(UIView.layoutFittingCompressedSize)
        button.frame = CGRect(origin: .zero, size: fitting)
        #expect(fitting.height < 44)
        let outset = (44 - fitting.height) / 2
        #expect(button.point(inside: CGPoint(x: fitting.width / 2, y: -outset + 0.5), with: nil))
        #expect(button.point(inside: CGPoint(x: fitting.width / 2, y: fitting.height + outset - 0.5), with: nil))
        #expect(!button.point(inside: CGPoint(x: fitting.width / 2, y: -outset - 1), with: nil))
    }

    @Test("Highlighting resolves the pressed fill from the spec", arguments: TSButtonVariant.allCases)
    func pressedFill(variant: TSButtonVariant) {
        let button = makeButton(variant: variant, counter: TapCounter())
        button.isHighlighted = true
        button.layoutIfNeeded()
        let expected = TSButtonStyleSpec.resolve(.init(variant: variant, state: .pressed)).fill?.uiColor ?? .clear
        let actual = button.configuration?.background.backgroundColor ?? .clear
        let traits = UITraitCollection(userInterfaceStyle: .light)
        #expect(actual.resolvedColor(with: traits) == expected.resolvedColor(with: traits))
    }

    @Test("Text grows with Dynamic Type instead of truncating")
    func dynamicType() {
        let regular = TSButtonView(title: "Save changes")
        let large = TSButtonView(title: "Save changes")
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        host.content.addSubview(large)
        large.layoutIfNeeded()
        regular.layoutIfNeeded()
        #expect(large.traitCollection.preferredContentSizeCategory == .accessibilityExtraExtraExtraLarge)
        let regularHeight = regular.intrinsicContentSize.height
        let largeHeight = large.intrinsicContentSize.height
        #expect(largeHeight > regularHeight)
        #expect(large.titleLabel?.numberOfLines == 0)
    }

    @Test("Corners stay at the spec radius at every text size, never the system's dynamic radius", arguments: TSButtonVariant.allCases)
    func fixedCorners(variant: TSButtonVariant) {
        let button = TSButtonView(title: "Save changes", variant: variant)
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        host.content.addSubview(button)
        button.layoutIfNeeded()
        button.updateConfiguration()
        #expect(button.configuration?.cornerStyle == .fixed)
        #expect(button.configuration?.background.cornerRadius == button.resolved.cornerRadius)
    }

    @Test("A disabled leading icon renders like the enabled one; the 0.45 view alpha is the only dimming")
    func disabledIconDimmedOnce() throws {
        let enabled = try leadingIconInk(isEnabled: true)
        let disabled = try leadingIconInk(isEnabled: false)
        #expect(enabled.alpha == 255)
        #expect(RenderedInk.isClose(disabled, enabled))
    }

    private func leadingIconInk(isEnabled: Bool) throws -> RenderedInk.Pixel {
        let button = TSButtonView(title: "Delete", variant: .secondary, leadingIcon: .trash)
        button.isEnabled = isEnabled
        button.frame = CGRect(origin: .zero, size: button.systemLayoutSizeFitting(UIView.layoutFittingCompressedSize))
        button.updateConfiguration()
        button.layoutIfNeeded()
        let imageView = try #require(button.imageView)
        return try #require(RenderedInk.strongest(in: imageView))
    }
}
