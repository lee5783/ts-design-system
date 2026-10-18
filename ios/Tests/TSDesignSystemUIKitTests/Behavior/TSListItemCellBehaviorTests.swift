import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSListItemCell behaviour")
struct TSListItemCellBehaviorTests {
    private func makeCell(width: CGFloat = 358) -> TSListItemCell {
        TSListItemCell(frame: CGRect(x: 0, y: 0, width: width, height: 68))
    }

    private func state(isHighlighted: Bool = false) -> UICellConfigurationState {
        var state = UICellConfigurationState(traitCollection: UITraitCollection(preferredContentSizeCategory: .large))
        state.isHighlighted = isHighlighted
        return state
    }

    @Test("The item becomes a TSListItemConfiguration content configuration and the cell is the accessibility element")
    func contentConfiguration() throws {
        let cell = makeCell()
        cell.item = TSListItemConfiguration(title: "Inbox", subtitle: "12 unread", showsChevron: true, isInteractive: true)
        cell.updateConfiguration(using: state())
        let configuration = try #require(cell.contentConfiguration as? TSListItemConfiguration)
        #expect(configuration.title == "Inbox")
        let content = try #require(cell.itemContentView)
        #expect(!content.isAccessibilityElement)
        #expect(cell.isAccessibilityElement)
    }

    @Test("A highlighted interactive cell draws surface-pressed; a highlighted static cell does not")
    func pressedOnlyWithAction() throws {
        let cell = makeCell()
        cell.item = TSListItemConfiguration(title: "Inbox", isInteractive: true)
        cell.isHighlighted = true
        cell.updateConfiguration(using: cell.configurationState)
        let content = try #require(cell.itemContentView)
        #expect(content.backgroundColor == TSColor.surfacePressed.uiColor.resolvedColor(with: content.traitCollection))
        cell.item = TSListItemConfiguration(title: "Favorites")
        cell.updateConfiguration(using: cell.configurationState)
        #expect(cell.itemContentView?.backgroundColor == .clear)
        cell.item = TSListItemConfiguration(title: "Wi-Fi", trailing: .toggle(isOn: true))
        cell.updateConfiguration(using: cell.configurationState)
        #expect(cell.itemContentView?.backgroundColor == .clear)
    }

    @Test("A selected cell is not drawn as pressed; only the highlight is")
    func selectedIsNotPressed() throws {
        let cell = makeCell()
        cell.item = TSListItemConfiguration(title: "Inbox", isInteractive: true)
        var selected = state()
        selected.isSelected = true
        cell.updateConfiguration(using: selected)
        let content = try #require(cell.itemContentView)
        #expect(content.backgroundColor == .clear)
        let configuration = try #require(cell.contentConfiguration as? TSListItemConfiguration)
        #expect(!configuration.isHighlighted)
        cell.isHighlighted = true
        cell.updateConfiguration(using: cell.configurationState)
        #expect(cell.itemContentView?.backgroundColor == TSColor.surfacePressed.uiColor.resolvedColor(with: content.traitCollection))
    }

    @Test("Reuse resets every slot, the pressed fill, the accessibility state and the group position")
    func reuseResetsState() throws {
        let cell = makeCell()
        cell.item = TSListItemConfiguration(
            title: "Wi-Fi",
            subtitle: "Home",
            leading: .avatar(name: "Ada Lovelace"),
            trailing: .toggle(isOn: true),
            isEnabled: false
        )
        cell.groupPosition = .last
        cell.isHighlighted = true
        cell.updateConfiguration(using: cell.configurationState)
        let first = try #require(cell.itemContentView)
        #expect(!first.switchSlot.isHidden)

        cell.prepareForReuse()
        cell.isHighlighted = false
        cell.item = TSListItemConfiguration(title: "Settings", leading: .icon(.settings), showsChevron: true, isInteractive: true)
        cell.updateConfiguration(using: state())
        let content = try #require(cell.itemContentView)
        #expect(content === first)
        #expect(content.switchSlot.isHidden)
        #expect(content.avatarView.isHidden)
        #expect(!content.tileView.isHidden)
        #expect(content.subtitleLabel.isHidden)
        #expect(content.trailingLabel.isHidden)
        #expect(content.badgeView.isHidden)
        #expect(!content.chevronView.isHidden)
        #expect(content.backgroundColor == .clear)
        #expect(content.alpha == 1)
        #expect(cell.groupPosition == .single)
    }

    @Test("A switch toggled inside a cell keeps the new value when the cell re-applies its state")
    func toggleSurvivesStateUpdates() throws {
        var values: [Bool] = []
        let cell = makeCell()
        cell.item = TSListItemConfiguration(title: "Wi-Fi", trailing: .toggle(isOn: false), onToggle: { values.append($0) })
        cell.updateConfiguration(using: state())
        let switchView = try #require(cell.itemContentView?.switchSlot.switchView)
        switchView.reduceMotion = { true }
        switchView.playHaptic = { _ in }
        switchView.userDidToggle()
        #expect(values == [true])
        #expect(cell.item?.trailing == .toggle(isOn: true))
        cell.updateConfiguration(using: state(isHighlighted: true))
        #expect(cell.itemContentView?.switchSlot.switchView.isOn == true)
    }

    @Test(
        "Group positions round the right corners and draw a top divider below the first row",
        arguments: [(0, 1, TSListGroupPosition.single), (0, 3, .first), (1, 3, .middle), (2, 3, .last)]
    )
    func groupPosition(index: Int, count: Int, expected: TSListGroupPosition) {
        let position = TSListGroupPosition(index: index, count: count)
        #expect(position == expected)
        #expect(position.showsTopDivider == (index > 0))
        let cell = makeCell()
        cell.groupPosition = position
        #expect(cell.layer.maskedCorners == position.roundedCorners)
        #expect(cell.borderView.position == position)
        cell.layoutIfNeeded()
        #expect((cell.borderView.dividerLayer.path != nil) == position.showsTopDivider)
    }

    @Test("In a TSListLayout collection view cells learn their group position and headers use the section title")
    func collectionView() throws {
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        let collectionView = UICollectionView(frame: window.bounds, collectionViewLayout: TSListLayout.makeLayout(hasHeaders: true))
        let registration = UICollectionView.CellRegistration<TSListItemCell, String> { cell, _, title in
            cell.item = TSListItemConfiguration(title: title, showsChevron: true, isInteractive: true)
        }
        let headerRegistration = UICollectionView.SupplementaryRegistration<TSListSectionHeaderView>(
            elementKind: UICollectionView.elementKindSectionHeader
        ) { header, _, _ in
            header.title = "Section"
        }
        let dataSource = UICollectionViewDiffableDataSource<Int, String>(collectionView: collectionView) { collectionView, indexPath, title in
            collectionView.dequeueConfiguredReusableCell(using: registration, for: indexPath, item: title)
        }
        dataSource.supplementaryViewProvider = { collectionView, _, indexPath in
            collectionView.dequeueConfiguredReusableSupplementary(using: headerRegistration, for: indexPath)
        }
        var snapshot = NSDiffableDataSourceSnapshot<Int, String>()
        snapshot.appendSections([0])
        snapshot.appendItems(["One", "Two", "Three"])
        dataSource.apply(snapshot, animatingDifferences: false)
        window.addSubview(collectionView)
        window.isHidden = false
        collectionView.layoutIfNeeded()

        let cells = (0..<3).compactMap { collectionView.cellForItem(at: IndexPath(item: $0, section: 0)) as? TSListItemCell }
        #expect(cells.map(\.groupPosition) == [.first, .middle, .last])
        #expect(cells.allSatisfy { abs($0.bounds.height - 48) < 0.5 })
        #expect(cells.allSatisfy { abs($0.frame.minX - 16) < 0.5 && abs($0.frame.width - 358) < 0.5 })
        let header = try #require(collectionView.supplementaryView(
            forElementKind: UICollectionView.elementKindSectionHeader,
            at: IndexPath(item: 0, section: 0)
        ) as? TSListSectionHeaderView)
        #expect(header.frame.maxY <= cells[0].frame.minY)
        _ = dataSource
    }
}
