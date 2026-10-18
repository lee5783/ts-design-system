import UIKit

public final class TSDividerView: UIView {
    private let lineView = UIView()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        let group = TSListItemStyleSpec.group
        isAccessibilityElement = false
        isUserInteractionEnabled = false
        lineView.backgroundColor = group.divider.uiColor
        lineView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(lineView)
        setContentHuggingPriority(.required, for: .vertical)
        setContentCompressionResistancePriority(.required, for: .vertical)
        NSLayoutConstraint.activate([
            lineView.topAnchor.constraint(equalTo: topAnchor),
            lineView.bottomAnchor.constraint(equalTo: bottomAnchor),
            lineView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: group.dividerInset),
            lineView.trailingAnchor.constraint(equalTo: trailingAnchor)
        ])
    }

    public convenience init() {
        self.init(frame: .zero)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override public var intrinsicContentSize: CGSize {
        CGSize(width: UIView.noIntrinsicMetric, height: TSListItemStyleSpec.group.dividerThickness)
    }
}
