import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSDisclosureListItemView and TSDividerView behaviour")
struct TSDisclosureListItemViewBehaviorTests {
    private func layout<View: UIView>(_ view: View, width: CGFloat = 390) -> View {
        let size = view.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .required,
            verticalFittingPriority: .fittingSizeLevel
        )
        view.frame = CGRect(origin: .zero, size: size)
        view.layoutIfNeeded()
        return view
    }

    private func makeRow(isExpanded: Bool = false) -> (TSDisclosureListItemView, UILabel) {
        let body = UILabel()
        body.text = "Licence text"
        let row = TSDisclosureListItemView(title: "Kotlin", subtitle: "Apache License 2.0", bodyView: body, isExpanded: isExpanded)
        return (layout(row), body)
    }

    @Test("A tap toggles the body and sends .valueChanged")
    func tapToggles() {
        let counter = TapCounter()
        let (row, _) = makeRow()
        row.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        #expect(row.bodyContainer.isHidden)
        row.sendActions(for: .touchUpInside)
        #expect(row.isExpanded)
        #expect(!row.bodyContainer.isHidden)
        row.sendActions(for: .touchUpInside)
        #expect(!row.isExpanded)
        #expect(counter.count == 2)
    }

    @Test("Setting isExpanded in code does not send .valueChanged")
    func programmatic() {
        let counter = TapCounter()
        let (row, _) = makeRow()
        row.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        row.isExpanded = true
        #expect(!row.bodyContainer.isHidden)
        #expect(counter.count == 0)
    }

    @Test("The header is a button element with Collapsed or Expanded and no navigation hint; activation toggles")
    func accessibility() {
        let (row, _) = makeRow()
        let header = row.headerView
        #expect(!row.isAccessibilityElement)
        #expect(header.isAccessibilityElement)
        #expect(header.accessibilityLabel == "Kotlin, Apache License 2.0")
        #expect(header.accessibilityValue == "Collapsed")
        #expect(header.accessibilityHint == nil)
        #expect(header.accessibilityTraits.contains(.button))
        #expect(header.accessibilityActivate())
        #expect(row.isExpanded)
        #expect(header.accessibilityValue == "Expanded")
        row.accessibilityIdentifier = "licence.kotlin"
        #expect(header.accessibilityIdentifier == "licence.kotlin")
    }

    @Test("A disabled row does not toggle and dims")
    func disabled() {
        let (row, _) = makeRow(isExpanded: true)
        row.isEnabled = false
        row.sendActions(for: .touchUpInside)
        #expect(row.isExpanded)
        #expect(!row.headerView.accessibilityActivate())
        #expect(row.headerView.accessibilityTraits.contains(.notEnabled))
        #expect(abs(row.bodyContainer.alpha - 0.45) < 0.001)
    }

    @Test("The chevron turns down when expanded: 90° in LTR, -90° on the mirrored glyph in RTL")
    func chevron() {
        let (row, _) = makeRow()
        #expect(row.headerView.chevronView.transform == .identity)
        row.isExpanded = true
        let angle = atan2(row.headerView.chevronView.transform.b, row.headerView.chevronView.transform.a)
        #expect(abs(angle - .pi / 2) < 0.001)
        let rtl = TSDisclosureListItemView(title: "Kotlin", isExpanded: true)
        rtl.semanticContentAttribute = .forceRightToLeft
        rtl.isExpanded = false
        rtl.isExpanded = true
        let rtlAngle = atan2(rtl.headerView.chevronView.transform.b, rtl.headerView.chevronView.transform.a)
        #expect(abs(rtlAngle + .pi / 2) < 0.001)
    }

    @Test("The header is 68 with a subtitle; the body sits below with 16 side and 12 bottom padding")
    func geometry() {
        let (row, body) = makeRow(isExpanded: true)
        #expect(abs(row.headerView.frame.height - 68) < 1)
        let bodyFrame = body.convert(body.bounds, to: row)
        #expect(abs(bodyFrame.minX - 16) < 0.5)
        #expect(abs(bodyFrame.minY - row.headerView.frame.maxY) < 0.5)
        #expect(abs(row.bounds.maxY - bodyFrame.maxY - 12) < 0.5)
        #expect(row.hitTest(CGPoint(x: 20, y: 20), with: nil) === row)
        #expect(row.hitTest(CGPoint(x: 20, y: bodyFrame.midY), with: nil) !== row)
    }

    @Test("TSDividerView is 1 pt of border-subtle, full width, hidden from accessibility")
    func divider() {
        let divider = layout(TSDividerView())
        #expect(divider.intrinsicContentSize.height == TSListItemStyleSpec.group.dividerThickness)
        #expect(divider.frame.width == 390)
        #expect(!divider.isAccessibilityElement)
        let line = divider.subviews.first
        #expect(line?.frame == divider.bounds)
        #expect(line?.backgroundColor?.resolvedColor(with: divider.traitCollection) == TSListItemStyleSpec.group.divider.uiColor.resolvedColor(with: divider.traitCollection))
    }
}
