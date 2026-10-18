import UIKit

public final class TSListItemCell: UICollectionViewListCell {
    public var item: TSListItemConfiguration? {
        didSet { setNeedsUpdateConfiguration() }
    }

    public internal(set) var groupPosition: TSListGroupPosition = .single {
        didSet {
            guard groupPosition != oldValue else { return }
            applyGroupPosition()
        }
    }

    let borderView = TSListGroupBorderView()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        clipsToBounds = true
        layer.cornerCurve = .continuous
        addSubview(borderView)
        applyGroupPosition()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override public func apply(_ layoutAttributes: UICollectionViewLayoutAttributes) {
        super.apply(layoutAttributes)
        let indexPath = layoutAttributes.indexPath
        let count = (superview as? UICollectionView)?.numberOfItems(inSection: indexPath.section) ?? 1
        groupPosition = TSListGroupPosition(index: indexPath.item, count: count)
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        borderView.frame = bounds
        bringSubviewToFront(borderView)
    }

    override public func updateConfiguration(using state: UICellConfigurationState) {
        var background = UIBackgroundConfiguration.clear()
        background.backgroundColor = TSListItemStyleSpec.group.fill.uiColor
        backgroundConfiguration = background
        guard let item else {
            contentConfiguration = nil
            return
        }
        var configuration = item.updated(for: state)
        let onToggle = configuration.onToggle
        configuration.onToggle = { [weak self] isOn in
            self?.item?.trailing = .toggle(isOn: isOn)
            onToggle?(isOn)
        }
        contentConfiguration = configuration
        itemContentView?.accessibilityHost = self
    }

    var itemContentView: TSListItemContentView? {
        contentView as? TSListItemContentView
    }

    override public func prepareForReuse() {
        super.prepareForReuse()
        groupPosition = .single
    }

    private func applyGroupPosition() {
        layer.cornerRadius = TSListItemStyleSpec.group.cornerRadius
        layer.maskedCorners = groupPosition.roundedCorners
        borderView.position = groupPosition
    }
}
