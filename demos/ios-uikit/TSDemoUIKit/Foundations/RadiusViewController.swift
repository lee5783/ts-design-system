import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class RadiusViewController: DemoPageViewController {
    private let sampleHeight = TSSize.controlLg

    init() {
        super.init(page: .radius)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        TSRadius.allTokens.map { token in
            TokenRowView(
                name: token.name,
                detail: token.isCapsule ? DemoStrings.string(.capsule) : TokenFormat.points(token.value),
                visual: SwatchView(
                    fill: .accentSubtle,
                    stroke: .accentPrimary,
                    lineWidth: 1.5,
                    cornerRadius: token.cornerRadius(forHeight: sampleHeight),
                    size: CGSize(width: TSSpacing.space16 + TSSpacing.space6, height: sampleHeight)
                )
            )
        }
    }
}
