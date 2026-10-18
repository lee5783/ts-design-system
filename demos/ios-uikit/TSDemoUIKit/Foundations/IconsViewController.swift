import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class IconsViewController: DemoPageViewController {
    init() {
        super.init(page: .icons)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [sizesSection(), labelledSection(), gridView()]
    }

    private func sizesSection() -> UIView {
        DemoComponentSectionView(.sizes, views: TSIconSize.allCases.map { size in
            let icons: [UIView] = [TSIconName.home, .chevronRight, .bell].map { name in
                let icon = TSIconView(name, size: size)
                icon.tintColor = TSColor.textPrimary.uiColor
                return icon
            }
            let row = UIStackView(arrangedSubviews: icons + [DemoPlayground.caption("\(size.rawValue) · \(Int(size.pointSize)) pt")])
            row.axis = .horizontal
            row.alignment = .center
            row.spacing = TSSpacing.space3
            return row
        })
    }

    private func labelledSection() -> UIView {
        let status: [(TSIconName, DemoStringKey, TSColor)] = [
            (.checkCircle, .sampleSuccess, .success),
            (.alert, .sampleWarning, .warning),
            (.xCircle, .sampleError, .danger),
            (.info, .sampleInformation, .info)
        ]
        let row = UIStackView(arrangedSubviews: status.map { name, _, color in
            let icon = TSIconView(name, size: .lg)
            icon.tintColor = color.uiColor
            return icon
        })
        row.axis = .horizontal
        row.spacing = TSSpacing.space3
        return DemoComponentSectionView(.labelled, views: [row])
    }

    private func gridView() -> UIView {
        let tiles = TSIconName.allCases.map { icon in
            var views: [UIView] = [
                iconView(icon),
                DemoLabel(icon.assetName, font: .caption, color: .textSecondary, alignment: .center)
            ]
            if icon.flipsForRightToLeftLayoutDirection {
                views.append(DemoLabel(DemoStrings.string(.mirrorsInRightToLeft), font: .overline, color: .accentText, alignment: .center))
            }
            let content = UIStackView(arrangedSubviews: views)
            content.axis = .vertical
            content.alignment = .center
            content.spacing = TSSpacing.space2
            content.translatesAutoresizingMaskIntoConstraints = false
            let tile = UIView()
            tile.addSubview(content)
            let hugging = tile.heightAnchor.constraint(equalToConstant: 0)
            hugging.priority = .fittingSizeLevel
            NSLayoutConstraint.activate([
                hugging,
                content.topAnchor.constraint(equalTo: tile.topAnchor),
                content.leadingAnchor.constraint(equalTo: tile.leadingAnchor),
                content.trailingAnchor.constraint(equalTo: tile.trailingAnchor),
                content.bottomAnchor.constraint(lessThanOrEqualTo: tile.bottomAnchor),
                tile.heightAnchor.constraint(greaterThanOrEqualToConstant: TSSpacing.space16)
            ])
            tile.isAccessibilityElement = true
            tile.accessibilityLabel = views.compactMap { ($0 as? DemoLabel)?.string }.joined(separator: ", ")
            tile.accessibilityIdentifier = "demo.token.\(icon.assetName)"
            return tile
        }
        return DemoGridView(items: tiles, minimumItemWidth: 88, columnSpacing: TSSpacing.space3, rowSpacing: TSSpacing.space4)
    }

    private func iconView(_ name: TSIconName) -> UIView {
        let icon = TSIconView(name, size: .lg)
        icon.tintColor = TSColor.textPrimary.uiColor
        return icon
    }
}
