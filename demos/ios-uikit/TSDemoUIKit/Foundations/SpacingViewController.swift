import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class SpacingViewController: DemoPageViewController {
    init() {
        super.init(page: .spacing)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        TSSpacing.allTokens.map { token in
            let track = UIView()
            track.translatesAutoresizingMaskIntoConstraints = false
            let bar = SwatchView(
                fill: .accentPrimary,
                cornerRadius: TSRadius.radiusXs,
                size: CGSize(width: token.value, height: TSSize.iconMd)
            )
            track.addSubview(bar)
            NSLayoutConstraint.activate([
                track.widthAnchor.constraint(equalToConstant: TSSpacing.space16),
                track.heightAnchor.constraint(equalToConstant: TSSize.iconMd),
                bar.leadingAnchor.constraint(equalTo: track.leadingAnchor),
                bar.centerYAnchor.constraint(equalTo: track.centerYAnchor)
            ])
            return TokenRowView(name: token.name, detail: TokenFormat.points(token.value), visual: track)
        }
    }
}
