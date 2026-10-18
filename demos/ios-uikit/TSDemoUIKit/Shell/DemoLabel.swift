import TSDesignSystemUIKit
import UIKit

final class DemoLabel: UILabel {
    private let style: TSFont
    private let color: UIColor
    private let alignment: NSTextAlignment

    var string: String {
        didSet { render() }
    }

    var truncatesTail = false {
        didSet { render() }
    }

    init(_ string: String, font: TSFont, color: TSColor, alignment: NSTextAlignment = .natural) {
        self.string = string
        self.style = font
        self.color = color.uiColor
        self.alignment = alignment
        super.init(frame: .zero)
        numberOfLines = 0
        render()
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if previousTraitCollection?.preferredContentSizeCategory != traitCollection.preferredContentSizeCategory
            || previousTraitCollection?.layoutDirection != traitCollection.layoutDirection
            || previousTraitCollection?.hasDifferentColorAppearance(comparedTo: traitCollection) != false {
            render()
        }
    }

    private func render() {
        let sizeCategory = traitCollection.preferredContentSizeCategory
        var attributes = style.attributes(for: sizeCategory, color: color.resolvedColor(with: traitCollection), alignment: resolvedAlignment)
        if truncatesTail, let paragraph = style.paragraphStyle(for: sizeCategory, alignment: resolvedAlignment).mutableCopy() as? NSMutableParagraphStyle {
            paragraph.lineBreakMode = .byTruncatingTail
            attributes[.paragraphStyle] = paragraph
        }
        attributedText = NSAttributedString(string: style.text(string), attributes: attributes)
    }

    private var resolvedAlignment: NSTextAlignment {
        guard alignment == .natural else { return alignment }
        return effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
    }
}
