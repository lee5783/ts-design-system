import TSDesignSystemUIKit
import UIKit

final class DemoLabel: UILabel {
    private let style: TSFont
    private let color: UIColor
    private let alignment: NSTextAlignment

    var string: String {
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
        attributedText = style.attributedString(
            string,
            sizeCategory: traitCollection.preferredContentSizeCategory,
            color: color.resolvedColor(with: traitCollection),
            alignment: resolvedAlignment
        )
    }

    private var resolvedAlignment: NSTextAlignment {
        guard alignment == .natural else { return alignment }
        return effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
    }
}
