import UIKit

public final class TSSwitchView: UIControl {
    public var label: String {
        didSet { applyStyle() }
    }

    public var isOn: Bool {
        didSet { applyStyle() }
    }

    public var isLabelHidden: Bool {
        didSet { applyStyle() }
    }

    override public var isEnabled: Bool {
        didSet { applyStyle() }
    }

    override public var isHighlighted: Bool {
        didSet { applyStyle() }
    }

    let stackView = UIStackView()
    let trackView = UIView()
    let thumbShadowView = UIView()
    let thumbView = UIView()
    let labelView = UILabel()

    var togglePath: TSCompatPath = TSCompat.toggleTrait {
        didSet { applyStyle() }
    }

    var playHaptic: @MainActor (TSHapticEvent) -> Void = { TSHaptics.shared.play($0) }
    var reduceMotion: @MainActor () -> Bool = { TSReduceMotion.isEnabled }

    public init(label: String, isOn: Bool = false, isLabelHidden: Bool = false) {
        self.label = label
        self.isOn = isOn
        self.isLabelHidden = isLabelHidden
        super.init(frame: .zero)
        configureHierarchy()
        addAction(UIAction { [weak self] _ in self?.userDidToggle() }, for: .touchUpInside)
        applyStyle()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var controlState: TSControlState {
        .make(isPressed: isHighlighted, isEnabled: isEnabled, isFocused: isFocused)
    }

    var resolved: TSSwitchStyleSpec.Resolved {
        TSSwitchStyleSpec.resolve(.init(isOn: isOn, state: controlState))
    }

    public func setOn(_ isOn: Bool, animated: Bool) {
        guard animated else {
            self.isOn = isOn
            return
        }
        TSReduceMotion.animate(duration: resolved.animationDuration, reduceMotion: reduceMotion()) {
            self.isOn = isOn
        }
    }

    override public var canBecomeFocused: Bool {
        isEnabled
    }

    override public func didUpdateFocus(in context: UIFocusUpdateContext, with coordinator: UIFocusAnimationCoordinator) {
        super.didUpdateFocus(in: context, with: coordinator)
        applyStyle()
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        let resolved = resolved
        layoutThumb(resolved)
        focusEffect = UIFocusHaloEffect(
            roundedRect: trackView.convert(trackView.bounds, to: self),
            cornerRadius: resolved.trackCornerRadius,
            curve: .circular
        )
    }

    override public func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        TSHitArea.expandedRect(bounds).contains(point)
    }

    override public func accessibilityActivate() -> Bool {
        guard isEnabled else { return false }
        userDidToggle()
        return true
    }

    func userDidToggle() {
        guard isEnabled else { return }
        setOn(!isOn, animated: true)
        playHaptic(.selection)
        sendActions(for: .valueChanged)
    }

    private func configureHierarchy() {
        let resolved = resolved
        stackView.axis = .horizontal
        stackView.alignment = .center
        stackView.spacing = resolved.spacing
        stackView.isUserInteractionEnabled = false
        stackView.translatesAutoresizingMaskIntoConstraints = false
        [trackView, labelView].forEach(stackView.addArrangedSubview)
        addSubview(stackView)

        [thumbShadowView, thumbView].forEach(trackView.addSubview)
        labelView.numberOfLines = 0
        labelView.setContentCompressionResistancePriority(.required, for: .vertical)

        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(greaterThanOrEqualTo: topAnchor),
            stackView.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor),
            stackView.centerYAnchor.constraint(equalTo: centerYAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor),
            heightAnchor.constraint(greaterThanOrEqualToConstant: resolved.minHeight),
            trackView.widthAnchor.constraint(equalToConstant: resolved.trackWidth),
            trackView.heightAnchor.constraint(equalToConstant: resolved.trackHeight)
        ])
        let hugsContent = heightAnchor.constraint(equalTo: stackView.heightAnchor)
        hugsContent.priority = .defaultLow
        hugsContent.isActive = true
    }

    private func layoutThumb(_ resolved: TSSwitchStyleSpec.Resolved) {
        let isRightToLeft = effectiveUserInterfaceLayoutDirection == .rightToLeft
        let frame = CGRect(
            x: resolved.thumbOriginX(isRightToLeft: isRightToLeft),
            y: (resolved.trackHeight - resolved.thumbSize) / 2,
            width: resolved.thumbSize,
            height: resolved.thumbSize
        )
        thumbView.frame = frame
        thumbShadowView.frame = frame
        TSShadow.apply(
            resolved.thumbShadow,
            style: traitCollection.userInterfaceStyle,
            to: [thumbView.layer, thumbShadowView.layer],
            cornerRadius: resolved.thumbSize / 2
        )
    }

    private func applyStyle() {
        let resolved = resolved
        let traits = traitCollection
        let trackColor = resolved.trackColor.uiColor.resolvedColor(with: traits)
        trackView.backgroundColor = resolved.trackOpacity < 1 ? trackColor.withAlphaComponent(resolved.trackOpacity) : trackColor
        trackView.layer.cornerRadius = resolved.trackCornerRadius
        trackView.layer.cornerCurve = .circular
        let thumbColor = resolved.thumbColor.uiColor.resolvedColor(with: traits)
        [thumbView, thumbShadowView].forEach {
            $0.backgroundColor = thumbColor
            $0.layer.cornerRadius = resolved.thumbSize / 2
        }
        layoutThumb(resolved)

        labelView.isHidden = isLabelHidden
        labelView.attributedText = resolved.labelFont.attributedString(
            label,
            sizeCategory: traits.preferredContentSizeCategory,
            color: resolved.labelColor.uiColor.resolvedColor(with: traits),
            alignment: effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        )
        alpha = resolved.opacity

        accessibilityLabel = label
        TSAccessibilityTraitsCompat.apply(to: self, isOn: isOn, kind: .switch, path: togglePath)
        accessibilityTraits = isEnabled ? accessibilityTraits.subtracting(.notEnabled) : accessibilityTraits.union(.notEnabled)
    }
}
