import UIKit

public final class TSToastView: UIView {
    public var message: String {
        didSet { applyStyle() }
    }

    public var tone: TSToastTone {
        didSet { applyStyle() }
    }

    public var actionTitle: String? {
        didSet { applyStyle() }
    }

    public var onAction: (() -> Void)? {
        didSet { applyStyle() }
    }

    let stackView = UIStackView()
    let messageView = UIView()
    let iconView = TSIconView(.info, size: .md)
    let messageLabel = UILabel()
    let actionButton = TSToastActionButton()
    private var labelLeadingToIcon: NSLayoutConstraint?
    private var labelLeadingToEdge: NSLayoutConstraint?

    public init(_ message: String, tone: TSToastTone = .neutral, actionTitle: String? = nil, onAction: (() -> Void)? = nil) {
        self.message = message
        self.tone = tone
        self.actionTitle = actionTitle
        self.onAction = onAction
        super.init(frame: .zero)
        configureHierarchy()
        applyStyle()
    }

    public convenience init(item: TSToastItem, onAction: (() -> Void)? = nil) {
        self.init(item.message, tone: item.tone, actionTitle: item.action?.title, onAction: onAction ?? item.action.map { action in { action.handler() } })
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var resolved: TSToastStyleSpec.Resolved {
        TSToastStyleSpec.resolve(.init(tone: tone, hasAction: actionTitle != nil && onAction != nil))
    }

    var accessibilityText: String {
        TSToastAccessibility.label(tone: tone, message: message)
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        let resolved = resolved
        layer.cornerRadius = resolved.cornerRadius
        layer.cornerCurve = .continuous
        TSShadow.apply(resolved.shadow, style: traitCollection.userInterfaceStyle, to: [layer], cornerRadius: resolved.cornerRadius)
    }

    private func configureHierarchy() {
        let resolved = resolved
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.spacing = resolved.spacing
        stackView.isLayoutMarginsRelativeArrangement = true
        stackView.insetsLayoutMarginsFromSafeArea = false
        stackView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)

        messageLabel.numberOfLines = 0
        messageLabel.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)

        [iconView, messageLabel].forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
            messageView.addSubview($0)
        }
        let leadingToIcon = messageLabel.leadingAnchor.constraint(equalTo: iconView.trailingAnchor, constant: resolved.spacing)
        let leadingToEdge = messageLabel.leadingAnchor.constraint(equalTo: messageView.leadingAnchor)
        labelLeadingToIcon = leadingToIcon
        labelLeadingToEdge = leadingToEdge
        NSLayoutConstraint.activate([
            iconView.leadingAnchor.constraint(equalTo: messageView.leadingAnchor),
            iconView.centerYAnchor.constraint(equalTo: messageView.centerYAnchor),
            iconView.topAnchor.constraint(greaterThanOrEqualTo: messageView.topAnchor),
            messageLabel.topAnchor.constraint(equalTo: messageView.topAnchor),
            messageLabel.bottomAnchor.constraint(equalTo: messageView.bottomAnchor),
            messageLabel.trailingAnchor.constraint(equalTo: messageView.trailingAnchor)
        ])
        messageView.isAccessibilityElement = true
        messageView.accessibilityTraits = .staticText
        messageView.setContentHuggingPriority(.defaultLow, for: .horizontal)
        messageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        iconView.setContentHuggingPriority(.required, for: .horizontal)
        iconView.setContentCompressionResistancePriority(.required, for: .horizontal)
        iconView.tintAdjustmentMode = .normal

        actionButton.addAction(UIAction { [weak self] _ in self?.onAction?() }, for: .primaryActionTriggered)

        [messageView, actionButton].forEach(stackView.addArrangedSubview)

        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor),
            widthAnchor.constraint(lessThanOrEqualToConstant: resolved.maxWidth)
        ])
    }

    private func applyStyle() {
        let resolved = resolved
        let sizeCategory = traitCollection.preferredContentSizeCategory
        let alignment: NSTextAlignment = effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        backgroundColor = resolved.fill.uiColor.resolvedColor(with: traitCollection)
        let trailing = resolved.showsAction ? resolved.horizontalPadding - resolved.actionHorizontalPadding : resolved.horizontalPadding
        stackView.directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: resolved.verticalPadding,
            leading: resolved.horizontalPadding,
            bottom: resolved.verticalPadding,
            trailing: trailing
        )
        iconView.isHidden = resolved.icon == nil
        let active = iconView.isHidden ? labelLeadingToEdge : labelLeadingToIcon
        let inactive = iconView.isHidden ? labelLeadingToIcon : labelLeadingToEdge
        inactive?.isActive = false
        active?.isActive = true
        if let icon = resolved.icon, let iconColor = resolved.iconColor {
            iconView.name = icon
            iconView.size = resolved.iconSize
            iconView.tintColor = iconColor.uiColor.resolvedColor(with: traitCollection)
        }
        messageLabel.attributedText = resolved.messageFont.attributedString(
            message,
            sizeCategory: sizeCategory,
            color: resolved.messageColor.uiColor.resolvedColor(with: traitCollection),
            alignment: alignment
        )
        messageView.accessibilityLabel = accessibilityText
        actionButton.isHidden = !resolved.showsAction
        actionButton.apply(title: actionTitle ?? "", resolved: resolved, traitCollection: traitCollection)
    }
}

final class TSToastActionButton: UIControl {
    let titleLabel = UILabel()
    private var paddingConstraints: [NSLayoutConstraint] = []

    override var isHighlighted: Bool {
        didSet { alpha = TSPressFeedback.opacity.opacity(isPressed: isHighlighted) }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        titleLabel.isUserInteractionEnabled = false
        addSubview(titleLabel)
        let leading = titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor)
        let trailing = trailingAnchor.constraint(equalTo: titleLabel.trailingAnchor)
        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(greaterThanOrEqualTo: topAnchor),
            titleLabel.centerYAnchor.constraint(equalTo: centerYAnchor),
            leading,
            trailing
        ])
        paddingConstraints = [leading, trailing]
        setContentHuggingPriority(.required, for: .horizontal)
        setContentCompressionResistancePriority(.required, for: .horizontal)
        titleLabel.setContentCompressionResistancePriority(.required, for: .horizontal)
        titleLabel.setContentHuggingPriority(.required, for: .horizontal)
        addAction(UIAction { [weak self] _ in self?.sendActions(for: .primaryActionTriggered) }, for: .touchUpInside)
        isAccessibilityElement = true
        accessibilityTraits = .button
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    func apply(title: String, resolved: TSToastStyleSpec.Resolved, traitCollection: UITraitCollection) {
        let color = resolved.actionColor.uiColor.resolvedColor(with: traitCollection)
        var attributes = resolved.actionFont.attributes(for: traitCollection.preferredContentSizeCategory, color: color)
        if resolved.actionIsUnderlined {
            attributes[.underlineStyle] = NSUnderlineStyle.single.rawValue
        }
        titleLabel.attributedText = NSAttributedString(string: resolved.actionFont.text(title), attributes: attributes)
        paddingConstraints.forEach { $0.constant = resolved.actionHorizontalPadding }
        accessibilityLabel = title
    }

    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        TSHitArea.expandedRect(bounds).contains(point)
    }

    override func accessibilityActivate() -> Bool {
        sendActions(for: .primaryActionTriggered)
        return true
    }
}
