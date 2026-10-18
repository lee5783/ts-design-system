import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class DemoComponentSectionView: UIStackView {
    init(_ key: DemoStringKey, views: [UIView]) {
        let title = DemoLabel(DemoStrings.string(key), font: .overline, color: .textMuted)
        super.init(frame: .zero)
        axis = .vertical
        alignment = .leading
        spacing = TSSpacing.space3
        isLayoutMarginsRelativeArrangement = true
        directionalLayoutMargins = NSDirectionalEdgeInsets(top: 0, leading: 0, bottom: TSSpacing.space2, trailing: 0)
        ([title] + views).forEach(addArrangedSubview)
        title.widthAnchor.constraint(equalTo: widthAnchor).isActive = true
    }

    required init(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    func addFullWidth(_ view: UIView) {
        addArrangedSubview(view)
        view.widthAnchor.constraint(equalTo: layoutMarginsGuide.widthAnchor).isActive = true
    }
}

final class DemoComponentRowView: UIView {
    private let stackView: UIStackView
    private let isStacked: Bool

    init(_ views: [UIView], isStacked: Bool = false) {
        stackView = UIStackView(arrangedSubviews: views)
        self.isStacked = isStacked
        super.init(frame: .zero)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stackView)
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor)
        ])
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
        let isStacked = isStacked || traitCollection.preferredContentSizeCategory.isAccessibilityCategory
        stackView.axis = isStacked ? .vertical : .horizontal
        stackView.alignment = isStacked ? .leading : .center
        stackView.spacing = TSSpacing.space3
        stackView.arrangedSubviews.forEach {
            $0.setContentCompressionResistancePriority(isStacked ? .defaultHigh : .required, for: .horizontal)
        }
    }
}

@MainActor
enum DemoPlayground {
    static func caption(_ text: String) -> DemoLabel {
        DemoLabel(text, font: .code, color: .textSecondary)
    }

    static func counter(identifier: String) -> DemoLabel {
        let label = DemoLabel(DemoStrings.tapCount(0), font: .bodySm, color: .textSecondary)
        label.accessibilityIdentifier = identifier
        return label
    }

    static func segmented(_ items: [String], identifier: String? = nil, onChange: @escaping (Int) -> Void) -> UISegmentedControl {
        let control = UISegmentedControl(items: items)
        control.selectedSegmentIndex = 0
        control.accessibilityIdentifier = identifier
        control.addAction(UIAction { [unowned control] _ in onChange(control.selectedSegmentIndex) }, for: .valueChanged)
        return control
    }

    static func toggle(_ key: DemoStringKey, identifier: String? = nil, isOn: Bool = false, onChange: @escaping (Bool) -> Void) -> UIView {
        let control = UISwitch()
        control.isOn = isOn
        control.onTintColor = TSColor.accentPrimary.uiColor
        control.accessibilityIdentifier = identifier
        control.accessibilityLabel = DemoStrings.string(key)
        control.addAction(UIAction { [unowned control] _ in onChange(control.isOn) }, for: .valueChanged)
        let title = DemoLabel(DemoStrings.string(key), font: .body, color: .textPrimary)
        title.isAccessibilityElement = false
        let row = UIStackView(arrangedSubviews: [title, control])
        row.axis = .horizontal
        row.alignment = .center
        row.spacing = TSSpacing.space3
        return row
    }
}
