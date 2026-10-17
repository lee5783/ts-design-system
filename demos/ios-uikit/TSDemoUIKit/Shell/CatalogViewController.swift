import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class CatalogViewController: UIViewController {
    private let settings: DemoSettingsModel
    private lazy var collectionView = UICollectionView(frame: .zero, collectionViewLayout: Self.makeLayout())
    private var dataSource: UICollectionViewDiffableDataSource<DemoSection.ID, DemoPageID>?

    init(settings: DemoSettingsModel) {
        self.settings = settings
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = DemoStrings.string(.appTitle)
        navigationItem.largeTitleDisplayMode = .always
        navigationItem.leftBarButtonItem = DemoIcon.barButtonItem(
            .info,
            label: DemoStrings.string(.aboutTitle),
            identifier: "demo.about",
            action: UIAction { [weak self] _ in self?.showAbout() }
        )
        navigationItem.rightBarButtonItems = [
            DemoIcon.barButtonItem(
                .settings,
                label: DemoStrings.string(.settingsTitle),
                identifier: "demo.settings",
                action: UIAction { [weak self] _ in self?.showSettings() }
            ),
            DemoThemeBarButtonItem(settings: settings)
        ]
        view.backgroundColor = TSColor.backgroundPrimary.uiColor
        collectionView.backgroundColor = TSColor.backgroundPrimary.uiColor
        collectionView.contentInset.top = TSSpacing.space4
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        collectionView.delegate = self
        view.addSubview(collectionView)
        NSLayoutConstraint.activate([
            collectionView.topAnchor.constraint(equalTo: view.topAnchor),
            collectionView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            collectionView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            collectionView.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])
        configureDataSource()
    }

    private static func makeLayout() -> UICollectionViewLayout {
        TSListLayout.makeLayout(hasHeaders: true)
    }

    private func configureDataSource() {
        let cellRegistration = UICollectionView.CellRegistration<TSListItemCell, DemoPageID> { cell, _, page in
            cell.item = TSListItemConfiguration(title: page.title, showsChevron: true, isInteractive: true)
            cell.accessibilityIdentifier = "demo.catalog.\(page.rawValue)"
        }
        let headerRegistration = UICollectionView.SupplementaryRegistration<TSListSectionHeaderView>(
            elementKind: UICollectionView.elementKindSectionHeader
        ) { [weak self] header, _, indexPath in
            guard let section = self?.dataSource?.sectionIdentifier(for: indexPath.section) else { return }
            header.title = DemoStrings.sectionTitle(section)
        }
        let dataSource = UICollectionViewDiffableDataSource<DemoSection.ID, DemoPageID>(collectionView: collectionView) { collectionView, indexPath, page in
            collectionView.dequeueConfiguredReusableCell(using: cellRegistration, for: indexPath, item: page)
        }
        dataSource.supplementaryViewProvider = { collectionView, _, indexPath in
            collectionView.dequeueConfiguredReusableSupplementary(using: headerRegistration, for: indexPath)
        }
        var snapshot = NSDiffableDataSourceSnapshot<DemoSection.ID, DemoPageID>()
        for section in DemoCatalog.sections {
            snapshot.appendSections([section.id])
            snapshot.appendItems(section.pages, toSection: section.id)
        }
        dataSource.apply(snapshot, animatingDifferences: false)
        self.dataSource = dataSource
    }

    private func showAbout() {
        navigationController?.pushViewController(AboutViewController(), animated: !settings.reduceMotion)
    }

    private func showSettings() {
        let settingsNavigation = UINavigationController(rootViewController: DemoSettingsViewController(settings: settings))
        let container = DemoSettingsContainerViewController(settings: settings, content: settingsNavigation)
        present(container, animated: !settings.reduceMotion)
    }
}

extension CatalogViewController: UICollectionViewDelegate {
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        guard let page = dataSource?.itemIdentifier(for: indexPath) else { return }
        navigationController?.pushViewController(DemoPageFactory.makeViewController(for: page), animated: !settings.reduceMotion)
    }
}
