import UIKit

public final class TSListItemView: UIControl {
    public var configuration: TSListItemConfiguration {
        didSet { applyConfiguration() }
    }

    override public var isEnabled: Bool {
        didSet { applyConfiguration() }
    }

    override public var isHighlighted: Bool {
        didSet { applyConfiguration() }
    }

    let itemContentView: TSListItemContentView

    public init(configuration: TSListItemConfiguration) {
        self.configuration = configuration
        itemContentView = TSListItemContentView(configuration: configuration)
        super.init(frame: .zero)
        configureHierarchy()
        addAction(UIAction { [weak self] _ in self?.userDidActivate() }, for: .touchUpInside)
        applyConfiguration()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var effectiveConfiguration: TSListItemConfiguration {
        var configuration = configuration
        configuration.isEnabled = configuration.isEnabled && isEnabled
        return configuration.updated(isHighlighted: isHighlighted, isFocused: isFocused)
    }

    override public var canBecomeFocused: Bool {
        configuration.role.isPressable && configuration.isEnabled && isEnabled
    }

    override public func didUpdateFocus(in context: UIFocusUpdateContext, with coordinator: UIFocusAnimationCoordinator) {
        super.didUpdateFocus(in: context, with: coordinator)
        applyConfiguration()
    }

    override public func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        guard let hit = super.hitTest(point, with: event) else { return nil }
        if hit.isDescendant(of: itemContentView.switchSlot) {
            return hit
        }
        return self
    }

    func userDidActivate() {
        let configuration = effectiveConfiguration
        guard configuration.isEnabled, configuration.role.isPressable else { return }
        sendActions(for: .primaryActionTriggered)
    }

    private func configureHierarchy() {
        itemContentView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(itemContentView)
        NSLayoutConstraint.activate([
            itemContentView.topAnchor.constraint(equalTo: topAnchor),
            itemContentView.bottomAnchor.constraint(equalTo: bottomAnchor),
            itemContentView.leadingAnchor.constraint(equalTo: leadingAnchor),
            itemContentView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
        itemContentView.toggleDidChange = { [weak self] isOn in
            self?.configuration.trailing = .toggle(isOn: isOn)
            self?.sendActions(for: .valueChanged)
        }
        itemContentView.accessibilityHost = self
    }

    private func applyConfiguration() {
        itemContentView.configuration = effectiveConfiguration
    }
}
