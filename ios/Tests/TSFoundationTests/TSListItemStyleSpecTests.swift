import CoreGraphics
import Testing
import TSFoundation
import TSTokens

@Suite("TSListItemStyleSpec")
struct TSListItemStyleSpecTests {
    @Test("The matrix covers subtitle × interactive × layout × 4 states once each")
    func matrixIsComplete() {
        let configurations = TSListItemStyleSpec.allConfigurations
        #expect(configurations.count == 32)
        #expect(Set(configurations).count == 32)
    }

    @Test("Pressed and focused fills apply only to a row with an action", arguments: TSListItemStyleSpec.allConfigurations)
    func background(configuration: TSListItemStyleSpec.Configuration) {
        let resolved = TSListItemStyleSpec.resolve(configuration)
        let expected: TSColor? = switch (configuration.isInteractive, configuration.state) {
        case (true, .pressed): .surfacePressed
        case (true, .focused): .surfaceHover
        default: nil
        }
        #expect(resolved.background == expected)
        #expect(resolved.opacity == (configuration.state == .disabled ? 0.45 : 1))
    }

    @Test("Text, slots and padding map to the DS tokens", arguments: TSListItemStyleSpec.allConfigurations)
    func tokens(configuration: TSListItemStyleSpec.Configuration) {
        let resolved = TSListItemStyleSpec.resolve(configuration)
        #expect(resolved.titleFont == .body)
        #expect(resolved.titleColor == .textPrimary)
        #expect(resolved.subtitleFont == .bodySm)
        #expect(resolved.subtitleColor == .textMuted)
        #expect(resolved.showsSubtitle == configuration.hasSubtitle)
        #expect(resolved.leadingColor == .textSecondary)
        #expect(resolved.trailingFont == .bodySm)
        #expect(resolved.trailingColor == .textMuted)
        #expect(resolved.trailingSpacing == 8)
        #expect(resolved.chevron == .chevronRight)
        #expect(resolved.chevronSize == .md)
        #expect(resolved.chevronColor == .textMuted)
        #expect(resolved.horizontalPadding == 16)
        #expect(resolved.verticalPadding == 12)
        #expect(resolved.spacing == 12)
        #expect(resolved.minHeight == 44)
    }

    @Test("Horizontal rows truncate to one line; stacked rows wrap", arguments: TSListItemLayout.allCases)
    func layout(layout: TSListItemLayout) {
        let resolved = TSListItemStyleSpec.resolve(.init(hasSubtitle: true, layout: layout))
        let isStacked = layout == .stacked
        #expect(resolved.isStacked == isStacked)
        #expect(resolved.titleLineLimit == (isStacked ? nil : 1))
        #expect(resolved.subtitleLineLimit == (isStacked ? nil : 1))
        #expect(resolved.stackedSpacing == 8)
    }

    @Test("Accessibility text sizes reflow to the stacked layout")
    func reflow() {
        #expect(TSListItemLayout.make(isAccessibilitySize: false) == .horizontal)
        #expect(TSListItemLayout.make(isAccessibilitySize: true) == .stacked)
    }

    @Test("Default heights are 48 with a title and 68 with a subtitle")
    func heights() {
        let resolved = TSListItemStyleSpec.resolve(.init())
        let title = TSFont.body.token.lineHeight
        let subtitle = TSFont.bodySm.token.lineHeight
        #expect(resolved.verticalPadding * 2 + title == 48)
        #expect(resolved.verticalPadding * 2 + title + subtitle == 68)
    }

    @Test("The icon tile is 36 pt, radius-md, accent-subtle with an accent-text 20 pt icon")
    func tile() {
        let tile = TSListItemStyleSpec.tile
        #expect(tile.size == 36)
        #expect(tile.cornerRadius == TSRadius.radiusMd)
        #expect(tile.fill == .accentSubtle)
        #expect(tile.foreground == .accentText)
        #expect(tile.iconSize.pointSize == 20)
    }

    @Test("The group is a surface-card card with a border-subtle border, radius-lg and full-width dividers")
    func group() {
        let group = TSListItemStyleSpec.group
        #expect(group.fill == .surfaceCard)
        #expect(group.border == .borderSubtle)
        #expect(group.borderWidth == 1)
        #expect(group.cornerRadius == TSRadius.radiusLg)
        #expect(group.divider == .borderSubtle)
        #expect(group.dividerThickness == 1)
        #expect(group.dividerInset == 0)
        #expect(group.headerFont == .overline)
        #expect(group.headerColor == .textMuted)
    }

    @Test("The disclosure chevron turns down over duration-base, mirrored in RTL")
    func disclosure() {
        let disclosure = TSListItemStyleSpec.disclosure
        #expect(disclosure.rotationDegrees(isExpanded: false, isRightToLeft: false) == 0)
        #expect(disclosure.rotationDegrees(isExpanded: false, isRightToLeft: true) == 0)
        #expect(disclosure.rotationDegrees(isExpanded: true, isRightToLeft: false) == 90)
        #expect(disclosure.rotationDegrees(isExpanded: true, isRightToLeft: true) == -90)
        #expect(disclosure.animationDuration == TSMotion.durationBase)
        #expect(disclosure.animationEasing == TSMotion.easingStandard)
        #expect(disclosure.bodyHorizontalPadding == TSSpacing.space4)
        #expect(disclosure.bodyBottomPadding == TSSpacing.space3)
    }
}

@Suite("TSListItemAccessibility")
struct TSListItemAccessibilityTests {
    @Test(
        "The role comes from the action, the chevron and a trailing switch",
        arguments: [
            (false, false, false, TSListItemRole.plain),
            (false, true, false, .plain),
            (true, false, false, .action),
            (true, true, false, .navigation),
            (true, true, true, .toggle),
            (false, false, true, .toggle)
        ]
    )
    func role(hasAction: Bool, showsChevron: Bool, hasToggle: Bool, expected: TSListItemRole) {
        let role = TSListItemRole.make(hasAction: hasAction, showsChevron: showsChevron, hasToggle: hasToggle)
        #expect(role == expected)
        #expect(role.isPressable == (expected == .action || expected == .navigation))
    }

    @Test("The label joins the title and subtitle; only navigation rows get a hint")
    func labelAndHint() {
        #expect(TSListItemAccessibility.label(title: "Inbox", subtitle: "12 unread") == "Inbox, 12 unread")
        #expect(TSListItemAccessibility.label(title: "Inbox", subtitle: nil) == "Inbox")
        #expect(TSListItemAccessibility.label(title: "Inbox", subtitle: "") == "Inbox")
        #expect(TSListItemAccessibility.hint(for: .navigation) == "Opens another screen")
        #expect(TSListItemAccessibility.hint(for: .action) == nil)
        #expect(TSListItemAccessibility.hint(for: .toggle) == nil)
        #expect(TSListItemAccessibility.hint(for: .plain) == nil)
    }

    @Test("A disclosure row reports Expanded or Collapsed")
    func expansionValue() {
        #expect(TSListItemAccessibility.expansionValue(isExpanded: true) == "Expanded")
        #expect(TSListItemAccessibility.expansionValue(isExpanded: false) == "Collapsed")
    }
}
