import UIKit

package final class TSGrowingTextView: UITextView {
    package struct Typography: Equatable {
        package let font: TSFont
        package let color: UIColor
        package let placeholder: String
        package let placeholderColor: UIColor
        package let sizeCategory: UIContentSizeCategory
        package let alignment: NSTextAlignment

        package init(
            font: TSFont,
            color: UIColor,
            placeholder: String,
            placeholderColor: UIColor,
            sizeCategory: UIContentSizeCategory,
            alignment: NSTextAlignment = .natural
        ) {
            self.font = font
            self.color = color
            self.placeholder = placeholder
            self.placeholderColor = placeholderColor
            self.sizeCategory = sizeCategory
            self.alignment = alignment
        }
    }

    package let placeholderLabel = UILabel()
    package var minimumLines = TSTextFieldStyleSpec.minimumLines {
        didSet { refreshLayout() }
    }

    package var maximumLines: Int? = TSTextFieldStyleSpec.maximumLines {
        didSet { refreshLayout() }
    }

    package var verticalInset: CGFloat = 0 {
        didSet {
            textContainerInset = UIEdgeInsets(top: verticalInset, left: 0, bottom: verticalInset, right: 0)
            placeholderTop.constant = verticalInset
            refreshLayout()
        }
    }

    package var onTextChange: ((String) -> Void)?
    private(set) package var typography = Typography(font: .body, color: .label, placeholder: "", placeholderColor: .placeholderText, sizeCategory: .large)
    private var measuredWidth: CGFloat = 0
    private let storage: NSTextStorage
    private lazy var placeholderTop = placeholderLabel.topAnchor.constraint(equalTo: frameLayoutGuide.topAnchor)

    package init() {
        let storage = NSTextStorage()
        let layoutManager = NSLayoutManager()
        let container = NSTextContainer(size: CGSize(width: 0, height: CGFloat.greatestFiniteMagnitude))
        container.widthTracksTextView = true
        layoutManager.allowsNonContiguousLayout = false
        layoutManager.addTextContainer(container)
        storage.addLayoutManager(layoutManager)
        self.storage = storage
        super.init(frame: .zero, textContainer: container)
        backgroundColor = .clear
        textContainerInset = .zero
        textContainer.lineFragmentPadding = 0
        isScrollEnabled = false
        adjustsFontForContentSizeCategory = false
        setContentHuggingPriority(.defaultLow, for: .horizontal)
        setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        setContentHuggingPriority(.required, for: .vertical)
        setContentCompressionResistancePriority(.required, for: .vertical)
        placeholderLabel.numberOfLines = 0
        placeholderLabel.isAccessibilityElement = false
        placeholderLabel.translatesAutoresizingMaskIntoConstraints = false
        addSubview(placeholderLabel)
        NSLayoutConstraint.activate([
            placeholderTop,
            placeholderLabel.leadingAnchor.constraint(equalTo: frameLayoutGuide.leadingAnchor),
            placeholderLabel.trailingAnchor.constraint(equalTo: frameLayoutGuide.trailingAnchor)
        ])
        NotificationCenter.default.addObserver(self, selector: #selector(textDidChange), name: UITextView.textDidChangeNotification, object: self)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    package var styledText: String {
        get { text ?? "" }
        set {
            guard newValue != text || attributedText.length == 0 else { return }
            attributedText = NSAttributedString(string: newValue, attributes: attributes)
            refreshLayout()
        }
    }

    package var lineHeight: CGFloat {
        typography.font.lineHeight(for: typography.sizeCategory)
    }

    package func apply(_ typography: Typography) {
        guard typography != self.typography else { return }
        self.typography = typography
        let current = text ?? ""
        typingAttributes = attributes
        attributedText = NSAttributedString(string: current, attributes: attributes)
        placeholderLabel.attributedText = typography.font.attributedString(
            typography.placeholder,
            sizeCategory: typography.sizeCategory,
            color: typography.placeholderColor,
            alignment: typography.alignment
        )
        refreshLayout()
    }

    package func layout(forWidth width: CGFloat) -> TSTextFieldStyleSpec.MultilineLayout {
        let contentHeight = width > 0 ? sizeThatFits(CGSize(width: width, height: .greatestFiniteMagnitude)).height : 0
        return TSTextFieldStyleSpec.multilineLayout(
            contentHeight: contentHeight,
            lineHeight: lineHeight,
            verticalInset: verticalInset,
            minimumLines: minimumLines,
            maximumLines: maximumLines
        )
    }

    override package var intrinsicContentSize: CGSize {
        CGSize(width: UIView.noIntrinsicMetric, height: layout(forWidth: bounds.width).height)
    }

    override package func layoutSubviews() {
        super.layoutSubviews()
        if bounds.width != measuredWidth {
            measuredWidth = bounds.width
            refreshLayout()
        }
    }

    private var attributes: [NSAttributedString.Key: Any] {
        typography.font.attributes(for: typography.sizeCategory, color: typography.color, alignment: typography.alignment)
    }

    private func refreshLayout() {
        placeholderLabel.isHidden = !(text ?? "").isEmpty
        let layout = layout(forWidth: bounds.width)
        if isScrollEnabled != layout.isScrollEnabled {
            isScrollEnabled = layout.isScrollEnabled
            if !isFirstResponder {
                contentOffset = .zero
            }
        }
        invalidateIntrinsicContentSize()
    }

    @objc
    private func textDidChange() {
        refreshLayout()
        onTextChange?(text ?? "")
    }
}
