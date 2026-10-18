import UIKit

public final class TSBottomNavView: UIControl {
    public var items: [TSBottomNavItem] {
        didSet { applyItems(oldValue: oldValue) }
    }

    public var selectedID: String {
        didSet { applySelection() }
    }

    override public var isEnabled: Bool {
        didSet { applyStyle() }
    }

    let stackView = UIStackView()
    let hairlineView = UIView()
    private(set) var itemViews: [TSBottomNavItemView] = []

    var reduceMotion: @MainActor () -> Bool = { false }

    private var hairlineHeight: NSLayoutConstraint?

    public init(items: [TSBottomNavItem], selectedID: String? = nil) {
        self.items = items
        self.selectedID = selectedID ?? items.first?.id ?? ""
        super.init(frame: .zero)
        configureHierarchy()
        rebuildItems()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var selectedItemView: TSBottomNavItemView? {
        itemViews.first { $0.item.id == selectedID }
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    func userDidSelect(_ itemView: TSBottomNavItemView) {
        guard isEnabled, itemView.item.id != selectedID else { return }
        TSAnimation.animate(isInstant: reduceMotion()) {
            self.selectedID = itemView.item.id
        }
        sendActions(for: .valueChanged)
    }

    private func configureHierarchy() {
        isAccessibilityElement = false
        addInteraction(UILargeContentViewerInteraction())

        let container = TSBottomNavStyleSpec.container
        [stackView, hairlineView].forEach { $0.translatesAutoresizingMaskIntoConstraints = false }
        hairlineView.isUserInteractionEnabled = false
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.distribution = .fillEqually
        stackView.spacing = 0
        addSubview(stackView)
        addSubview(hairlineView)

        let hairline = hairlineView.heightAnchor.constraint(equalToConstant: container.borderWidth)
        hairlineHeight = hairline
        NSLayoutConstraint.activate([
            hairlineView.topAnchor.constraint(equalTo: topAnchor),
            hairlineView.leadingAnchor.constraint(equalTo: leadingAnchor),
            hairlineView.trailingAnchor.constraint(equalTo: trailingAnchor),
            hairline,
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.leadingAnchor.constraint(equalTo: safeAreaLayoutGuide.leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: safeAreaLayoutGuide.trailingAnchor),
            stackView.heightAnchor.constraint(equalToConstant: container.height),
            stackView.bottomAnchor.constraint(equalTo: safeAreaLayoutGuide.bottomAnchor)
        ])
    }

    private func applyItems(oldValue: [TSBottomNavItem]) {
        guard oldValue.map(\.id) == items.map(\.id) else {
            rebuildItems()
            return
        }
        zip(itemViews, items).forEach { $0.item = $1 }
    }

    private func rebuildItems() {
        itemViews.forEach { $0.removeFromSuperview() }
        itemViews = items.map { item in
            let itemView = TSBottomNavItemView(item: item)
            itemView.onSelect = { [weak self] in self?.userDidSelect($0) }
            stackView.addArrangedSubview(itemView)
            return itemView
        }
        if !items.contains(where: { $0.id == selectedID }), let first = items.first {
            selectedID = first.id
        }
        applyStyle()
    }

    private func applySelection() {
        itemViews.forEach { $0.isSelected = $0.item.id == selectedID }
    }

    private func applyStyle() {
        let container = TSBottomNavStyleSpec.container
        let traits = traitCollection
        backgroundColor = container.fill.uiColor.resolvedColor(with: traits)
        hairlineView.backgroundColor = container.border.uiColor.resolvedColor(with: traits)
        hairlineHeight?.constant = container.borderWidth
        itemViews.forEach {
            $0.isEnabled = isEnabled
            $0.isSelected = $0.item.id == selectedID
        }
    }
}
