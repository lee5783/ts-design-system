import UIKit

public final class TSAvatarView: UIView {
    public var name: String {
        didSet { applyStyle() }
    }

    public var image: UIImage? {
        didSet { applyStyle() }
    }

    public var size: TSAvatarSize {
        didSet {
            invalidateIntrinsicContentSize()
            applyStyle()
        }
    }

    let imageView = UIImageView()
    let initialsLabel = UILabel()
    let placeholderView = TSIconView(.user)

    public init(name: String, image: UIImage? = nil, size: TSAvatarSize = .md) {
        self.name = name
        self.image = image
        self.size = size
        super.init(frame: .zero)
        configureHierarchy()
        applyStyle()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var initials: String {
        TSInitials.initials(for: name)
    }

    var content: TSAvatarContent {
        .make(initials: initials, hasImage: TSAvatarImage.usable(image) != nil)
    }

    var resolved: TSAvatarStyleSpec.Resolved {
        TSAvatarStyleSpec.resolve(.init(size: size, content: content))
    }

    override public var intrinsicContentSize: CGSize {
        let diameter = resolved.diameter
        return CGSize(width: diameter, height: diameter)
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = min(bounds.width, bounds.height) / 2
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    private func configureHierarchy() {
        clipsToBounds = true
        setContentHuggingPriority(.required, for: .horizontal)
        setContentHuggingPriority(.required, for: .vertical)
        setContentCompressionResistancePriority(.required, for: .horizontal)
        setContentCompressionResistancePriority(.required, for: .vertical)
        imageView.contentMode = .scaleAspectFill
        initialsLabel.textAlignment = .center
        initialsLabel.numberOfLines = 1
        initialsLabel.adjustsFontSizeToFitWidth = true
        initialsLabel.adjustsFontForContentSizeCategory = false
        [imageView, initialsLabel, placeholderView].forEach {
            $0.translatesAutoresizingMaskIntoConstraints = false
            addSubview($0)
        }
        let inset = resolved.contentInset
        NSLayoutConstraint.activate([
            imageView.topAnchor.constraint(equalTo: topAnchor),
            imageView.bottomAnchor.constraint(equalTo: bottomAnchor),
            imageView.leadingAnchor.constraint(equalTo: leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: trailingAnchor),
            initialsLabel.leadingAnchor.constraint(equalTo: leadingAnchor, constant: inset),
            initialsLabel.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -inset),
            initialsLabel.centerYAnchor.constraint(equalTo: centerYAnchor),
            placeholderView.centerXAnchor.constraint(equalTo: centerXAnchor),
            placeholderView.centerYAnchor.constraint(equalTo: centerYAnchor)
        ])
        isAccessibilityElement = true
    }

    private func applyStyle() {
        let resolved = resolved
        let foreground = resolved.foreground.uiColor.resolvedColor(with: traitCollection)
        backgroundColor = resolved.fill.uiColor.resolvedColor(with: traitCollection)
        imageView.isHidden = !resolved.showsImage
        imageView.image = resolved.showsImage ? TSAvatarImage.usable(image) : nil
        initialsLabel.isHidden = !resolved.showsInitials
        initialsLabel.text = initials
        initialsLabel.font = .systemFont(ofSize: resolved.initialsFontSize, weight: TSFontWeight.uiFontWeight(resolved.initialsFontWeight))
        initialsLabel.minimumScaleFactor = resolved.initialsMinimumScaleFactor
        initialsLabel.textColor = foreground
        placeholderView.isHidden = !resolved.showsPlaceholder
        placeholderView.size = resolved.placeholderIconSize
        placeholderView.tintColor = foreground
    }
}
