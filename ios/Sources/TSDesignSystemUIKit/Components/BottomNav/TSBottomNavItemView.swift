import UIKit

final class TSBottomNavItemView: UIControl {
    var item: TSBottomNavItem {
        didSet { applyStyle() }
    }

    var position: (index: Int, count: Int) {
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

    var onSelect: @MainActor (TSBottomNavItemView) -> Void = { _ in }

    let pillView = UIView()
    let iconView: TSIconView
    let labelView = UILabel()
    let badgeView = UIView()
    let badgeLabel = UILabel()

    private var badgeLeft: NSLayoutConstraint?
    private var badgeRight: NSLayoutConstraint?

    init(item: TSBottomNavItem, position: (index: Int, count: Int)) {
        self.item = item
        self.position = position
        iconView = TSIconView(item.icon, size: .lg)
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

    var resolved: TSBottomNavStyleSpec.Resolved {
        TSBottomNavStyleSpec.resolve(.init(isSelected: isSelected, state: controlState))
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
        let isRightToLeft = effectiveUserInterfaceLayoutDirection == .rightToLeft
        badgeLeft?.isActive = !isRightToLeft
        badgeRight?.isActive = isRightToLeft
        focusEffect = UIFocusHaloEffect(
            roundedRect: pillView.frame,
            cornerRadius: resolved.pillCornerRadius,
            curve: .continuous
        )
    }

    override func accessibilityActivate() -> Bool {
        guard isEnabled else { return false }
        onSelect(self)
        return true
    }

    private func configureHierarchy() {
        let resolved = resolved
        let badge = TSBottomNavStyleSpec.badge
        [pillView, iconView, labelView, badgeView, badgeLabel].forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
            $0.isUserInteractionEnabled = false
            $0.isAccessibilityElement = false
        }
        pillView.layer.cornerCurve = .continuous
        pillView.layer.cornerRadius = resolved.pillCornerRadius
        badgeView.layer.cornerCurve = .continuous
        badgeView.layer.cornerRadius = badge.cornerRadius
        labelView.numberOfLines = 1
        labelView.textAlignment = .center
        labelView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        badgeLabel.textAlignment = .center
        badgeLabel.numberOfLines = 1
        badgeLabel.setContentCompressionResistancePriority(.required, for: .horizontal)

        addSubview(pillView)
        pillView.addSubview(iconView)
        addSubview(labelView)
        addSubview(badgeView)
        badgeView.addSubview(badgeLabel)

        let content = UILayoutGuide()
        addLayoutGuide(content)
        badgeLeft = badgeView.leftAnchor.constraint(equalTo: centerXAnchor, constant: badge.offsetStart)
        badgeRight = badgeView.rightAnchor.constraint(equalTo: centerXAnchor, constant: -badge.offsetStart)
        badgeLeft?.isActive = true
        NSLayoutConstraint.activate([
            content.centerYAnchor.constraint(equalTo: centerYAnchor),
            content.topAnchor.constraint(greaterThanOrEqualTo: topAnchor),
            content.leadingAnchor.constraint(equalTo: leadingAnchor),
            content.trailingAnchor.constraint(equalTo: trailingAnchor),
            pillView.topAnchor.constraint(equalTo: content.topAnchor),
            pillView.centerXAnchor.constraint(equalTo: centerXAnchor),
            pillView.widthAnchor.constraint(equalToConstant: resolved.pillWidth),
            pillView.heightAnchor.constraint(equalToConstant: resolved.pillHeight),
            iconView.centerXAnchor.constraint(equalTo: pillView.centerXAnchor),
            iconView.centerYAnchor.constraint(equalTo: pillView.centerYAnchor),
            labelView.topAnchor.constraint(equalTo: pillView.bottomAnchor, constant: resolved.spacing),
            labelView.leadingAnchor.constraint(greaterThanOrEqualTo: content.leadingAnchor, constant: TSSpacing.space0_5),
            labelView.trailingAnchor.constraint(lessThanOrEqualTo: content.trailingAnchor, constant: -TSSpacing.space0_5),
            labelView.centerXAnchor.constraint(equalTo: centerXAnchor),
            labelView.bottomAnchor.constraint(equalTo: content.bottomAnchor),
            badgeView.topAnchor.constraint(equalTo: pillView.topAnchor, constant: badge.topFromPill),
            badgeView.heightAnchor.constraint(equalToConstant: badge.height),
            badgeView.widthAnchor.constraint(greaterThanOrEqualToConstant: badge.minWidth),
            badgeLabel.centerYAnchor.constraint(equalTo: badgeView.centerYAnchor),
            badgeLabel.leadingAnchor.constraint(equalTo: badgeView.leadingAnchor, constant: badge.horizontalPadding),
            badgeLabel.trailingAnchor.constraint(equalTo: badgeView.trailingAnchor, constant: -badge.horizontalPadding)
        ])
        showsLargeContentViewer = true
        scalesLargeContentImage = true
    }

    private func applyStyle() {
        let resolved = resolved
        let traits = traitCollection
        let badge = TSBottomNavStyleSpec.badge
        let foreground = resolved.foreground.uiColor.resolvedColor(with: traits)
        let sizeCategory = TSBottomNavStyleSpec.labelSizeCategory(for: traits.preferredContentSizeCategory)

        iconView.name = item.icon
        iconView.tintColor = foreground
        pillView.backgroundColor = resolved.pillFill?.uiColor.resolvedColor(with: traits) ?? .clear
        labelView.attributedText = resolved.labelFont.truncatingAttributedString(
            item.label,
            sizeCategory: sizeCategory,
            color: foreground,
            alignment: .center,
            isSingleLine: true
        )
        badgeView.isHidden = item.badgeText == nil
        badgeView.backgroundColor = badge.fill.uiColor.resolvedColor(with: traits)
        badgeLabel.text = item.badgeText
        badgeLabel.font = TSBottomNavStyleSpec.badgeFont()
        badgeLabel.textColor = badge.foreground.uiColor.resolvedColor(with: traits)
        alpha = resolved.opacity

        largeContentTitle = item.label
        largeContentImage = item.icon.templateImage(pointSize: resolved.iconSize.pointSize)
        isAccessibilityElement = true
        accessibilityLabel = item.label
        accessibilityValue = TSBottomNavAccessibility.value(index: position.index, count: position.count, badge: item.badge)
        var accessibilityTraits: UIAccessibilityTraits = isSelected ? .selected : []
        if !isEnabled {
            accessibilityTraits.insert(.notEnabled)
        }
        self.accessibilityTraits = accessibilityTraits
        setNeedsLayout()
    }
}
