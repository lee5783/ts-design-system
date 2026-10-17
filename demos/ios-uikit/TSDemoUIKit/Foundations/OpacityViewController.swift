import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class OpacityViewController: DemoPageViewController {
    init() {
        super.init(page: .opacity)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        TSOpacity.allTokens.map { token in
            let translucent = SwatchView(fill: .accentPrimary, cornerRadius: TSRadius.radiusMd)
            translucent.alpha = token.value
            let pair = UIStackView(arrangedSubviews: [
                SwatchView(fill: .accentPrimary, cornerRadius: TSRadius.radiusMd),
                translucent
            ])
            pair.distribution = .fillEqually
            pair.spacing = TSSpacing.space1
            pair.translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                pair.widthAnchor.constraint(equalToConstant: TSSpacing.space16 + TSSpacing.space6),
                pair.heightAnchor.constraint(equalToConstant: TSSize.controlMd)
            ])
            return TokenRowView(name: token.name, detail: "\(Int((token.value * 100).rounded()))%", visual: pair)
        }
    }
}
