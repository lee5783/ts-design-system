import UIKit

extension TSFont {
    func truncatingAttributedString(
        _ string: String,
        sizeCategory: UIContentSizeCategory,
        color: UIColor,
        alignment: NSTextAlignment = .natural,
        isSingleLine: Bool
    ) -> NSAttributedString {
        var attributes = attributes(for: sizeCategory, color: color, alignment: alignment)
        let paragraphStyle = paragraphStyle(for: sizeCategory, alignment: alignment).mutableCopy() as? NSMutableParagraphStyle
        paragraphStyle?.lineBreakMode = isSingleLine ? .byTruncatingTail : .byWordWrapping
        attributes[.paragraphStyle] = paragraphStyle
        return NSAttributedString(string: text(string), attributes: attributes)
    }
}
