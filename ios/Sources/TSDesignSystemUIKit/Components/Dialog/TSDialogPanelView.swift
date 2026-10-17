import UIKit

final class TSDialogPanelView: UIView {
    var variant: TSDialogVariant {
        didSet { applyStyle() }
    }

    var title: String {
        didSet { applyStyle() }
    }

    var message: String? {
        didSet { applyStyle() }
    }

    var bodyView: UIView? {
        didSet {
            oldValue?.removeFromSuperview()
            applyStyle()
        }
    }

    var actions: [TSDialogAction] {
        didSet { rebuildActions() }
    }

    var onAction: ((TSDialogAction) -> Void)?
    var onClose: (() -> Void)?

    let stackView = UIStackView()
    let titleLabel = UILabel()
    let messageLabel = UILabel()
    let actionsContainer = UIView()
    let actionsStack = UIStackView()
    private(set) var actionButtons: [TSButtonView] = []
    private var actionsLeading: NSLayoutConstraint?
    private var actionsLeadingFlexible: NSLayoutConstraint?

    init(
        title: String,
        message: String? = nil,
        variant: TSDialogVariant = .center,
        actions: [TSDialogAction] = []
    ) {
        self.title = title
        self.message = message
        self.variant = variant
        self.actions = actions
        super.init(frame: .zero)
        configureHierarchy()
        rebuildActions()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var resolved: TSDialogStyleSpec.Resolved {
        TSDialogStyleSpec.resolve(.init(variant: variant, isAccessibilitySize: traitCollection.preferredContentSizeCategory.isAccessibilityCategory))
    }

    var orderedActions: [TSDialogAction] {
        TSDialogLayout.orderedActions(actions, primaryFirst: resolved.primaryActionFirst)
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        let wasAccessibilitySize = previousTraitCollection?.preferredContentSizeCategory.isAccessibilityCategory
        if wasAccessibilitySize != traitCollection.preferredContentSizeCategory.isAccessibilityCategory {
            rebuildActions()
        } else {
            applyStyle()
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let resolved = resolved
        layer.cornerRadius = resolved.cornerRadius
        layer.cornerCurve = .continuous
        layer.maskedCorners = resolved.roundsBottomCorners
            ? [.layerMinXMinYCorner, .layerMaxXMinYCorner, .layerMinXMaxYCorner, .layerMaxXMaxYCorner]
            : [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        if let shadow = resolved.shadow {
            TSShadow.apply(shadow, style: traitCollection.userInterfaceStyle, to: [layer], cornerRadius: resolved.cornerRadius)
        } else {
            TSShadow.apply(nil, to: layer, cornerRadius: resolved.cornerRadius)
        }
    }

    override func accessibilityPerformEscape() -> Bool {
        guard let onClose else { return false }
        onClose()
        return true
    }

    private func configureHierarchy() {
        stackView.axis = .vertical
        stackView.alignment = .fill
        stackView.spacing = 0
        stackView.isLayoutMarginsRelativeArrangement = true
        stackView.insetsLayoutMarginsFromSafeArea = false
        stackView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)

        titleLabel.numberOfLines = 0
        titleLabel.accessibilityTraits = .header
        messageLabel.numberOfLines = 0
        [titleLabel, messageLabel].forEach {
            $0.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        }

        actionsStack.spacing = 0
        actionsStack.translatesAutoresizingMaskIntoConstraints = false
        actionsContainer.addSubview(actionsStack)

        [titleLabel, messageLabel, actionsContainer].forEach(stackView.addArrangedSubview)

        let leading = actionsStack.leadingAnchor.constraint(equalTo: actionsContainer.leadingAnchor)
        let flexible = actionsStack.leadingAnchor.constraint(greaterThanOrEqualTo: actionsContainer.leadingAnchor)
        actionsLeading = leading
        actionsLeadingFlexible = flexible
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor),
            actionsStack.topAnchor.constraint(equalTo: actionsContainer.topAnchor),
            actionsStack.bottomAnchor.constraint(equalTo: actionsContainer.bottomAnchor),
            actionsStack.trailingAnchor.constraint(equalTo: actionsContainer.trailingAnchor),
            flexible
        ])

        accessibilityCustomActions = [
            UIAccessibilityCustomAction(name: TSStrings.close) { [weak self] _ in
                MainActor.assumeIsolated {
                    self?.onClose?()
                }
                return true
            }
        ]
    }

    private func rebuildActions() {
        actionButtons.forEach { $0.removeFromSuperview() }
        let resolved = resolved
        actionButtons = orderedActions.map { action in
            let button = TSButtonView(title: action.label, variant: action.variant, primaryAction: UIAction { [weak self] _ in
                self?.onAction?(action)
            })
            button.isBlock = resolved.blockActions
            button.setContentCompressionResistancePriority(.required, for: .horizontal)
            return button
        }
        actionButtons.forEach(actionsStack.addArrangedSubview)
        applyStyle()
    }

    private func applyStyle() {
        let resolved = resolved
        let sizeCategory = traitCollection.preferredContentSizeCategory
        let alignment: NSTextAlignment = effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        backgroundColor = resolved.fill.uiColor.resolvedColor(with: traitCollection)
        stackView.directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: resolved.contentTopInset,
            leading: resolved.paddingHorizontal,
            bottom: resolved.paddingBottom,
            trailing: resolved.paddingHorizontal
        )
        titleLabel.attributedText = resolved.titleFont.attributedString(
            title,
            sizeCategory: sizeCategory,
            color: resolved.titleColor.uiColor.resolvedColor(with: traitCollection),
            alignment: alignment
        )
        messageLabel.isHidden = message == nil || bodyView != nil
        messageLabel.attributedText = message.map {
            resolved.bodyFont.attributedString($0, sizeCategory: sizeCategory, color: resolved.bodyColor.uiColor.resolvedColor(with: traitCollection), alignment: alignment)
        }
        if let bodyView, bodyView.superview !== stackView {
            stackView.insertArrangedSubview(bodyView, at: 2)
        }
        stackView.setCustomSpacing(resolved.titleSpacing, after: titleLabel)
        if let bodyView {
            stackView.setCustomSpacing(resolved.actionsTopSpacing, after: bodyView)
            stackView.setCustomSpacing(0, after: messageLabel)
        } else {
            stackView.setCustomSpacing(resolved.actionsTopSpacing, after: messageLabel)
        }
        if message == nil, bodyView == nil {
            stackView.setCustomSpacing(resolved.actionsTopSpacing, after: titleLabel)
        }
        actionsContainer.isHidden = actions.isEmpty
        let isVertical = resolved.actionsAxis == .vertical
        actionsStack.axis = isVertical ? .vertical : .horizontal
        actionsStack.alignment = isVertical ? .fill : .center
        actionsStack.spacing = resolved.actionsSpacing
        actionsLeadingFlexible?.isActive = !isVertical
        actionsLeading?.isActive = isVertical
        setNeedsLayout()
    }
}
