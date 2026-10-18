import UIKit

public final class TSBadgeView: UIView {
    public var text: String {
        didSet { applyStyle() }
    }

    public var tone: TSBadgeTone {
        didSet { applyStyle() }
    }

    public var showsDot: Bool {
        didSet { applyStyle() }
    }

    public var icon: TSIconName? {
        didSet { applyStyle() }
    }

    let stackView = UIStackView()
    let dotView = UIView()
    let iconView = TSIconView(.info, size: .sm)
    let textLabel = UILabel()

    public init(text: String, tone: TSBadgeTone = .neutral, showsDot: Bool = false, icon: TSIconName? = nil) {
        self.text = text
        self.tone = tone
        self.showsDot = showsDot
        self.icon = icon
        super.init(frame: .zero)
        configureHierarchy()
        applyStyle()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var resolved: TSBadgeStyleSpec.Resolved {
        TSBadgeStyleSpec.resolve(.init(tone: tone, hasDot: showsDot, hasIcon: icon != nil))
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    private func configureHierarchy() {
        let resolved = resolved
        stackView.axis = .horizontal
        stackView.alignment = .center
        stackView.spacing = resolved.spacing
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.isUserInteractionEnabled = false
        [dotView, iconView, textLabel].forEach(stackView.addArrangedSubview)
        addSubview(stackView)
        dotView.layer.cornerRadius = resolved.dotSize / 2
        textLabel.numberOfLines = 1
        textLabel.lineBreakMode = .byTruncatingTail
        textLabel.setContentHuggingPriority(.required, for: .horizontal)
        textLabel.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        setContentHuggingPriority(.required, for: .horizontal)
        setContentHuggingPriority(.required, for: .vertical)
        NSLayoutConstraint.activate([
            dotView.widthAnchor.constraint(equalToConstant: resolved.dotSize),
            dotView.heightAnchor.constraint(equalToConstant: resolved.dotSize),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: resolved.horizontalPadding),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -resolved.horizontalPadding),
            stackView.topAnchor.constraint(greaterThanOrEqualTo: topAnchor, constant: resolved.verticalPadding),
            stackView.bottomAnchor.constraint(lessThanOrEqualTo: bottomAnchor, constant: -resolved.verticalPadding),
            stackView.centerYAnchor.constraint(equalTo: centerYAnchor),
            heightAnchor.constraint(greaterThanOrEqualToConstant: resolved.minHeight)
        ])
        let fitsContent = heightAnchor.constraint(equalToConstant: resolved.minHeight)
        fitsContent.priority = .defaultLow
        fitsContent.isActive = true
        isAccessibilityElement = true
    }

    private func applyStyle() {
        let resolved = resolved
        let foreground = resolved.foreground.uiColor.resolvedColor(with: traitCollection)
        backgroundColor = resolved.fill.uiColor.resolvedColor(with: traitCollection)
        tsApplyBorder(resolved.border, width: resolved.borderWidth, cornerRadius: resolved.cornerRadius)
        dotView.isHidden = !resolved.showsDot
        dotView.backgroundColor = foreground
        iconView.isHidden = !resolved.showsIcon
        if let icon {
            iconView.name = icon
        }
        iconView.size = resolved.iconSize
        iconView.tintColor = foreground
        textLabel.attributedText = truncatingText(resolved.font, color: foreground)
    }

    private func truncatingText(_ font: TSFont, color: UIColor) -> NSAttributedString {
        let sizeCategory = traitCollection.preferredContentSizeCategory
        var attributes = font.attributes(for: sizeCategory, color: color)
        let paragraphStyle = font.paragraphStyle(for: sizeCategory).mutableCopy() as? NSMutableParagraphStyle
        paragraphStyle?.lineBreakMode = .byTruncatingTail
        attributes[.paragraphStyle] = paragraphStyle
        return NSAttributedString(string: font.text(text), attributes: attributes)
    }
}
