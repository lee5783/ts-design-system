import UIKit

public final class TSCardView: UIControl {
    public var title: String? {
        didSet { applyContent() }
    }

    public var subtitle: String? {
        didSet { applyContent() }
    }

    public var bodyText: String? {
        didSet { applyContent() }
    }

    public var bodyView: UIView? {
        didSet {
            oldValue?.removeFromSuperview()
            applyContent()
        }
    }

    public var accessoryView: UIView? {
        didSet {
            typedAccessory = nil
            oldValue?.removeFromSuperview()
            (oldValue as? TSIconButtonView)?.isDimmedByContainer = false
            (oldValue as? TSButtonView)?.isDimmedByContainer = false
            applyContent()
        }
    }

    public var accessory: TSCardAccessory? {
        get { typedAccessory }
        set {
            accessoryView = newValue.map(Self.makeAccessoryView)
            typedAccessory = newValue
            applyStyle()
        }
    }

    public var footerActions: [TSCardAction] {
        didSet { rebuildFooter() }
    }

    public var isRaised: Bool {
        didSet { applyStyle() }
    }

    public var isInteractive: Bool {
        didSet { applyStyle() }
    }

    override public var isEnabled: Bool {
        didSet { applyStyle() }
    }

    override public var isHighlighted: Bool {
        didSet { applyStyle() }
    }

    let surfaceView = UIView()
    let contentStack = UIStackView()
    let headerStack = UIStackView()
    let headingStack = UIStackView()
    let titleLabel = UILabel()
    let subtitleLabel = UILabel()
    let bodyStack = UIStackView()
    let bodyLabel = UILabel()
    let footerView = UIView()
    let footerDivider = UIView()
    let footerStack = UIStackView()
    private(set) var footerButtons: [TSButtonView] = []
    private var typedAccessory: TSCardAccessory?

    public init(
        title: String? = nil,
        subtitle: String? = nil,
        bodyText: String? = nil,
        isRaised: Bool = false,
        isInteractive: Bool = false,
        footerActions: [TSCardAction] = [],
        accessory: TSCardAccessory? = nil
    ) {
        self.title = title
        self.subtitle = subtitle
        self.bodyText = bodyText
        self.isRaised = isRaised
        self.isInteractive = isInteractive
        self.footerActions = footerActions
        self.typedAccessory = accessory
        super.init(frame: .zero)
        configureHierarchy()
        addAction(UIAction { [weak self] _ in self?.userDidActivate() }, for: .touchUpInside)
        accessoryView = accessory.map(Self.makeAccessoryView)
        rebuildFooter()
        applyContent()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var controlState: TSControlState {
        .make(isPressed: isHighlighted, isEnabled: isEnabled, isFocused: isFocused)
    }

    var resolved: TSCardStyleSpec.Resolved {
        TSCardStyleSpec.resolve(.init(isRaised: isRaised, isInteractive: isInteractive, state: controlState))
    }

    override public var canBecomeFocused: Bool {
        isInteractive && isEnabled
    }

    override public func didUpdateFocus(in context: UIFocusUpdateContext, with coordinator: UIFocusAnimationCoordinator) {
        super.didUpdateFocus(in: context, with: coordinator)
        applyStyle()
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyContent()
    }

    override public func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        guard let hit = super.hitTest(point, with: event) else { return nil }
        guard isInteractive else { return hit }
        let interactiveViews = footerButtons + [accessoryView].compactMap { $0 }
        if interactiveViews.contains(where: { hit.isDescendant(of: $0) }) {
            return hit
        }
        return self
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        let resolved = resolved
        if let shadow = resolved.shadow {
            TSShadow.apply(shadow, style: traitCollection.userInterfaceStyle, to: [layer], cornerRadius: resolved.cornerRadius)
        } else {
            TSShadow.apply(nil, to: layer, cornerRadius: resolved.cornerRadius)
        }
        if isInteractive {
            TSFocusRing.applyHalo(to: self, cornerRadius: resolved.cornerRadius)
        } else {
            focusEffect = nil
        }
    }

    func userDidActivate() {
        guard isInteractive, isEnabled else { return }
        sendActions(for: .primaryActionTriggered)
    }

    private static func makeAccessoryView(_ accessory: TSCardAccessory) -> UIView {
        switch accessory {
        case let .iconButton(icon, label, action):
            TSIconButtonView(icon: icon, label: label, primaryAction: UIAction { _ in
                MainActor.assumeIsolated { action() }
            })
        }
    }

    private func configureHierarchy() {
        let resolved = resolved
        surfaceView.translatesAutoresizingMaskIntoConstraints = false
        surfaceView.clipsToBounds = true
        surfaceView.isUserInteractionEnabled = true
        addSubview(surfaceView)

        contentStack.axis = .vertical
        contentStack.spacing = 0
        contentStack.translatesAutoresizingMaskIntoConstraints = false
        surfaceView.addSubview(contentStack)

        headerStack.axis = .horizontal
        headerStack.alignment = .top
        headerStack.spacing = resolved.headerSpacing
        headerStack.isLayoutMarginsRelativeArrangement = true
        headerStack.insetsLayoutMarginsFromSafeArea = false

        headingStack.axis = .vertical
        headingStack.alignment = .fill
        headingStack.spacing = resolved.subtitleSpacing
        [titleLabel, subtitleLabel].forEach(headingStack.addArrangedSubview)
        headerStack.addArrangedSubview(headingStack)
        titleLabel.numberOfLines = 0
        subtitleLabel.numberOfLines = 0
        [titleLabel, subtitleLabel].forEach {
            $0.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        }
        headingStack.setContentHuggingPriority(.defaultLow, for: .horizontal)
        headingStack.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)

        bodyStack.axis = .vertical
        bodyStack.alignment = .fill
        bodyStack.spacing = resolved.padding
        bodyStack.isLayoutMarginsRelativeArrangement = true
        bodyStack.insetsLayoutMarginsFromSafeArea = false
        bodyStack.directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: resolved.padding,
            leading: resolved.padding,
            bottom: resolved.padding,
            trailing: resolved.padding
        )
        bodyLabel.numberOfLines = 0
        bodyStack.addArrangedSubview(bodyLabel)

        footerDivider.translatesAutoresizingMaskIntoConstraints = false
        footerDivider.isAccessibilityElement = false
        footerStack.axis = .horizontal
        footerStack.alignment = .center
        footerStack.spacing = resolved.footerSpacing
        footerStack.translatesAutoresizingMaskIntoConstraints = false
        footerView.addSubview(footerDivider)
        footerView.addSubview(footerStack)

        [headerStack, bodyStack, footerView].forEach(contentStack.addArrangedSubview)

        NSLayoutConstraint.activate([
            surfaceView.topAnchor.constraint(equalTo: topAnchor),
            surfaceView.bottomAnchor.constraint(equalTo: bottomAnchor),
            surfaceView.leadingAnchor.constraint(equalTo: leadingAnchor),
            surfaceView.trailingAnchor.constraint(equalTo: trailingAnchor),
            contentStack.topAnchor.constraint(equalTo: surfaceView.topAnchor),
            contentStack.bottomAnchor.constraint(equalTo: surfaceView.bottomAnchor),
            contentStack.leadingAnchor.constraint(equalTo: surfaceView.leadingAnchor),
            contentStack.trailingAnchor.constraint(equalTo: surfaceView.trailingAnchor),
            footerDivider.topAnchor.constraint(equalTo: footerView.topAnchor),
            footerDivider.leadingAnchor.constraint(equalTo: footerView.leadingAnchor),
            footerDivider.trailingAnchor.constraint(equalTo: footerView.trailingAnchor),
            footerDivider.heightAnchor.constraint(equalToConstant: resolved.dividerThickness),
            footerStack.topAnchor.constraint(equalTo: footerDivider.bottomAnchor, constant: resolved.footerVerticalPadding),
            footerStack.bottomAnchor.constraint(equalTo: footerView.bottomAnchor, constant: -resolved.footerVerticalPadding),
            footerStack.trailingAnchor.constraint(equalTo: footerView.trailingAnchor, constant: -resolved.footerHorizontalPadding),
            footerStack.leadingAnchor.constraint(greaterThanOrEqualTo: footerView.leadingAnchor, constant: resolved.footerHorizontalPadding)
        ])
    }

    private func rebuildFooter() {
        footerButtons.forEach { $0.removeFromSuperview() }
        footerButtons = footerActions.map { action in
            let button = TSButtonView(title: action.label, variant: action.variant, primaryAction: UIAction { _ in action.handler() })
            button.setContentCompressionResistancePriority(.required, for: .horizontal)
            return button
        }
        footerButtons.forEach(footerStack.addArrangedSubview)
        applyContent()
    }

    private func applyContent() {
        let resolved = resolved
        let sizeCategory = traitCollection.preferredContentSizeCategory
        let alignment: NSTextAlignment = effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        let isAccessibilitySize = sizeCategory.isAccessibilityCategory

        titleLabel.isHidden = title == nil
        titleLabel.attributedText = title.map {
            resolved.titleFont.attributedString($0, sizeCategory: sizeCategory, color: resolved.titleColor.uiColor.resolvedColor(with: traitCollection), alignment: alignment)
        }
        subtitleLabel.isHidden = subtitle == nil
        subtitleLabel.attributedText = subtitle.map {
            resolved.subtitleFont.attributedString($0, sizeCategory: sizeCategory, color: resolved.subtitleColor.uiColor.resolvedColor(with: traitCollection), alignment: alignment)
        }
        if let accessoryView, accessoryView.superview !== headerStack {
            accessoryView.setContentHuggingPriority(.required, for: .horizontal)
            accessoryView.setContentCompressionResistancePriority(.required, for: .horizontal)
            headerStack.addArrangedSubview(accessoryView)
        }
        let hasHeader = title != nil || subtitle != nil || accessoryView != nil

        bodyLabel.isHidden = bodyText == nil
        bodyLabel.attributedText = bodyText.map {
            resolved.bodyFont.attributedString($0, sizeCategory: sizeCategory, color: resolved.bodyColor.uiColor.resolvedColor(with: traitCollection), alignment: alignment)
        }
        if let bodyView, bodyView.superview !== bodyStack {
            bodyStack.addArrangedSubview(bodyView)
        }
        let hasBody = bodyText != nil || bodyView != nil

        headerStack.isHidden = !hasHeader
        headerStack.directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: resolved.padding,
            leading: resolved.padding,
            bottom: hasBody ? 0 : resolved.padding,
            trailing: resolved.padding
        )
        bodyStack.isHidden = !hasBody
        footerView.isHidden = footerButtons.isEmpty
        footerStack.axis = isAccessibilitySize ? .vertical : .horizontal
        footerStack.alignment = isAccessibilitySize ? .trailing : .center
        applyStyle()
    }

    private func applyStyle() {
        let resolved = resolved
        surfaceView.backgroundColor = resolved.fill.uiColor.resolvedColor(with: traitCollection)
        surfaceView.tsApplyBorder(resolved.border, width: resolved.borderWidth, cornerRadius: resolved.cornerRadius)
        footerDivider.backgroundColor = resolved.divider.uiColor.resolvedColor(with: traitCollection)
        backgroundColor = .clear
        layer.cornerRadius = resolved.cornerRadius
        layer.cornerCurve = .continuous
        surfaceView.alpha = resolved.opacity
        footerButtons.forEach {
            $0.isEnabled = isEnabled
            $0.isDimmedByContainer = !isEnabled
        }
        accessoryView.flatMap { $0 as? UIControl }?.isEnabled = isEnabled
        (accessoryView as? TSIconButtonView)?.isDimmedByContainer = !isEnabled
        (accessoryView as? TSButtonView)?.isDimmedByContainer = !isEnabled
        isAccessibilityElement = false
        setNeedsLayout()
    }
}
