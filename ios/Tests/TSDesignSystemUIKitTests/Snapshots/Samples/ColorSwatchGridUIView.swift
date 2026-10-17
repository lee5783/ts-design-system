import TSDesignSystemUIKit
import UIKit

final class ColorSwatchGridUIView: UIView {
    static let columnCount = 3

    private let stack = UIStackView()
    private var swatches: [UIView] = []
    private var labels: [(token: TSColor, label: UILabel)] = []

    override init(frame: CGRect) {
        super.init(frame: frame)
        stack.axis = .vertical
        stack.spacing = TSSpacing.space3
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.isLayoutMarginsRelativeArrangement = true
        stack.directionalLayoutMargins = NSDirectionalEdgeInsets(
            top: TSSpacing.space4,
            leading: TSSpacing.space4,
            bottom: TSSpacing.space4,
            trailing: TSSpacing.space4
        )
        addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: topAnchor),
            stack.leadingAnchor.constraint(equalTo: leadingAnchor),
            stack.trailingAnchor.constraint(equalTo: trailingAnchor),
            stack.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
        let tokens = TSColor.allTokens
        for start in stride(from: 0, to: tokens.count, by: Self.columnCount) {
            let row = UIStackView()
            row.axis = .horizontal
            row.alignment = .top
            row.distribution = .fillEqually
            row.spacing = TSSpacing.space3
            for index in start..<(start + Self.columnCount) {
                row.addArrangedSubview(index < tokens.count ? makeCell(tokens[index]) : UIView())
            }
            stack.addArrangedSubview(row)
        }
        applyTraits()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyTraits()
    }

    private func makeCell(_ token: TSColor) -> UIView {
        let swatch = UIView()
        swatch.backgroundColor = token.uiColor
        swatch.layer.cornerRadius = TSRadius.radiusMd
        swatch.layer.borderWidth = 1
        swatch.heightAnchor.constraint(equalToConstant: 40).isActive = true
        swatches.append(swatch)
        let label = UILabel()
        label.numberOfLines = 0
        labels.append((token, label))
        let cell = UIStackView(arrangedSubviews: [swatch, label])
        cell.axis = .vertical
        cell.spacing = TSSpacing.space1
        return cell
    }

    private func applyTraits() {
        let border = TSColor.borderSubtle.uiColor.resolvedColor(with: traitCollection).cgColor
        swatches.forEach { $0.layer.borderColor = border }
        let sizeCategory = traitCollection.preferredContentSizeCategory
        for entry in labels {
            entry.label.attributedText = TSFont.caption.attributedString(
                entry.token.name,
                sizeCategory: sizeCategory,
                color: TSColor.textSecondary.uiColor
            )
        }
    }
}
