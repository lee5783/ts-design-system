import UIKit

public final class TSIconView: UIImageView {
    public var name: TSIconName {
        didSet { updateImage() }
    }

    public var size: TSIconSize {
        didSet { invalidateIntrinsicContentSize() }
    }

    public var label: String? {
        didSet { updateAccessibility() }
    }

    public init(_ name: TSIconName, size: TSIconSize = .md, label: String? = nil) {
        self.name = name
        self.size = size
        self.label = label
        super.init(frame: .zero)
        contentMode = .scaleAspectFit
        setContentHuggingPriority(.required, for: .horizontal)
        setContentHuggingPriority(.required, for: .vertical)
        setContentCompressionResistancePriority(.required, for: .horizontal)
        setContentCompressionResistancePriority(.required, for: .vertical)
        updateImage()
        updateAccessibility()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override public var intrinsicContentSize: CGSize {
        let pointSize = resolved.pointSize
        return CGSize(width: pointSize, height: pointSize)
    }

    private var resolved: TSIconStyleSpec.Resolved {
        TSIconStyleSpec.resolve(.init(size: size, hasLabel: label != nil))
    }

    private func updateImage() {
        image = name.image()?.withRenderingMode(.alwaysTemplate)
    }

    private func updateAccessibility() {
        isAccessibilityElement = resolved.isAccessibilityElement
        accessibilityLabel = label
        accessibilityTraits = resolved.isAccessibilityElement ? .image : []
    }
}
