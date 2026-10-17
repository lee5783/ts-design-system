import TSDesignSystemUIKit
import UIKit

final class ShadowCardView: UIView {
    private let token: TSShadowToken
    private let shadowLayers = [CALayer(), CALayer()]
    private let fillLayer = CALayer()

    init(token: TSShadowToken, content: UIView) {
        self.token = token
        super.init(frame: .zero)
        shadowLayers.forEach(layer.addSublayer)
        fillLayer.cornerRadius = TSRadius.radiusLg
        fillLayer.cornerCurve = .continuous
        layer.addSublayer(fillLayer)
        content.translatesAutoresizingMaskIntoConstraints = false
        addSubview(content)
        let hugging = heightAnchor.constraint(equalToConstant: 0)
        hugging.priority = .fittingSizeLevel
        NSLayoutConstraint.activate([
            hugging,
            content.topAnchor.constraint(equalTo: topAnchor, constant: TSSpacing.space4),
            content.leadingAnchor.constraint(equalTo: leadingAnchor, constant: TSSpacing.space4),
            content.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -TSSpacing.space4),
            content.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor, constant: -TSSpacing.space4),
            heightAnchor.constraint(greaterThanOrEqualToConstant: TSSpacing.space16 + TSSpacing.space8)
        ])
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        (shadowLayers + [fillLayer]).forEach { $0.frame = bounds }
        updateAppearance()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if traitCollection.hasDifferentColorAppearance(comparedTo: previousTraitCollection) {
            updateAppearance()
        }
    }

    private func updateAppearance() {
        fillLayer.backgroundColor = TSColor.surfaceCard.uiColor.resolvedColor(with: traitCollection).cgColor
        TSShadow.apply(token, style: traitCollection.userInterfaceStyle, to: shadowLayers, cornerRadius: TSRadius.radiusLg)
    }
}
