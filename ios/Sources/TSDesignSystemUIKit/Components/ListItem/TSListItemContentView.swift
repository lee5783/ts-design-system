import UIKit

public final class TSListItemContentView: UIView, UIContentView {
    public var configuration: UIContentConfiguration {
        get { itemConfiguration }
        set {
            guard let newValue = newValue as? TSListItemConfiguration else { return }
            itemConfiguration = newValue
        }
    }

    public private(set) var itemConfiguration: TSListItemConfiguration {
        didSet { applyStyle() }
    }

    var togglePath: TSCompatPath = TSCompat.toggleTrait {
        didSet { applyStyle() }
    }

    weak var accessibilityHost: UIView? {
        didSet { applyStyle() }
    }

    var activate: (@MainActor () -> Bool)?
    var toggleDidChange: (@MainActor (Bool) -> Void)?

    let rowStack = UIStackView()
    let bodyStack = UIStackView()
    let textStack = UIStackView()
    let tileView = TSListItemTileView()
    let avatarView = TSAvatarView(name: "")
    let titleLabel = UILabel()
    let subtitleLabel = UILabel()
    let trailingLabel = UILabel()
    let badgeView = TSBadgeView(text: "")
    let switchSlot = TSListItemSwitchSlot()
    let chevronView = TSIconView(.chevronRight)

    private var stackedTextWidth: NSLayoutConstraint?

    public init(configuration: TSListItemConfiguration) {
        itemConfiguration = configuration
        super.init(frame: .zero)
        configureHierarchy()
        applyStyle()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var layout: TSListItemLayout {
        .make(isAccessibilitySize: traitCollection.preferredContentSizeCategory.isAccessibilityCategory)
    }

    var resolved: TSListItemStyleSpec.Resolved {
        let configuration = itemConfiguration
        return TSListItemStyleSpec.resolve(.init(
            hasSubtitle: configuration.subtitle != nil,
            isInteractive: configuration.role.isPressable,
            layout: layout,
            state: configuration.controlState
        ))
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    override public func accessibilityActivate() -> Bool {
        performAccessibilityActivation()
    }

    func performAccessibilityActivation() -> Bool {
        let configuration = itemConfiguration
        guard configuration.isEnabled else { return false }
        switch configuration.role {
        case .toggle:
            switchSlot.switchView.userDidToggle()
            return true
        case .action, .navigation:
            return activate?() ?? false
        case .plain:
            return false
        }
    }

    private func configureHierarchy() {
        let resolved = resolved
        rowStack.axis = .horizontal
        rowStack.alignment = .center
        rowStack.translatesAutoresizingMaskIntoConstraints = false
        textStack.axis = .vertical
        textStack.alignment = .fill
        textStack.spacing = 0
        [titleLabel, subtitleLabel].forEach(textStack.addArrangedSubview)
        [tileView, avatarView, textStack, trailingLabel, badgeView, switchSlot].forEach(bodyStack.addArrangedSubview)
        [bodyStack, chevronView].forEach(rowStack.addArrangedSubview)
        addSubview(rowStack)

        textStack.setContentHuggingPriority(.defaultLow, for: .horizontal)
        titleLabel.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        subtitleLabel.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        [titleLabel, subtitleLabel, trailingLabel].forEach {
            $0.setContentCompressionResistancePriority(.required, for: .vertical)
        }
        trailingLabel.setContentHuggingPriority(.required, for: .horizontal)
        trailingLabel.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        badgeView.isAccessibilityElement = false
        avatarView.isAccessibilityElement = false
        switchSlot.switchView.addAction(UIAction { [weak self] _ in self?.switchDidChange() }, for: .valueChanged)

        let bottom = rowStack.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -resolved.verticalPadding)
        bottom.priority = .required - 1
        let minHeight = heightAnchor.constraint(greaterThanOrEqualToConstant: resolved.minHeight)
        minHeight.priority = .required - 1
        stackedTextWidth = textStack.widthAnchor.constraint(equalTo: bodyStack.widthAnchor)
        NSLayoutConstraint.activate([
            rowStack.topAnchor.constraint(greaterThanOrEqualTo: topAnchor, constant: resolved.verticalPadding),
            rowStack.centerYAnchor.constraint(equalTo: centerYAnchor),
            rowStack.leadingAnchor.constraint(equalTo: leadingAnchor, constant: resolved.horizontalPadding),
            rowStack.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -resolved.horizontalPadding),
            bottom,
            minHeight
        ])
    }

    private func applyStyle() {
        let configuration = itemConfiguration
        let resolved = resolved
        let traits = traitCollection

        applyLeading(configuration.leading)
        applyText(configuration, resolved: resolved)
        let hasTrailing = applyTrailing(configuration, resolved: resolved)

        chevronView.isHidden = !configuration.showsChevron
        chevronView.name = resolved.chevron
        chevronView.size = resolved.chevronSize
        chevronView.tintColor = resolved.chevronColor.uiColor

        bodyStack.axis = resolved.isStacked ? .vertical : .horizontal
        bodyStack.alignment = resolved.isStacked ? .leading : .center
        bodyStack.spacing = resolved.isStacked ? resolved.stackedSpacing : resolved.spacing
        rowStack.spacing = !resolved.isStacked && hasTrailing ? resolved.trailingSpacing : resolved.spacing
        stackedTextWidth?.isActive = resolved.isStacked

        backgroundColor = resolved.background?.uiColor.resolvedColor(with: traits) ?? .clear
        alpha = resolved.opacity

        applyAccessibility(configuration)
    }

    private func applyAccessibility(_ configuration: TSListItemConfiguration) {
        let element = accessibilityHost ?? self
        let role = configuration.role
        isAccessibilityElement = accessibilityHost == nil
        element.isAccessibilityElement = true
        element.accessibilityLabel = TSListItemAccessibility.label(title: configuration.title, subtitle: configuration.subtitle)
        element.accessibilityHint = TSListItemAccessibility.hint(for: role)
        element.accessibilityTraits = role.isPressable ? .button : []
        element.accessibilityValue = trailingAccessibilityValue(configuration.trailing)
        if let isOn = configuration.trailing.isOn {
            TSAccessibilityTraitsCompat.apply(to: element, isOn: isOn, kind: .switch, path: togglePath)
        }
        if !configuration.isEnabled {
            element.accessibilityTraits.insert(.notEnabled)
        }
    }

    private func applyLeading(_ leading: TSListItemLeading) {
        switch leading {
        case .none:
            tileView.isHidden = true
            avatarView.isHidden = true
        case let .icon(name):
            tileView.isHidden = false
            avatarView.isHidden = true
            tileView.iconView.name = name
        case let .avatar(name, image):
            tileView.isHidden = true
            avatarView.isHidden = false
            avatarView.name = name
            avatarView.image = image
        }
    }

    private func applyText(_ configuration: TSListItemConfiguration, resolved: TSListItemStyleSpec.Resolved) {
        let sizeCategory = traitCollection.preferredContentSizeCategory
        let alignment: NSTextAlignment = effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        titleLabel.numberOfLines = resolved.titleLineLimit ?? 0
        titleLabel.attributedText = resolved.titleFont.truncatingAttributedString(
            configuration.title,
            sizeCategory: sizeCategory,
            color: resolved.titleColor.uiColor.resolvedColor(with: traitCollection),
            alignment: alignment,
            isSingleLine: resolved.titleLineLimit == 1
        )
        subtitleLabel.isHidden = !resolved.showsSubtitle
        subtitleLabel.numberOfLines = resolved.subtitleLineLimit ?? 0
        subtitleLabel.attributedText = resolved.subtitleFont.truncatingAttributedString(
            configuration.subtitle ?? "",
            sizeCategory: sizeCategory,
            color: resolved.subtitleColor.uiColor.resolvedColor(with: traitCollection),
            alignment: alignment,
            isSingleLine: resolved.subtitleLineLimit == 1
        )
    }

    private func applyTrailing(_ configuration: TSListItemConfiguration, resolved: TSListItemStyleSpec.Resolved) -> Bool {
        trailingLabel.isHidden = true
        badgeView.isHidden = true
        switchSlot.isHidden = true
        switch configuration.trailing {
        case .none:
            return false
        case let .text(text):
            trailingLabel.isHidden = false
            trailingLabel.numberOfLines = resolved.isStacked ? 0 : 1
            trailingLabel.attributedText = resolved.trailingFont.truncatingAttributedString(
                text,
                sizeCategory: traitCollection.preferredContentSizeCategory,
                color: resolved.trailingColor.uiColor.resolvedColor(with: traitCollection),
                isSingleLine: !resolved.isStacked
            )
        case let .badge(text, tone):
            badgeView.isHidden = false
            badgeView.text = text
            badgeView.tone = tone
        case let .toggle(isOn):
            switchSlot.isHidden = false
            let switchView = switchSlot.switchView
            switchView.label = configuration.title
            switchView.isEnabled = configuration.isEnabled
            if switchView.isOn != isOn {
                switchView.isOn = isOn
            }
        }
        return true
    }

    private func trailingAccessibilityValue(_ trailing: TSListItemTrailing) -> String? {
        switch trailing {
        case .none, .toggle: nil
        case let .text(text): text
        case let .badge(text, _): text
        }
    }

    private func switchDidChange() {
        let isOn = switchSlot.switchView.isOn
        itemConfiguration.trailing = .toggle(isOn: isOn)
        toggleDidChange?(isOn)
        itemConfiguration.onToggle?(isOn)
    }
}

final class TSListItemTileView: UIView {
    let iconView = TSIconView(.info)

    override init(frame: CGRect) {
        super.init(frame: frame)
        let tile = TSListItemStyleSpec.tile
        backgroundColor = tile.fill.uiColor
        layer.cornerRadius = tile.cornerRadius
        layer.cornerCurve = .continuous
        iconView.size = tile.iconSize
        iconView.tintColor = tile.foreground.uiColor
        iconView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(iconView)
        setContentHuggingPriority(.required, for: .horizontal)
        setContentHuggingPriority(.required, for: .vertical)
        setContentCompressionResistancePriority(.required, for: .horizontal)
        setContentCompressionResistancePriority(.required, for: .vertical)
        NSLayoutConstraint.activate([
            iconView.centerXAnchor.constraint(equalTo: centerXAnchor),
            iconView.centerYAnchor.constraint(equalTo: centerYAnchor)
        ])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override var intrinsicContentSize: CGSize {
        let size = TSListItemStyleSpec.tile.size
        return CGSize(width: size, height: size)
    }
}

final class TSListItemSwitchSlot: UIView {
    let switchView = TSSwitchView(label: "", isLabelHidden: true)

    override init(frame: CGRect) {
        super.init(frame: frame)
        let resolved = switchView.resolved
        switchView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(switchView)
        setContentHuggingPriority(.required, for: .horizontal)
        setContentCompressionResistancePriority(.required, for: .horizontal)
        NSLayoutConstraint.activate([
            heightAnchor.constraint(equalToConstant: resolved.trackHeight),
            switchView.centerYAnchor.constraint(equalTo: centerYAnchor),
            switchView.leadingAnchor.constraint(equalTo: leadingAnchor),
            switchView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        bounds.union(switchView.frame).contains(point)
    }
}
