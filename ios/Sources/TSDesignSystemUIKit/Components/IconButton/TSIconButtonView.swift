import UIKit

public final class TSIconButtonView: UIButton {
    public var icon: TSIconName {
        didSet { setNeedsUpdateConfiguration() }
    }

    public var iconImage: UIImage? {
        didSet { setNeedsUpdateConfiguration() }
    }

    public var label: String {
        didSet { setNeedsUpdateConfiguration() }
    }

    public var variant: TSIconButtonVariant {
        didSet { setNeedsUpdateConfiguration() }
    }

    public var isRound: Bool {
        didSet { setNeedsUpdateConfiguration() }
    }

    var isDimmedByContainer = false {
        didSet { setNeedsUpdateConfiguration() }
    }

    public init(
        icon: TSIconName,
        label: String,
        variant: TSIconButtonVariant = .ghost,
        isRound: Bool = false,
        primaryAction: UIAction? = nil
    ) {
        self.icon = icon
        self.label = label
        self.variant = variant
        self.isRound = isRound
        super.init(frame: .zero)
        configuration = .plain()
        if let primaryAction {
            addAction(primaryAction, for: .primaryActionTriggered)
        }
        let size = TSIconButtonStyleSpec.resolve(.init()).size
        NSLayoutConstraint.activate([
            widthAnchor.constraint(equalToConstant: size),
            heightAnchor.constraint(equalToConstant: size)
        ])
        showsLargeContentViewer = true
        scalesLargeContentImage = true
        addInteraction(UILargeContentViewerInteraction())
        setNeedsUpdateConfiguration()
    }

    public convenience init(
        iconImage: UIImage,
        label: String,
        variant: TSIconButtonVariant = .ghost,
        isRound: Bool = false,
        primaryAction: UIAction? = nil
    ) {
        self.init(icon: .plus, label: label, variant: variant, isRound: isRound, primaryAction: primaryAction)
        self.iconImage = iconImage
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var controlState: TSControlState {
        .make(isPressed: isHighlighted, isEnabled: isEnabled, isFocused: isFocused)
    }

    var resolved: TSIconButtonStyleSpec.Resolved {
        TSIconButtonStyleSpec.resolve(.init(variant: variant, isRound: isRound, state: controlState))
    }

    override public var intrinsicContentSize: CGSize {
        let size = resolved.size
        return CGSize(width: size, height: size)
    }

    override public func updateConfiguration() {
        let resolved = resolved
        let iconColor = resolved.icon.uiColor.resolvedColor(with: traitCollection)
        var configuration = UIButton.Configuration.plain()
        configuration.image = iconImage?.tsTemplateIcon(pointSize: resolved.iconSize) ?? icon.templateImage(pointSize: resolved.iconSize)
        configuration.baseForegroundColor = iconColor
        configuration.imageColorTransformer = UIConfigurationColorTransformer { _ in iconColor }
        configuration.contentInsets = .zero
        configuration.background = .tsControl(fill: resolved.fill, cornerRadius: resolved.cornerRadius)
        configuration.cornerStyle = .fixed
        self.configuration = configuration
        tsApplyBorder(resolved.border, width: resolved.borderWidth, cornerRadius: resolved.cornerRadius)
        alpha = isDimmedByContainer ? 1 : resolved.opacity
        accessibilityLabel = label
        largeContentTitle = label
        largeContentImage = (iconImage ?? icon.image())?.withRenderingMode(.alwaysTemplate)
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        TSFocusRing.applyHalo(to: self, cornerRadius: resolved.cornerRadius)
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        setNeedsUpdateConfiguration()
    }

    override public var accessibilityTraits: UIAccessibilityTraits {
        get { super.accessibilityTraits.union(isEnabled ? .button : [.button, .notEnabled]) }
        set { super.accessibilityTraits = newValue }
    }

    override public func sendAction(_ action: UIAction) {
        guard isEnabled else { return }
        super.sendAction(action)
    }

    override public func sendAction(_ action: Selector, to target: Any?, for event: UIEvent?) {
        guard isEnabled else { return }
        super.sendAction(action, to: target, for: event)
    }
}
