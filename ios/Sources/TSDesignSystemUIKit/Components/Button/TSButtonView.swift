import UIKit

public final class TSButtonView: UIButton {
    public var title: String {
        didSet { invalidateContent() }
    }

    public var variant: TSButtonVariant {
        didSet { invalidateContent() }
    }

    public var size: TSButtonSize {
        didSet { invalidateContent() }
    }

    public var leadingIcon: TSIconName? {
        didSet { invalidateContent() }
    }

    public var trailingIcon: TSIconName? {
        didSet { invalidateContent() }
    }

    public var leadingIconImage: UIImage? {
        didSet { invalidateContent() }
    }

    public var trailingIconImage: UIImage? {
        didSet { invalidateContent() }
    }

    public var numberOfLines = 0 {
        didSet { invalidateContent() }
    }

    public var lineBreakMode: NSLineBreakMode = .byWordWrapping {
        didSet { invalidateContent() }
    }

    public var isLoading = false {
        didSet {
            guard isLoading != oldValue else { return }
            isUserInteractionEnabled = !isLoading
            invalidateContent()
        }
    }

    public var isBlock = false {
        didSet { updateHugging() }
    }

    var isDimmedByContainer = false {
        didSet { setNeedsUpdateConfiguration() }
    }

    private let spinner = TSSpinnerView()
    private let trailingIconView = TSIconView(.chevronRight, size: .md)

    public init(
        title: String,
        variant: TSButtonVariant = .primary,
        size: TSButtonSize = .md,
        leadingIcon: TSIconName? = nil,
        trailingIcon: TSIconName? = nil,
        primaryAction: UIAction? = nil
    ) {
        self.title = title
        self.variant = variant
        self.size = size
        self.leadingIcon = leadingIcon
        self.trailingIcon = trailingIcon
        super.init(frame: .zero)
        configuration = .plain()
        if let primaryAction {
            addAction(primaryAction, for: .primaryActionTriggered)
        }
        trailingIconView.isUserInteractionEnabled = false
        addSubview(spinner)
        addSubview(trailingIconView)
        updateHugging()
        invalidateContent()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var controlState: TSControlState {
        .make(isPressed: isHighlighted, isEnabled: isEnabled, isLoading: isLoading, isFocused: isFocused)
    }

    var resolved: TSButtonStyleSpec.Resolved {
        TSButtonStyleSpec.resolve(.init(variant: variant, size: size, state: controlState))
    }

    override public func updateConfiguration() {
        let resolved = resolved
        let traits = traitCollection
        let labelColor = resolved.label.uiColor.resolvedColor(with: traits)
        let reservedTrailing = hasTrailingIcon ? resolved.iconSize + resolved.iconSpacing : 0
        var configuration = UIButton.Configuration.plain()
        configuration.attributedTitle = title(resolved: resolved, color: labelColor)
        configuration.titleAlignment = .center
        configuration.titleLineBreakMode = lineBreakMode
        configuration.baseForegroundColor = labelColor
        configuration.image = leadingImage(resolved: resolved)
        configuration.imageColorTransformer = UIConfigurationColorTransformer { _ in labelColor }
        configuration.imagePlacement = .leading
        configuration.imagePadding = resolved.iconSpacing
        configuration.contentInsets = NSDirectionalEdgeInsets(
            top: resolved.verticalPadding,
            leading: resolved.horizontalPadding,
            bottom: resolved.verticalPadding,
            trailing: resolved.horizontalPadding + reservedTrailing
        )
        configuration.background = .tsControl(fill: resolved.fill, cornerRadius: resolved.cornerRadius)
        configuration.cornerStyle = .fixed
        self.configuration = configuration
        tsApplyBorder(resolved.border, width: resolved.borderWidth, cornerRadius: resolved.cornerRadius)
        alpha = isDimmedByContainer ? 1 : resolved.opacity
        spinner.isHidden = !resolved.showsSpinner
        spinner.tintColor = labelColor
        spinner.lineWidth = resolved.spinnerStrokeWidth
        titleLabel?.numberOfLines = numberOfLines
        trailingIconView.isHidden = !hasTrailingIcon
        if let trailingIconImage {
            trailingIconView.image = trailingIconImage.tsTemplateIcon(pointSize: resolved.iconSize)
        } else if let trailingIcon {
            trailingIconView.name = trailingIcon
        }
        trailingIconView.tintColor = labelColor
        accessibilityLabel = title
        accessibilityValue = isLoading ? TSStrings.loading : nil
        setNeedsLayout()
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        let resolved = resolved
        if let imageView, !spinner.isHidden {
            let side = resolved.spinnerSize
            spinner.frame = CGRect(x: imageView.frame.midX - side / 2, y: imageView.frame.midY - side / 2, width: side, height: side)
        }
        if let titleLabel, !trailingIconView.isHidden {
            let side = resolved.iconSize
            let isRightToLeft = effectiveUserInterfaceLayoutDirection == .rightToLeft
            let x = isRightToLeft ? titleLabel.frame.minX - resolved.iconSpacing - side : titleLabel.frame.maxX + resolved.iconSpacing
            trailingIconView.frame = CGRect(x: x, y: bounds.midY - side / 2, width: side, height: side)
        }
        if let shadow = resolved.shadow {
            TSShadow.apply(shadow, style: traitCollection.userInterfaceStyle, to: [layer], cornerRadius: resolved.cornerRadius)
        } else {
            TSShadow.apply(nil, to: layer, cornerRadius: resolved.cornerRadius)
        }
        TSFocusRing.applyHalo(to: self, cornerRadius: resolved.cornerRadius)
    }

    override public var intrinsicContentSize: CGSize {
        let size = super.intrinsicContentSize
        return CGSize(width: size.width, height: max(size.height, resolved.minHeight))
    }

    override public func sizeThatFits(_ size: CGSize) -> CGSize {
        let fitting = super.sizeThatFits(size)
        return CGSize(width: fitting.width, height: max(fitting.height, resolved.minHeight))
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        setNeedsUpdateConfiguration()
    }

    override public func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        TSHitArea.expandedRect(bounds, minimum: resolved.minimumHitSize).contains(point)
    }

    override public var accessibilityTraits: UIAccessibilityTraits {
        get { super.accessibilityTraits.union(isEnabled && !isLoading ? .button : [.button, .notEnabled]) }
        set { super.accessibilityTraits = newValue }
    }

    override public func sendAction(_ action: UIAction) {
        guard isEnabled, !isLoading else { return }
        super.sendAction(action)
    }

    override public func sendAction(_ action: Selector, to target: Any?, for event: UIEvent?) {
        guard isEnabled, !isLoading else { return }
        super.sendAction(action, to: target, for: event)
    }

    private var hasTrailingIcon: Bool {
        trailingIcon != nil || trailingIconImage != nil
    }

    private func invalidateContent() {
        setNeedsUpdateConfiguration()
        invalidateIntrinsicContentSize()
    }

    private func updateHugging() {
        setContentHuggingPriority(isBlock ? .defaultLow - 1 : .required, for: .horizontal)
    }

    private func title(resolved: TSButtonStyleSpec.Resolved, color: UIColor) -> AttributedString {
        var attributes = resolved.font.attributes(for: traitCollection.preferredContentSizeCategory, color: color, alignment: .center)
        if resolved.isUnderlined {
            attributes[.underlineStyle] = NSUnderlineStyle.single.rawValue
        }
        if lineBreakMode != .byWordWrapping, let paragraph = (attributes[.paragraphStyle] as? NSParagraphStyle)?.mutableCopy() as? NSMutableParagraphStyle {
            paragraph.lineBreakMode = lineBreakMode
            attributes[.paragraphStyle] = paragraph
        }
        return AttributedString(NSAttributedString(string: resolved.font.text(title), attributes: attributes))
    }

    private func leadingImage(resolved: TSButtonStyleSpec.Resolved) -> UIImage? {
        if resolved.showsSpinner {
            return UIGraphicsImageRenderer(size: CGSize(width: resolved.iconSize, height: resolved.iconSize)).image { _ in }
        }
        if let leadingIconImage {
            return leadingIconImage.tsTemplateIcon(pointSize: resolved.iconSize)
        }
        return leadingIcon?.templateImage(pointSize: resolved.iconSize)
    }
}
