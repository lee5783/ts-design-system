import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class SizeViewController: DemoPageViewController {
    private let visualLimit = TSSpacing.space16

    init() {
        super.init(page: .size)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        TSSize.allTokens.map { token in
            let side = min(token.value, visualLimit)
            let frame = UIView()
            frame.translatesAutoresizingMaskIntoConstraints = false
            let square = SwatchView(
                fill: .accentSubtle,
                stroke: .accentPrimary,
                cornerRadius: TSRadius.radiusXs,
                dashPattern: token.value > visualLimit ? [4, 3] : nil,
                size: CGSize(width: side, height: side)
            )
            frame.addSubview(square)
            NSLayoutConstraint.activate([
                frame.widthAnchor.constraint(equalToConstant: visualLimit),
                frame.heightAnchor.constraint(equalToConstant: visualLimit),
                square.centerXAnchor.constraint(equalTo: frame.centerXAnchor),
                square.centerYAnchor.constraint(equalTo: frame.centerYAnchor)
            ])
            return TokenRowView(name: token.name, detail: TokenFormat.points(token.value), visual: frame)
        }
    }
}
