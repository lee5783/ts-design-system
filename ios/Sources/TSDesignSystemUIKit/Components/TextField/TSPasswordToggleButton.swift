import UIKit

final class TSPasswordToggleButton: UIButton {
    var isRevealed = false {
        didSet {
            accessibilityLabel = TSTextFieldAccessibility.toggleLabel(isRevealed: isRevealed)
            setNeedsUpdateConfiguration()
        }
    }

    init() {
        super.init(frame: .zero)
        configuration = .plain()
        let size = TSTextFieldStyleSpec.passwordToggle(isRevealed: false).size
        setContentHuggingPriority(.required, for: .horizontal)
        setContentCompressionResistancePriority(.required, for: .horizontal)
        NSLayoutConstraint.activate([
            widthAnchor.constraint(equalToConstant: size),
            heightAnchor.constraint(equalToConstant: size)
        ])
        accessibilityLabel = TSTextFieldAccessibility.toggleLabel(isRevealed: isRevealed)
        setNeedsUpdateConfiguration()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var resolved: TSTextFieldStyleSpec.PasswordToggle {
        TSTextFieldStyleSpec.passwordToggle(isRevealed: isRevealed)
    }

    override func updateConfiguration() {
        let resolved = resolved
        var configuration = UIButton.Configuration.plain()
        let iconColor = resolved.iconColor.uiColor.resolvedColor(with: traitCollection)
        configuration.image = resolved.icon.templateImage(pointSize: resolved.iconSize.pointSize)
        configuration.baseForegroundColor = iconColor
        configuration.imageColorTransformer = UIConfigurationColorTransformer { _ in iconColor }
        configuration.contentInsets = .zero
        configuration.background = .tsControl(fill: resolved.fill, cornerRadius: resolved.cornerRadius)
        configuration.cornerStyle = .fixed
        self.configuration = configuration
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        setNeedsUpdateConfiguration()
    }

    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        TSHitArea.expandedRect(bounds, minimum: resolved.minimumHitSize).contains(point)
    }

    override var accessibilityTraits: UIAccessibilityTraits {
        get { super.accessibilityTraits.union(isEnabled ? .button : [.button, .notEnabled]) }
        set { super.accessibilityTraits = newValue }
    }
}
