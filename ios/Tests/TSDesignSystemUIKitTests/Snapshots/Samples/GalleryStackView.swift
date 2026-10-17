import TSDesignSystemUIKit
import UIKit

class GalleryStackView: UIView {
    let stack = UIStackView()

    override init(frame: CGRect) {
        super.init(frame: frame)
        stack.axis = .vertical
        stack.alignment = .leading
        stack.spacing = TSSpacing.space3
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.isLayoutMarginsRelativeArrangement = true
        stack.insetsLayoutMarginsFromSafeArea = false
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
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    func addRow(_ views: [UIView], spacing: CGFloat = TSSpacing.space4) {
        let row = UIStackView(arrangedSubviews: views)
        row.axis = .horizontal
        row.alignment = .center
        row.spacing = spacing
        stack.addArrangedSubview(row)
    }
}
