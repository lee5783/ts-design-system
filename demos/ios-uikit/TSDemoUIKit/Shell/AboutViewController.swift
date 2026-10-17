import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class AboutViewController: UIViewController {
    private struct Row: Hashable {
        let title: String
        let value: String
        let identifier: String
    }

    private let buildInfo = DemoBuildInfo.load(from: .main)
    private lazy var collectionView = UICollectionView(
        frame: .zero,
        collectionViewLayout: UICollectionViewCompositionalLayout.list(using: UICollectionLayoutListConfiguration(appearance: .insetGrouped))
    )
    private var dataSource: UICollectionViewDiffableDataSource<Int, Row>?

    override func viewDidLoad() {
        super.viewDidLoad()
        title = DemoStrings.string(.aboutTitle)
        navigationItem.largeTitleDisplayMode = .never
        collectionView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(collectionView)
        NSLayoutConstraint.activate([
            collectionView.topAnchor.constraint(equalTo: view.topAnchor),
            collectionView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            collectionView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            collectionView.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])
        configureDataSource()
    }

    private func configureDataSource() {
        let registration = UICollectionView.CellRegistration<UICollectionViewListCell, Row> { cell, _, row in
            var content = UIListContentConfiguration.valueCell()
            content.text = row.title
            content.textProperties.font = TSFont.body.uiFont()
            content.textProperties.color = TSColor.textPrimary.uiColor
            content.textProperties.adjustsFontForContentSizeCategory = true
            content.secondaryText = row.value
            content.secondaryTextProperties.font = TSFont.code.uiFont()
            content.secondaryTextProperties.color = TSColor.textSecondary.uiColor
            content.secondaryTextProperties.adjustsFontForContentSizeCategory = true
            cell.contentConfiguration = content
            cell.isAccessibilityElement = true
            cell.accessibilityLabel = "\(row.title), \(row.value)"
            cell.accessibilityIdentifier = row.identifier
        }
        let dataSource = UICollectionViewDiffableDataSource<Int, Row>(collectionView: collectionView) { collectionView, indexPath, row in
            collectionView.dequeueConfiguredReusableCell(using: registration, for: indexPath, item: row)
        }
        var snapshot = NSDiffableDataSourceSnapshot<Int, Row>()
        snapshot.appendSections([0])
        snapshot.appendItems([
            Row(
                title: DemoStrings.string(.aboutLibraryVersion),
                value: buildInfo.libraryVersion ?? DemoStrings.string(.aboutUnknown),
                identifier: "demo.about.library-version"
            ),
            Row(
                title: DemoStrings.string(.aboutDesignSystemVersion),
                value: buildInfo.designSystemVersion ?? DemoStrings.string(.aboutUnknown),
                identifier: "demo.about.design-system-version"
            )
        ])
        dataSource.apply(snapshot, animatingDifferences: false)
        self.dataSource = dataSource
    }
}

