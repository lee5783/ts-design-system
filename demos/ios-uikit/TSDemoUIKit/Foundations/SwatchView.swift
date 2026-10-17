import TSDesignSystemUIKit
import UIKit

final class SwatchView: UIView {
    private let shapeLayer = CAShapeLayer()
    private let fill: UIColor
    private let stroke: UIColor?
    private let lineWidth: CGFloat
    private let cornerRadius: CGFloat

    init(
        fill: TSColor,
        stroke: TSColor? = nil,
        lineWidth: CGFloat = 1,
        cornerRadius: CGFloat,
        dashPattern: [NSNumber]? = nil,
        size: CGSize? = nil
    ) {
        self.fill = fill.uiColor
        self.stroke = stroke?.uiColor
        self.lineWidth = stroke == nil ? 0 : lineWidth
        self.cornerRadius = cornerRadius
        super.init(frame: .zero)
        shapeLayer.lineWidth = self.lineWidth
        shapeLayer.lineDashPattern = dashPattern
        layer.addSublayer(shapeLayer)
        isAccessibilityElement = false
        if let size {
            translatesAutoresizingMaskIntoConstraints = false
            NSLayoutConstraint.activate([
                widthAnchor.constraint(equalToConstant: size.width),
                heightAnchor.constraint(equalToConstant: size.height)
            ])
        }
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        shapeLayer.frame = bounds
        let rect = bounds.insetBy(dx: lineWidth / 2, dy: lineWidth / 2)
        let radius = min(cornerRadius, min(rect.width, rect.height) / 2)
        shapeLayer.path = UIBezierPath(roundedRect: rect, cornerRadius: max(0, radius)).cgPath
        updateColors()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if traitCollection.hasDifferentColorAppearance(comparedTo: previousTraitCollection) {
            updateColors()
        }
    }

    private func updateColors() {
        shapeLayer.fillColor = fill.resolvedColor(with: traitCollection).cgColor
        shapeLayer.strokeColor = stroke?.resolvedColor(with: traitCollection).cgColor
    }
}
