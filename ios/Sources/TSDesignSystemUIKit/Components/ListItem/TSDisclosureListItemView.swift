import UIKit

public final class TSDisclosureListItemView: UIControl {
    public var title: String {
        didSet { applyConfiguration() }
    }

    public var subtitle: String? {
        didSet { applyConfiguration() }
    }

    public var leading: TSListItemLeading {
        didSet { applyConfiguration() }
    }

    public var isExpanded: Bool {
        didSet {
            guard isExpanded != oldValue else { return }
            applyExpansion(animated: window != nil)
        }
    }

    public var bodyView: UIView? {
        didSet {
            oldValue?.removeFromSuperview()
            installBody()
        }
    }

    override public var accessibilityIdentifier: String? {
        didSet { headerView.accessibilityIdentifier = accessibilityIdentifier }
    }

    override public var isEnabled: Bool {
        didSet { applyConfiguration() }
    }

    override public var isHighlighted: Bool {
        didSet { applyConfiguration() }
    }

    let headerView: TSListItemContentView
    let bodyContainer = UIView()
    private let stackView = UIStackView()

    public init(
        title: String,
        subtitle: String? = nil,
        leading: TSListItemLeading = .none,
        bodyView: UIView? = nil,
        isExpanded: Bool = false
    ) {
        self.title = title
        self.subtitle = subtitle
        self.leading = leading
        self.bodyView = bodyView
        self.isExpanded = isExpanded
        headerView = TSListItemContentView(configuration: TSListItemConfiguration(title: title))
        super.init(frame: .zero)
        configureHierarchy()
        installBody()
        addAction(UIAction { [weak self] _ in self?.userDidToggle() }, for: .touchUpInside)
        applyConfiguration()
        applyExpansion(animated: false)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var headerConfiguration: TSListItemConfiguration {
        var configuration = TSListItemConfiguration(
            title: title,
            subtitle: subtitle,
            leading: leading,
            showsChevron: true,
            isInteractive: true,
            isEnabled: isEnabled
        )
        configuration.expansionState = isExpanded
        return configuration.updated(isHighlighted: isHighlighted, isFocused: isFocused)
    }

    var chevronRotationDegrees: Double {
        TSListItemStyleSpec.disclosure.rotationDegrees(
            isExpanded: isExpanded,
            isRightToLeft: effectiveUserInterfaceLayoutDirection == .rightToLeft
        )
    }

    override public var canBecomeFocused: Bool {
        isEnabled
    }

    override public func didUpdateFocus(in context: UIFocusUpdateContext, with coordinator: UIFocusAnimationCoordinator) {
        super.didUpdateFocus(in: context, with: coordinator)
        applyConfiguration()
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyChevronRotation()
    }

    override public func beginTracking(_ touch: UITouch, with event: UIEvent?) -> Bool {
        headerView.frame.contains(touch.location(in: self)) && super.beginTracking(touch, with: event)
    }

    override public func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        guard let hit = super.hitTest(point, with: event) else { return nil }
        if headerView.frame.contains(point) {
            return self
        }
        return hit === self ? nil : hit
    }

    func userDidToggle() {
        guard isEnabled else { return }
        isExpanded.toggle()
        sendActions(for: .valueChanged)
    }

    private func configureHierarchy() {
        stackView.axis = .vertical
        stackView.alignment = .fill
        stackView.spacing = 0
        stackView.translatesAutoresizingMaskIntoConstraints = false
        [headerView, bodyContainer].forEach(stackView.addArrangedSubview)
        addSubview(stackView)
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
        headerView.isUserInteractionEnabled = false
        headerView.activate = { [weak self] in
            guard let self, self.isEnabled else { return false }
            self.userDidToggle()
            return true
        }
        isAccessibilityElement = false
    }

    private func installBody() {
        guard let bodyView else { return }
        let disclosure = TSListItemStyleSpec.disclosure
        bodyView.translatesAutoresizingMaskIntoConstraints = false
        bodyContainer.addSubview(bodyView)
        NSLayoutConstraint.activate([
            bodyView.topAnchor.constraint(equalTo: bodyContainer.topAnchor),
            bodyView.bottomAnchor.constraint(equalTo: bodyContainer.bottomAnchor, constant: -disclosure.bodyBottomPadding),
            bodyView.leadingAnchor.constraint(equalTo: bodyContainer.leadingAnchor, constant: disclosure.bodyHorizontalPadding),
            bodyView.trailingAnchor.constraint(equalTo: bodyContainer.trailingAnchor, constant: -disclosure.bodyHorizontalPadding)
        ])
    }

    private func applyConfiguration() {
        let configuration = headerConfiguration
        headerView.configuration = configuration
        bodyContainer.alpha = configuration.isEnabled ? 1 : TSOpacity.opacityDisabled
    }

    private func applyExpansion(animated: Bool) {
        bodyContainer.isHidden = !isExpanded
        applyConfiguration()
        let disclosure = TSListItemStyleSpec.disclosure
        TSReduceMotion.animate(
            duration: disclosure.animationDuration,
            easing: disclosure.animationEasing,
            reduceMotion: !animated || TSReduceMotion.isEnabled
        ) { [weak self] in
            self?.applyChevronRotation()
        }
        if animated {
            UIAccessibility.post(notification: .layoutChanged, argument: nil)
        }
    }

    private func applyChevronRotation() {
        headerView.chevronView.transform = CGAffineTransform(rotationAngle: chevronRotationDegrees * .pi / 180)
    }
}
