import TSDesignSystemUIKit
import UIKit

final class TypographySampleUIView: UIView {
    private let stack = UIStackView()
    private var rows: [(font: TSFont, name: UILabel, sample: UILabel)] = []

    override init(frame: CGRect) {
        super.init(frame: frame)
        stack.axis = .vertical
        stack.alignment = .fill
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
        for font in TSFont.allFonts {
            let name = UILabel()
            let sample = UILabel()
            [name, sample].forEach { $0.numberOfLines = 0 }
            let row = UIStackView(arrangedSubviews: [name, sample])
            row.axis = .vertical
            row.spacing = TSSpacing.space1
            stack.addArrangedSubview(row)
            rows.append((font, name, sample))
        }
        applyText()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyText()
    }

    private func applyText() {
        let sizeCategory = traitCollection.preferredContentSizeCategory
        for row in rows {
            row.name.attributedText = TSFont.caption.attributedString(
                row.font.token.name,
                sizeCategory: sizeCategory,
                color: TSColor.textMuted.uiColor
            )
            row.sample.attributedText = row.font.attributedString(
                "Sphinx of black quartz",
                sizeCategory: sizeCategory,
                color: TSColor.textPrimary.uiColor
            )
        }
    }
}
