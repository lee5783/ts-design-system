import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class ElevationViewController: DemoPageViewController {
    init() {
        super.init(page: .elevation)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        let cards = TSShadowTokens.allTokens.map { token in
            let detail = "\(DemoStrings.string(.lightAppearance)) \(token.light.count) · \(DemoStrings.string(.darkAppearance)) \(token.dark.count)"
            let labels = UIStackView(arrangedSubviews: [
                DemoLabel(token.name, font: .label, color: .textPrimary),
                DemoLabel(detail, font: .code, color: .textMuted)
            ])
            labels.axis = .vertical
            labels.spacing = TSSpacing.space2
            let card = ShadowCardView(token: token, content: labels)
            card.isAccessibilityElement = true
            card.accessibilityLabel = "\(token.name), \(detail)"
            card.accessibilityIdentifier = "demo.token.\(token.name)"
            return card
        }
        let grid = DemoGridView(items: cards, minimumItemWidth: 140, columnSpacing: TSSpacing.space6, rowSpacing: TSSpacing.space6)
        let container = UIStackView(arrangedSubviews: [grid])
        container.isLayoutMarginsRelativeArrangement = true
        container.directionalLayoutMargins = NSDirectionalEdgeInsets(top: TSSpacing.space2, leading: 0, bottom: TSSpacing.space2, trailing: 0)
        return [container]
    }
}
