import TSDesignSystemUIKit
import UIKit

final class DemoGridView: UIView {
    private let items: [UIView]
    private let minimumItemWidth: CGFloat
    private let accessibilityMinimumItemWidth: CGFloat
    private let columnSpacing: CGFloat
    private let rowsStackView = UIStackView()
    private var columnCount = 0

    init(
        items: [UIView],
        minimumItemWidth: CGFloat,
        accessibilityMinimumItemWidth: CGFloat = 280,
        columnSpacing: CGFloat,
        rowSpacing: CGFloat
    ) {
        self.items = items
        self.minimumItemWidth = minimumItemWidth
        self.accessibilityMinimumItemWidth = accessibilityMinimumItemWidth
        self.columnSpacing = columnSpacing
        super.init(frame: .zero)
        rowsStackView.axis = .vertical
        rowsStackView.spacing = rowSpacing
        rowsStackView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(rowsStackView)
        NSLayoutConstraint.activate([
            rowsStackView.topAnchor.constraint(equalTo: topAnchor),
            rowsStackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            rowsStackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            rowsStackView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        let itemWidth = traitCollection.preferredContentSizeCategory.isAccessibilityCategory ? accessibilityMinimumItemWidth : minimumItemWidth
        let columns = max(1, Int((bounds.width + columnSpacing) / (itemWidth + columnSpacing)))
        guard columns != columnCount else { return }
        columnCount = columns
        rebuildRows()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if previousTraitCollection?.preferredContentSizeCategory != traitCollection.preferredContentSizeCategory {
            setNeedsLayout()
        }
    }

    private func rebuildRows() {
        rowsStackView.arrangedSubviews.forEach { $0.removeFromSuperview() }
        for start in stride(from: 0, to: items.count, by: columnCount) {
            let rowItems = Array(items[start..<min(start + columnCount, items.count)])
            let fillers = (rowItems.count..<columnCount).map { _ in UIView() }
            let row = UIStackView(arrangedSubviews: rowItems + fillers)
            row.axis = .horizontal
            row.distribution = .fillEqually
            row.alignment = .fill
            row.spacing = columnSpacing
            rowsStackView.addArrangedSubview(row)
        }
    }
}
