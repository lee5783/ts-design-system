import TSDesignSystemUIKit
import UIKit

final class TokenRowView: UIView {
    private let stackView: UIStackView

    init(name: String, detail: String, visual: UIView) {
        let labels = UIStackView(arrangedSubviews: [
            DemoLabel(name, font: .label, color: .textPrimary),
            DemoLabel(detail, font: .code, color: .textMuted)
        ])
        labels.axis = .vertical
        labels.spacing = TSSpacing.space0_5
        stackView = UIStackView(arrangedSubviews: [visual, labels])
        super.init(frame: .zero)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
        isAccessibilityElement = true
        accessibilityLabel = "\(name), \(detail)"
        accessibilityIdentifier = "demo.token.\(name)"
        updateAxis()
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if previousTraitCollection?.preferredContentSizeCategory != traitCollection.preferredContentSizeCategory {
            updateAxis()
        }
    }

    private func updateAxis() {
        let isStacked = traitCollection.preferredContentSizeCategory.isAccessibilityCategory
        stackView.axis = isStacked ? .vertical : .horizontal
        stackView.alignment = isStacked ? .leading : .center
        stackView.spacing = isStacked ? TSSpacing.space2 : TSSpacing.space3
    }
}
