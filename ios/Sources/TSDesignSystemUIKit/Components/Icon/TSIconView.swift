import UIKit

public final class TSIconView: UIImageView {
    public var name: TSIconName {
        didSet { updateImage() }
    }

    public var size: TSIconSize {
        didSet { invalidateIntrinsicContentSize() }
    }

    public init(_ name: TSIconName, size: TSIconSize = .md) {
        self.name = name
        self.size = size
        super.init(frame: .zero)
        contentMode = .scaleAspectFit
        setContentHuggingPriority(.required, for: .horizontal)
        setContentHuggingPriority(.required, for: .vertical)
        setContentCompressionResistancePriority(.required, for: .horizontal)
        setContentCompressionResistancePriority(.required, for: .vertical)
        updateImage()
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
        TSIconStyleSpec.resolve(.init(size: size))
    }

    private func updateImage() {
        image = name.image()?.withRenderingMode(.alwaysTemplate)
    }
}
