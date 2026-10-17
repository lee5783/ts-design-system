import UIKit

final class TSTabView: UIControl {
    let item: TSTabItem
    let labelView = UILabel()

    var position: (index: Int, count: Int) {
        didSet { applyStyle() }
    }

    var variant: TSTabsVariant {
        didSet { applyStyle() }
    }

    override var isSelected: Bool {
        didSet { applyStyle() }
    }

    override var isEnabled: Bool {
        didSet { applyStyle() }
    }

    override var isHighlighted: Bool {
        didSet { applyStyle() }
    }

    var onSelect: @MainActor (TSTabView) -> Void = { _ in }

    private var minHeightConstraint: NSLayoutConstraint?
    private var labelInsets: [NSLayoutConstraint] = []

    init(item: TSTabItem, position: (index: Int, count: Int), variant: TSTabsVariant) {
        self.item = item
        self.position = position
        self.variant = variant
        super.init(frame: .zero)
        configureHierarchy()
        addAction(UIAction { [weak self] _ in self.map { $0.onSelect($0) } }, for: .touchUpInside)
        applyStyle()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var controlState: TSControlState {
        .make(isPressed: isHighlighted, isEnabled: isEnabled, isFocused: isFocused)
    }

    var resolved: TSTabsStyleSpec.Resolved {
        TSTabsStyleSpec.resolve(.init(variant: variant, isSelected: isSelected, state: controlState))
    }

    override var canBecomeFocused: Bool {
        isEnabled
    }

    override func didUpdateFocus(in context: UIFocusUpdateContext, with coordinator: UIFocusAnimationCoordinator) {
        super.didUpdateFocus(in: context, with: coordinator)
        applyStyle()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        focusEffect = UIFocusHaloEffect(roundedRect: bounds, cornerRadius: resolved.cornerRadius, curve: .continuous)
    }

    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        TSHitArea.expandedRect(bounds).contains(point)
    }

    override func accessibilityActivate() -> Bool {
        guard isEnabled else { return false }
        onSelect(self)
        return true
    }

    private func configureHierarchy() {
        labelView.translatesAutoresizingMaskIntoConstraints = false
        labelView.isUserInteractionEnabled = false
        labelView.isAccessibilityElement = false
        labelView.setContentCompressionResistancePriority(.required, for: .vertical)
        labelView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        labelView.baselineAdjustment = .alignCenters
        setContentHuggingPriority(.defaultHigh, for: .horizontal)
        addSubview(labelView)
        labelInsets = [
            labelView.topAnchor.constraint(greaterThanOrEqualTo: topAnchor),
            labelView.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor),
            labelView.leadingAnchor.constraint(equalTo: leadingAnchor),
            labelView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ]
        let minHeight = heightAnchor.constraint(greaterThanOrEqualToConstant: 0)
        minHeightConstraint = minHeight
        let hugsContent = heightAnchor.constraint(equalTo: labelView.heightAnchor)
        hugsContent.priority = .defaultLow
        NSLayoutConstraint.activate(labelInsets + [
            minHeight,
            hugsContent,
            labelView.centerYAnchor.constraint(equalTo: centerYAnchor)
        ])
    }

    private func applyStyle() {
        let resolved = resolved
        let traits = traitCollection
        let isUnderline = variant == .underline
        labelInsets[0].constant = resolved.paddingVertical
        labelInsets[1].constant = -resolved.paddingVertical
        labelInsets[2].constant = resolved.paddingHorizontal
        labelInsets[3].constant = -resolved.paddingHorizontal
        minHeightConstraint?.constant = resolved.minHeight

        labelView.numberOfLines = 1
        labelView.adjustsFontSizeToFitWidth = !isUnderline
        labelView.minimumScaleFactor = isUnderline ? 1 : TSTabsStyleSpec.segmentMinimumScaleFactor
        showsLargeContentViewer = !isUnderline
        largeContentTitle = item.label
        let labelColor = resolved.labelColor.uiColor.resolvedColor(with: traits)
        if isUnderline {
            labelView.attributedText = resolved.labelFont.attributedString(
                item.label,
                sizeCategory: traits.preferredContentSizeCategory,
                color: labelColor,
                alignment: .center
            )
        } else {
            labelView.attributedText = nil
            labelView.text = item.label
            labelView.font = resolved.labelFont.uiFont(for: traits.preferredContentSizeCategory)
            labelView.textColor = labelColor
            labelView.textAlignment = .center
        }
        labelView.lineBreakMode = .byTruncatingTail
        backgroundColor = isSelected ? .clear : (resolved.fill?.uiColor.resolvedColor(with: traits) ?? .clear)
        layer.cornerRadius = resolved.cornerRadius
        layer.cornerCurve = .continuous
        alpha = resolved.opacity

        isAccessibilityElement = true
        accessibilityLabel = item.label
        accessibilityValue = TSStrings.tabPosition(position.index + 1, of: position.count)
        var accessibilityTraits: UIAccessibilityTraits = isSelected ? .selected : []
        if !isEnabled {
            accessibilityTraits.insert(.notEnabled)
        }
        self.accessibilityTraits = accessibilityTraits
    }
}
