import UIKit

public final class TSListSectionHeaderView: UICollectionReusableView {
    public var title: String = "" {
        didSet { applyStyle() }
    }

    let titleLabel = UILabel()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        configureHierarchy()
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
        let group = TSListItemStyleSpec.group
        titleLabel.numberOfLines = 0
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        addSubview(titleLabel)
        let bottom = titleLabel.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -group.headerSpacing)
        bottom.priority = .required - 1
        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: topAnchor),
            titleLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: group.headerHorizontalPadding),
            titleLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -group.headerHorizontalPadding),
            bottom
        ])
        isAccessibilityElement = true
        accessibilityTraits = .header
    }

    private func applyStyle() {
        let group = TSListItemStyleSpec.group
        titleLabel.attributedText = group.headerFont.attributedString(
            title,
            sizeCategory: traitCollection.preferredContentSizeCategory,
            color: group.headerColor.uiColor.resolvedColor(with: traitCollection),
            alignment: effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        )
        accessibilityLabel = title
    }
}
