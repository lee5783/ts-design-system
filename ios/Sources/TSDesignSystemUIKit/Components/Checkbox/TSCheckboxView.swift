import UIKit

public final class TSCheckboxView: UIControl {
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
    let markView = UIView()
    let checkView = TSIconView(.check, size: .sm)
    let labelView = UILabel()

    var togglePath: TSCompatPath = TSCompat.toggleTrait {
        didSet { applyStyle() }
    }

    var playHaptic: @MainActor (TSHapticEvent) -> Void = { TSHaptics.shared.play($0) }

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

    var resolved: TSCheckboxStyleSpec.Resolved {
        TSCheckboxStyleSpec.resolve(.init(isOn: isOn, state: controlState))
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
        focusEffect = UIFocusHaloEffect(
            roundedRect: markView.convert(markView.bounds, to: self),
            cornerRadius: resolved.markCornerRadius,
            curve: .continuous
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
        isOn.toggle()
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
        [markView, labelView].forEach(stackView.addArrangedSubview)
        addSubview(stackView)

        checkView.translatesAutoresizingMaskIntoConstraints = false
        markView.addSubview(checkView)
        labelView.numberOfLines = 0
        labelView.setContentCompressionResistancePriority(.required, for: .vertical)

        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(greaterThanOrEqualTo: topAnchor),
            stackView.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor),
            stackView.centerYAnchor.constraint(equalTo: centerYAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor),
            heightAnchor.constraint(greaterThanOrEqualToConstant: resolved.minHeight),
            markView.widthAnchor.constraint(equalToConstant: resolved.markSize),
            markView.heightAnchor.constraint(equalToConstant: resolved.markSize),
            checkView.centerXAnchor.constraint(equalTo: markView.centerXAnchor),
            checkView.centerYAnchor.constraint(equalTo: markView.centerYAnchor)
        ])
        let hugsContent = heightAnchor.constraint(equalTo: stackView.heightAnchor)
        hugsContent.priority = .defaultLow
        hugsContent.isActive = true
    }

    private func applyStyle() {
        let resolved = resolved
        let traits = traitCollection
        markView.backgroundColor = resolved.markFill.uiColor.resolvedColor(with: traits)
        markView.tsApplyBorder(resolved.markBorder, width: resolved.markBorderWidth, cornerRadius: resolved.markCornerRadius)
        checkView.isHidden = !resolved.showsCheck
        checkView.size = resolved.checkSize
        checkView.tintColor = resolved.checkColor.uiColor.resolvedColor(with: traits)

        labelView.isHidden = isLabelHidden
        labelView.attributedText = resolved.labelFont.attributedString(
            label,
            sizeCategory: traits.preferredContentSizeCategory,
            color: resolved.labelColor.uiColor.resolvedColor(with: traits),
            alignment: effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        )
        alpha = resolved.opacity

        accessibilityLabel = label
        TSAccessibilityTraitsCompat.apply(to: self, isOn: isOn, kind: .checkbox, path: togglePath)
        accessibilityTraits = isEnabled ? accessibilityTraits.subtracting(.notEnabled) : accessibilityTraits.union(.notEnabled)
    }
}
