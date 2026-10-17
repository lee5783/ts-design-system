import UIKit

public final class TSListView: UIView {
    public var items: [UIView] {
        didSet { rebuild() }
    }

    let stackView = UIStackView()

    public init(items: [UIView] = []) {
        self.items = items
        super.init(frame: .zero)
        configureHierarchy()
        rebuild()
        applyStyle()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    private func configureHierarchy() {
        stackView.axis = .vertical
        stackView.alignment = .fill
        stackView.spacing = 0
        stackView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
        clipsToBounds = true
    }

    private func rebuild() {
        stackView.arrangedSubviews.forEach { $0.removeFromSuperview() }
        let group = TSListItemStyleSpec.group
        for (index, item) in items.enumerated() {
            if index > 0 {
                let divider = TSListDividerView()
                stackView.addArrangedSubview(divider)
                divider.heightAnchor.constraint(equalToConstant: group.dividerThickness).isActive = true
            }
            stackView.addArrangedSubview(item)
        }
    }

    private func applyStyle() {
        let group = TSListItemStyleSpec.group
        backgroundColor = group.fill.uiColor
        tsApplyBorder(group.border, width: group.borderWidth, cornerRadius: group.cornerRadius)
    }
}

final class TSListDividerView: UIView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = TSListItemStyleSpec.group.divider.uiColor
        isAccessibilityElement = false
        isUserInteractionEnabled = false
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }
}
