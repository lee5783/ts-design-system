import UIKit

final class TSListGroupBorderView: UIView {
    var position: TSListGroupPosition = .single {
        didSet { setNeedsLayout() }
    }

    let borderLayer = CAShapeLayer()
    let dividerLayer = CAShapeLayer()

    override init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = false
        isAccessibilityElement = false
        borderLayer.fillColor = nil
        layer.addSublayer(dividerLayer)
        layer.addSublayer(borderLayer)
        applyColors()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let group = TSListItemStyleSpec.group
        borderLayer.frame = bounds
        dividerLayer.frame = bounds
        borderLayer.lineWidth = group.borderWidth
        borderLayer.path = Self.borderPath(in: bounds, position: position, lineWidth: group.borderWidth, cornerRadius: group.cornerRadius).cgPath
        dividerLayer.path = position.showsTopDivider
            ? UIBezierPath(rect: CGRect(x: group.dividerInset, y: 0, width: bounds.width - group.dividerInset, height: group.dividerThickness)).cgPath
            : nil
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyColors()
    }

    static func borderPath(in bounds: CGRect, position: TSListGroupPosition, lineWidth: CGFloat, cornerRadius: CGFloat) -> UIBezierPath {
        let rect = bounds.insetBy(dx: lineWidth / 2, dy: lineWidth / 2)
        let radius = max(0, cornerRadius - lineWidth / 2)
        let path = UIBezierPath()
        let top = position.hasTopEdge ? rect.minY : bounds.minY
        let bottom = position.hasBottomEdge ? rect.maxY : bounds.maxY
        if position.hasBottomEdge {
            path.move(to: CGPoint(x: rect.minX, y: position.hasTopEdge ? rect.minY + radius : top))
            path.addLine(to: CGPoint(x: rect.minX, y: bottom - radius))
            path.addArc(withCenter: CGPoint(x: rect.minX + radius, y: bottom - radius), radius: radius, startAngle: .pi, endAngle: .pi / 2, clockwise: false)
            path.addLine(to: CGPoint(x: rect.maxX - radius, y: bottom))
            path.addArc(withCenter: CGPoint(x: rect.maxX - radius, y: bottom - radius), radius: radius, startAngle: .pi / 2, endAngle: 0, clockwise: false)
            path.addLine(to: CGPoint(x: rect.maxX, y: position.hasTopEdge ? rect.minY + radius : top))
        } else {
            path.move(to: CGPoint(x: rect.minX, y: bottom))
            path.addLine(to: CGPoint(x: rect.minX, y: position.hasTopEdge ? rect.minY + radius : top))
            path.move(to: CGPoint(x: rect.maxX, y: position.hasTopEdge ? rect.minY + radius : top))
            path.addLine(to: CGPoint(x: rect.maxX, y: bottom))
        }
        if position.hasTopEdge {
            path.move(to: CGPoint(x: rect.minX, y: rect.minY + radius))
            path.addArc(withCenter: CGPoint(x: rect.minX + radius, y: rect.minY + radius), radius: radius, startAngle: .pi, endAngle: .pi * 1.5, clockwise: true)
            path.addLine(to: CGPoint(x: rect.maxX - radius, y: rect.minY))
            path.addArc(withCenter: CGPoint(x: rect.maxX - radius, y: rect.minY + radius), radius: radius, startAngle: .pi * 1.5, endAngle: 0, clockwise: true)
        }
        return path
    }

    private func applyColors() {
        let group = TSListItemStyleSpec.group
        borderLayer.strokeColor = group.border.uiColor.resolvedColor(with: traitCollection).cgColor
        dividerLayer.fillColor = group.divider.uiColor.resolvedColor(with: traitCollection).cgColor
    }
}
