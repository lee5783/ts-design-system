import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class BottomNavViewController: DemoPageViewController {
    private var changeCount = 0
    private var itemCount = 4
    private var badge = 3
    private lazy var counter = DemoPlayground.counter(identifier: "demo.bottom-nav.playground.changes")
    private lazy var valueCaption: DemoLabel = {
        let caption = DemoPlayground.caption(BottomNavSample.label(for: BottomNavSample.five[0].id))
        caption.accessibilityIdentifier = "demo.bottom-nav.playground.value"
        return caption
    }()
    private lazy var playgroundNav: TSBottomNavView = {
        let nav = Self.bar(BottomNavSample.items(count: itemCount, inboxBadge: badge))
        nav.accessibilityIdentifier = "demo.bottom-nav.playground.nav"
        nav.addAction(UIAction { [weak self] _ in self?.navDidChange() }, for: .valueChanged)
        return nav
    }()

    init() {
        super.init(page: .bottomNav)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [variantsSection(), statesSection(), shellSection(), playgroundSection()]
    }

    private func variantsSection() -> UIView {
        let section = DemoComponentSectionView(.variants, views: [])
        section.addArrangedSubview(DemoPlayground.caption(DemoStrings.string(.threeItems)))
        section.addFullWidth(Self.bar(BottomNavSample.three))
        section.addArrangedSubview(DemoPlayground.caption(DemoStrings.string(.fourItemsWithBadges)))
        section.addFullWidth(Self.bar(BottomNavSample.four, selectedID: BottomNavSample.four[1].id))
        section.addArrangedSubview(DemoPlayground.caption(DemoStrings.string(.fiveItems)))
        section.addFullWidth(Self.bar(BottomNavSample.five))
        return section
    }

    private func statesSection() -> UIView {
        let section = DemoComponentSectionView(.states, views: [])
        let disabled = Self.bar(BottomNavSample.four)
        disabled.isEnabled = false
        section.addArrangedSubview(DemoPlayground.caption(DemoStrings.string(.stateDisabled)))
        section.addFullWidth(disabled)
        return section
    }

    private func shellSection() -> UIView {
        let open = TSButtonView(
            title: DemoStrings.string(.openShell),
            variant: .secondary,
            primaryAction: UIAction { [weak self] _ in self?.openShell() }
        )
        open.accessibilityIdentifier = "demo.bottom-nav.open-shell"
        return DemoComponentSectionView(.shell, views: [open, DemoPlayground.caption(DemoStrings.string(.shellCaption))])
    }

    private func playgroundSection() -> UIView {
        let nav = playgroundNav
        let counts = BottomNavSample.counts
        let countPicker = DemoPlayground.segmented(counts.map { "\($0)" }, identifier: "demo.bottom-nav.playground.count") { [weak self] in
            self?.itemCount = counts[$0]
            self?.updatePlaygroundItems()
        }
        countPicker.selectedSegmentIndex = counts.firstIndex(of: itemCount) ?? 0
        let addOne = button(.sampleNewMessage, identifier: "demo.bottom-nav.playground.add-one") { [weak self] in
            self?.badge += 1
            self?.updatePlaygroundItems()
        }
        let addHundred = button(.sampleAddHundred, identifier: "demo.bottom-nav.playground.add-hundred") { [weak self] in
            self?.badge += 100
            self?.updatePlaygroundItems()
        }
        let clear = button(.sampleMarkAllRead, variant: .ghost, identifier: "demo.bottom-nav.playground.clear") { [weak self] in
            self?.badge = 0
            self?.updatePlaygroundItems()
        }
        let section = DemoComponentSectionView(.playground, views: [])
        [
            countPicker,
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.bottom-nav.playground.disabled") { nav.isEnabled = !$0 }
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(DemoComponentRowView([addOne, addHundred]))
        section.addArrangedSubview(clear)
        section.addFullWidth(nav)
        section.addArrangedSubview(valueCaption)
        section.addArrangedSubview(counter)
        return section
    }

    private func button(
        _ key: DemoStringKey,
        variant: TSButtonVariant = .secondary,
        identifier: String,
        action: @escaping () -> Void
    ) -> TSButtonView {
        let button = TSButtonView(title: DemoStrings.string(key), variant: variant, primaryAction: UIAction { _ in action() })
        button.accessibilityIdentifier = identifier
        return button
    }

    private func updatePlaygroundItems() {
        playgroundNav.items = BottomNavSample.items(count: itemCount, inboxBadge: badge)
        valueCaption.string = BottomNavSample.label(for: playgroundNav.selectedID)
    }

    private func navDidChange() {
        changeCount += 1
        counter.string = DemoStrings.tapCount(changeCount)
        valueCaption.string = BottomNavSample.label(for: playgroundNav.selectedID)
    }

    private func openShell() {
        let settings = DemoSettingsContainerViewController.settings(for: self)
            ?? DemoSettingsModel(launchOptions: DemoLaunchOptions(arguments: ProcessInfo.processInfo.arguments))
        let container = DemoSettingsContainerViewController(settings: settings, content: BottomNavShell.make(settings: settings))
        container.modalPresentationStyle = .fullScreen
        present(container, animated: true)
    }

    private static func bar(_ items: [TSBottomNavItem], selectedID: String? = nil) -> TSBottomNavView {
        TSBottomNavView(items: items, selectedID: selectedID, label: DemoStrings.string(.sampleMainNavigation))
    }
}

enum BottomNavSample {
    static let counts = [3, 4, 5]
    static let three = items(count: 3, inboxBadge: 0)
    static let four = items(count: 4, inboxBadge: 3, alertsBadge: 120)
    static let five = items(count: 5, inboxBadge: 3, alertsBadge: 120)

    static func items(count: Int, inboxBadge: Int, alertsBadge: Int? = nil) -> [TSBottomNavItem] {
        let all = [
            TSBottomNavItem(id: DemoStringKey.sampleHome.rawValue, label: DemoStrings.string(.sampleHome), icon: .home),
            TSBottomNavItem(id: DemoStringKey.sampleInbox.rawValue, label: DemoStrings.string(.sampleInbox), icon: .mail, badge: inboxBadge),
            TSBottomNavItem(id: DemoStringKey.sampleSearch.rawValue, label: DemoStrings.string(.sampleSearch), icon: .search),
            TSBottomNavItem(id: DemoStringKey.sampleAlerts.rawValue, label: DemoStrings.string(.sampleAlerts), icon: .bell, badge: alertsBadge),
            TSBottomNavItem(id: DemoStringKey.sampleProfile.rawValue, label: DemoStrings.string(.sampleProfile), icon: .user)
        ]
        switch count {
        case 3:
            return [all[0], all[1], all[4]]
        case 4:
            return Array(all.prefix(4))
        default:
            return all
        }
    }

    static func label(for id: String) -> String {
        let label = items(count: 5, inboxBadge: 0).first { $0.id == id }?.label ?? id
        return "\(DemoStrings.string(.selected)): \(label)"
    }
}
