import Testing
import TSDesignSystemUIKit
import UIKit

@MainActor
@Suite("TSBadgeView behaviour")
struct TSBadgeViewBehaviorTests {
    @Test("A badge is a plain view, not a control", arguments: TSBadgeTone.allCases)
    func isNotAControl(tone: TSBadgeTone) {
        let badge = TSBadgeView(text: "Failed", tone: tone, showsDot: true, icon: .xCircle)
        #expect(!(badge as UIView is UIControl))
    }

    @Test("Tone colours resolve from the spec", arguments: TSBadgeTone.allCases)
    func colors(tone: TSBadgeTone) {
        let badge = TSBadgeView(text: "Status", tone: tone)
        let resolved = TSBadgeStyleSpec.resolve(.init(tone: tone))
        let traits = badge.traitCollection
        #expect(badge.backgroundColor == resolved.fill.uiColor.resolvedColor(with: traits))
        #expect(badge.layer.borderWidth == resolved.borderWidth)
        #expect(badge.layer.cornerRadius == resolved.cornerRadius)
    }

    @Test("The dot and icon appear only when set, and updates re-apply")
    func adornments() {
        let badge = TSBadgeView(text: "Draft")
        let hostedSubviews = badge.subviews.flatMap(\.subviews)
        #expect(hostedSubviews.filter { !$0.isHidden }.count == 1)
        badge.showsDot = true
        badge.icon = .checkCircle
        badge.text = "Active"
        #expect(hostedSubviews.filter { !$0.isHidden }.count == 3)
        #expect(hostedSubviews.compactMap { $0 as? TSIconView }.first?.name == .checkCircle)
    }

    @Test("The height is at least 22 and grows with the text at AX5")
    func height() {
        let regular = TSBadgeView(text: "New")
        let large = TSBadgeView(text: "New")
        let regularHost = TraitHost(sizeCategory: .large)
        let largeHost = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        [(regular, regularHost), (large, largeHost)].forEach { badge, host in
            badge.translatesAutoresizingMaskIntoConstraints = false
            host.content.addSubview(badge)
            host.window.layoutIfNeeded()
        }
        #expect(regular.bounds.height == 22)
        #expect(large.bounds.height > 22)
        #expect(large.bounds.width > regular.bounds.width)
    }
}
