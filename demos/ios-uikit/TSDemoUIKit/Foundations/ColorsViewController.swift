import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class ColorsViewController: DemoPageViewController {
    init() {
        super.init(page: .colors)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        TSColor.allTokens.map { token in
            TokenRowView(
                name: token.name,
                detail: "\(DemoStrings.string(.lightAppearance)) \(TokenFormat.hex(token.light)) · \(DemoStrings.string(.darkAppearance)) \(TokenFormat.hex(token.dark))",
                visual: SwatchView(
                    fill: token,
                    stroke: .borderSubtle,
                    cornerRadius: TSRadius.radiusMd,
                    size: CGSize(width: TSSize.controlMd, height: TSSize.controlMd)
                )
            )
        }
    }
}
