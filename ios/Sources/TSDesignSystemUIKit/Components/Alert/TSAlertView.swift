import UIKit

public final class TSAlertView: UIView {
    public var tone: TSAlertTone {
        didSet { applyStyle() }
    }

    public var title: String? {
        didSet { applyStyle() }
    }

    public var message: String? {
        didSet { applyStyle() }
    }

    public var icon: TSIconName? {
        didSet { applyStyle() }
    }

    public var actionView: UIView? {
        didSet {
            oldValue?.removeFromSuperview()
            applyStyle()
        }
    }

    public var onDismiss: (() -> Void)? {
        didSet { applyStyle() }
    }

    public var announcesWhenShown: Bool

    override public var isHidden: Bool {
        didSet {
            if oldValue, !isHidden {
                announceIfNeeded()
            }
        }
    }

    var announce: @MainActor (String) -> Void = { TSAlertAccessibility.post($0) }

    let stackView = UIStackView()
    let messageView = UIStackView()
    let iconView = TSIconView(.info, size: .md)
    let textStack = UIStackView()
    let titleLabel = UILabel()
    let messageLabel = UILabel()
    let dismissButton = TSAlertDismissButton()

    public init(
        tone: TSAlertTone = .info,
        title: String? = nil,
        message: String? = nil,
        icon: TSIconName? = nil,
        announcesWhenShown: Bool = false,
        onDismiss: (() -> Void)? = nil
    ) {
        self.tone = tone
        self.title = title
        self.message = message
        self.icon = icon
        self.announcesWhenShown = announcesWhenShown
        self.onDismiss = onDismiss
        super.init(frame: .zero)
        configureHierarchy()
        applyStyle()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var resolved: TSAlertStyleSpec.Resolved {
        TSAlertStyleSpec.resolve(.init(tone: tone, hasTitle: title != nil, isDismissible: onDismiss != nil))
    }

    var announcement: String {
        TSAlertAccessibility.label(tone: tone, title: title, message: message)
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    override public func didMoveToWindow() {
        super.didMoveToWindow()
        if window != nil, !isHidden {
            announceIfNeeded()
        }
    }

    override public func accessibilityPerformEscape() -> Bool {
        guard let onDismiss else { return false }
        onDismiss()
        return true
    }

    private func announceIfNeeded() {
        guard announcesWhenShown else { return }
        announce(announcement)
    }

    private func configureHierarchy() {
        let resolved = resolved
        let inset = NSDirectionalEdgeInsets(
            top: resolved.verticalPadding + resolved.borderWidth,
            leading: resolved.horizontalPadding + resolved.borderWidth,
            bottom: resolved.verticalPadding + resolved.borderWidth,
            trailing: resolved.horizontalPadding + resolved.borderWidth
        )
        stackView.axis = .horizontal
        stackView.alignment = .top
        stackView.spacing = resolved.spacing
        stackView.isLayoutMarginsRelativeArrangement = true
        stackView.insetsLayoutMarginsFromSafeArea = false
        stackView.directionalLayoutMargins = inset
        stackView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)

        textStack.axis = .vertical
        textStack.alignment = .fill
        textStack.spacing = 0
        [titleLabel, messageLabel].forEach(textStack.addArrangedSubview)
        titleLabel.numberOfLines = 0
        messageLabel.numberOfLines = 0
        [titleLabel, messageLabel].forEach {
            $0.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        }

        messageView.axis = .horizontal
        messageView.alignment = .top
        messageView.spacing = resolved.spacing
        [iconView, textStack].forEach(messageView.addArrangedSubview)
        messageView.isAccessibilityElement = true
        messageView.accessibilityTraits = .staticText
        messageView.setContentHuggingPriority(.defaultLow, for: .horizontal)
        messageView.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        iconView.setContentHuggingPriority(.required, for: .horizontal)
        iconView.setContentCompressionResistancePriority(.required, for: .horizontal)
        iconView.tintAdjustmentMode = .normal

        dismissButton.addAction(UIAction { [weak self] _ in self?.onDismiss?() }, for: .primaryActionTriggered)

        [messageView, dismissButton].forEach(stackView.addArrangedSubview)

        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
    }

    private func applyStyle() {
        let resolved = resolved
        let sizeCategory = traitCollection.preferredContentSizeCategory
        let alignment: NSTextAlignment = effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        backgroundColor = resolved.fill.uiColor.resolvedColor(with: traitCollection)
        layer.cornerRadius = resolved.cornerRadius
        layer.cornerCurve = .continuous
        iconView.name = icon ?? resolved.icon
        iconView.size = resolved.iconSize
        iconView.tintColor = resolved.iconColor.uiColor.resolvedColor(with: traitCollection)
        titleLabel.isHidden = !(resolved.showsTitle && title != nil)
        titleLabel.attributedText = title.map {
            resolved.titleFont.attributedString($0, sizeCategory: sizeCategory, color: resolved.titleColor.uiColor.resolvedColor(with: traitCollection), alignment: alignment)
        }
        messageLabel.isHidden = message == nil
        messageLabel.attributedText = message.map {
            resolved.messageFont.attributedString($0, sizeCategory: sizeCategory, color: resolved.messageColor.uiColor.resolvedColor(with: traitCollection), alignment: alignment)
        }
        if let actionView, actionView.superview !== stackView {
            actionView.setContentHuggingPriority(.required, for: .horizontal)
            actionView.setContentCompressionResistancePriority(.required, for: .horizontal)
            stackView.insertArrangedSubview(actionView, at: 1)
        }
        dismissButton.isHidden = !resolved.showsDismiss
        dismissButton.apply(resolved)
        messageView.accessibilityLabel = announcement
    }
}

final class TSAlertDismissButton: UIControl {
    let iconView = TSIconView(.close, size: .md)

    override var isHighlighted: Bool {
        didSet { alpha = TSPressFeedback.opacity.opacity(isPressed: isHighlighted) }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        iconView.translatesAutoresizingMaskIntoConstraints = false
        iconView.isUserInteractionEnabled = false
        iconView.tintAdjustmentMode = .normal
        addSubview(iconView)
        NSLayoutConstraint.activate([
            iconView.topAnchor.constraint(equalTo: topAnchor),
            iconView.bottomAnchor.constraint(equalTo: bottomAnchor),
            iconView.leadingAnchor.constraint(equalTo: leadingAnchor),
            iconView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
        setContentHuggingPriority(.required, for: .horizontal)
        setContentCompressionResistancePriority(.required, for: .horizontal)
        addAction(UIAction { [weak self] _ in self?.sendActions(for: .primaryActionTriggered) }, for: .touchUpInside)
        isAccessibilityElement = true
        accessibilityTraits = .button
        accessibilityLabel = TSStrings.dismiss
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    func apply(_ resolved: TSAlertStyleSpec.Resolved) {
        iconView.name = resolved.dismissIcon
        iconView.size = resolved.dismissIconSize
        iconView.tintColor = resolved.dismissColor.uiColor.resolvedColor(with: traitCollection)
    }

    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        TSHitArea.expandedRect(bounds).contains(point)
    }

    override func accessibilityActivate() -> Bool {
        sendActions(for: .primaryActionTriggered)
        return true
    }
}
