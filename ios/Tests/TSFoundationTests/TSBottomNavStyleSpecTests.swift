import Foundation
import Testing
import UIKit
@testable import TSFoundation
import TSTokens

@Suite("TSBottomNavStyleSpec")
struct TSBottomNavStyleSpecTests {
    private let english = Locale(identifier: "en_US")

    @Test("The matrix covers unselected/selected × 4 states once each")
    func matrixIsComplete() {
        let configurations = TSBottomNavStyleSpec.allConfigurations
        #expect(configurations.count == 8)
        #expect(Set(configurations).count == 8)
    }

    @Test("Every configuration resolves foreground, pill, opacity and focus", arguments: TSBottomNavStyleSpec.allConfigurations)
    func colors(configuration: TSBottomNavStyleSpec.Configuration) {
        let resolved = TSBottomNavStyleSpec.resolve(configuration)
        let state = configuration.state
        let expected: (foreground: TSColor, pill: TSColor?) =
            switch (configuration.isSelected, state) {
            case (true, _): (.accentText, .accentSubtle)
            case (false, .pressed): (.textMuted, .surfacePressed)
            case (false, _): (.textMuted, nil)
            }
        #expect(resolved.foreground == expected.foreground)
        #expect(resolved.pillFill == expected.pill)
        #expect(resolved.labelFont == TSFont.caption.weighted(500))
        #expect(resolved.iconSize == .lg)
        #expect(resolved.opacity == (state == .disabled ? 0.45 : 1))
        #expect(resolved.showsFocusRing == (state == .focused))
    }

    @Test("A disabled item ignores pressed")
    func disabledIgnoresPressed() {
        let resolved = TSBottomNavStyleSpec.resolve(.init(state: [.disabled, .pressed]))
        #expect(resolved.pillFill == nil)
        #expect(resolved.opacity == 0.45)
    }

    @Test("Bar 64 with a surface-card fill and a 1 pt border-subtle top; pill 56×28 capsule; label 2 pt below")
    func geometry() {
        let container = TSBottomNavStyleSpec.container
        #expect(container.height == 64)
        #expect(container.fill == .surfaceCard)
        #expect(container.border == .borderSubtle)
        #expect(container.borderWidth == 1)
        let resolved = TSBottomNavStyleSpec.resolve(.init(isSelected: true))
        #expect(resolved.pillWidth == 56)
        #expect(resolved.pillHeight == 28)
        #expect(resolved.pillCornerRadius == 14)
        #expect(resolved.spacing == 2)
        #expect(TSBottomNavStyleSpec.itemCountRange == 3...5)
    }

    @Test("Badge: danger-solid, 10/600 text-on-accent, 16 high and at least 16 wide, 6 from the top and 6 past the centre")
    func badge() {
        let badge = TSBottomNavStyleSpec.badge
        #expect(badge.fill == .dangerSolid)
        #expect(badge.foreground == .textOnAccent)
        #expect(badge.fontSize == 10)
        #expect(badge.fontWeight == 600)
        #expect(badge.height == 16)
        #expect(badge.minWidth == 16)
        #expect(badge.horizontalPadding == 4)
        #expect(badge.cornerRadius == 8)
        #expect(badge.offsetTop == 6)
        #expect(badge.offsetStart == 6)
        #expect(badge.topFromPill == -3)
        #expect(TSBottomNavStyleSpec.badgeFont().pointSize == 10)
    }

    @Test(
        "Labels follow Dynamic Type up to xxxLarge and are clamped above it",
        arguments: [
            (UIContentSizeCategory.extraSmall, UIContentSizeCategory.extraSmall),
            (.large, .large),
            (.extraExtraExtraLarge, .extraExtraExtraLarge),
            (.accessibilityMedium, .extraExtraExtraLarge),
            (.accessibilityExtraExtraExtraLarge, .extraExtraExtraLarge),
            (.unspecified, .large),
        ]
    )
    func labelClamp(category: UIContentSizeCategory, expected: UIContentSizeCategory) {
        #expect(TSBottomNavStyleSpec.labelSizeCategory(for: category) == expected)
    }

    @Test("The clamped label still fits the 64 pt bar under the pill")
    func clampedLabelFits() {
        let resolved = TSBottomNavStyleSpec.resolve(.init())
        let category = TSBottomNavStyleSpec.labelSizeCategory(for: .accessibilityExtraExtraExtraLarge)
        let content = resolved.pillHeight + resolved.spacing + resolved.labelFont.lineHeight(for: category)
        #expect(content <= TSBottomNavStyleSpec.container.height)
    }

    @Test("Badge text: hidden for nil and ≤ 0, the count, then 99+", arguments: [
        (Int?.none, String?.none),
        (0, nil),
        (-2, nil),
        (3, "3"),
        (99, "99"),
        (100, "99+"),
    ])
    func badgeText(badge: Int?, expected: String?) {
        let item = TSBottomNavItem(id: "inbox", label: "Inbox", icon: .bell, badge: badge)
        #expect(item.badgeText == expected)
        #expect(item.badged(nil).badgeText == nil)
        #expect(item.badged(5).badgeText == "5")
    }

    @Test(
        "The accessibility value is \"Tab, n of N\" plus the badge",
        arguments: [
            (1, 3, Int?.none, "Tab, 2 of 3"),
            (1, 4, 0, "Tab, 2 of 4"),
            (0, 4, 3, "Tab, 1 of 4, 3 new"),
            (4, 5, 250, "Tab, 5 of 5, 99+ new"),
        ]
    )
    func accessibilityValue(index: Int, count: Int, badge: Int?, expected: String) {
        #expect(TSBottomNavAccessibility.value(index: index, count: count, badge: badge, locale: english) == expected)
    }
}
