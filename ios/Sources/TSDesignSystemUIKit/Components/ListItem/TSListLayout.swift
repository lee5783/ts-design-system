import UIKit

public enum TSListLayout {
    @MainActor
    public static func section(
        layoutEnvironment: NSCollectionLayoutEnvironment,
        hasHeader: Bool = false,
        horizontalInset: CGFloat = TSSpacing.space4
    ) -> NSCollectionLayoutSection {
        let group = TSListItemStyleSpec.group
        var configuration = UICollectionLayoutListConfiguration(appearance: .plain)
        configuration.showsSeparators = false
        configuration.backgroundColor = .clear
        configuration.headerMode = hasHeader ? .supplementary : .none
        configuration.headerTopPadding = 0
        let section = NSCollectionLayoutSection.list(using: configuration, layoutEnvironment: layoutEnvironment)
        section.contentInsets = NSDirectionalEdgeInsets(top: 0, leading: horizontalInset, bottom: group.sectionSpacing, trailing: horizontalInset)
        section.boundarySupplementaryItems.forEach { $0.pinToVisibleBounds = false }
        section.supplementariesFollowContentInsets = true
        return section
    }

    @MainActor
    public static func makeLayout(hasHeaders: Bool = false, horizontalInset: CGFloat = TSSpacing.space4) -> UICollectionViewCompositionalLayout {
        UICollectionViewCompositionalLayout { _, layoutEnvironment in
            section(layoutEnvironment: layoutEnvironment, hasHeader: hasHeaders, horizontalInset: horizontalInset)
        }
    }
}
