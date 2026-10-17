import UIKit

final class TSSpinnerView: UIView {
    private let arcLayer = CAShapeLayer()
    private let animationKey = "ts.spinner.rotation"

    var lineWidth: CGFloat = TSComponentTokens.spinnerStrokeWidth {
        didSet { setNeedsLayout() }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = false
        isAccessibilityElement = false
        arcLayer.fillColor = UIColor.clear.cgColor
        arcLayer.lineCap = .butt
        layer.addSublayer(arcLayer)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        arcLayer.frame = bounds
        arcLayer.lineWidth = lineWidth
        let radius = (min(bounds.width, bounds.height) - lineWidth) / 2
        let center = CGPoint(x: bounds.midX, y: bounds.midY)
        let start = CGFloat.pi / 4
        arcLayer.path = UIBezierPath(arcCenter: center, radius: max(0, radius), startAngle: start, endAngle: start + .pi * 1.5, clockwise: true).cgPath
        updateColor()
    }

    override func tintColorDidChange() {
        super.tintColorDidChange()
        updateColor()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        updateColor()
    }

    override func didMoveToWindow() {
        super.didMoveToWindow()
        updateAnimation()
    }

    override var isHidden: Bool {
        didSet { updateAnimation() }
    }

    private func updateColor() {
        arcLayer.strokeColor = tintColor.resolvedColor(with: traitCollection).cgColor
    }

    private func updateAnimation() {
        guard window != nil, !isHidden else {
            layer.removeAnimation(forKey: animationKey)
            return
        }
        guard layer.animation(forKey: animationKey) == nil else { return }
        let rotation = CABasicAnimation(keyPath: "transform.rotation.z")
        rotation.fromValue = 0
        rotation.toValue = CGFloat.pi * 2
        rotation.duration = TSComponentTokens.spinnerRotationDuration
        rotation.repeatCount = .infinity
        rotation.isRemovedOnCompletion = false
        layer.add(rotation, forKey: animationKey)
    }
}
