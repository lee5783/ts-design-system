import UIKit

public final class TSSpinnerView: UIView {
    private let arcLayer = CAShapeLayer()
    private let animationKey = "ts.spinner.rotation"
    private var usesStyleSpec = false

    public var size: TSSpinnerSize = .md {
        didSet { applyStyle() }
    }

    public var tone: TSSpinnerTone = .primary {
        didSet { applyStyle() }
    }

    var lineWidth: CGFloat = TSComponentTokens.spinnerStrokeWidth {
        didSet { setNeedsLayout() }
    }

    public convenience init(size: TSSpinnerSize = .md, tone: TSSpinnerTone = .primary) {
        self.init(frame: .zero)
        usesStyleSpec = true
        isAccessibilityElement = true
        accessibilityLabel = TSStrings.loading
        accessibilityTraits = .updatesFrequently
        setContentHuggingPriority(.required, for: .horizontal)
        setContentHuggingPriority(.required, for: .vertical)
        self.size = size
        self.tone = tone
        applyStyle()
    }

    public convenience init() {
        self.init(size: .md, tone: .primary)
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

    var resolved: TSSpinnerStyleSpec.Resolved {
        TSSpinnerStyleSpec.resolve(.init(size: size, tone: tone))
    }

    override public var intrinsicContentSize: CGSize {
        guard usesStyleSpec else { return super.intrinsicContentSize }
        let side = resolved.size
        return CGSize(width: side, height: side)
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        arcLayer.frame = bounds
        arcLayer.lineWidth = lineWidth
        let radius = (min(bounds.width, bounds.height) - lineWidth) / 2
        let center = CGPoint(x: bounds.midX, y: bounds.midY)
        let start = CGFloat.pi / 4
        arcLayer.path = UIBezierPath(arcCenter: center, radius: max(0, radius), startAngle: start, endAngle: start + .pi * 1.5, clockwise: true).cgPath
        updateColor()
    }

    override public func tintColorDidChange() {
        super.tintColorDidChange()
        updateColor()
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        updateColor()
    }

    override public func didMoveToWindow() {
        super.didMoveToWindow()
        updateAnimation()
    }

    override public var isHidden: Bool {
        didSet { updateAnimation() }
    }

    private func applyStyle() {
        guard usesStyleSpec else { return }
        let resolved = resolved
        lineWidth = resolved.strokeWidth
        tintColor = resolved.color.uiColor
        invalidateIntrinsicContentSize()
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
