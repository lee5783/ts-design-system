import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class TypographyViewController: DemoPageViewController {
    init() {
        super.init(page: .typography)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        TSFont.allFonts.map { font in
            let detail = "\(font.token.name) · \(Int(font.token.fontSize))/\(Int(font.token.lineHeight)) · \(font.token.fontWeight)"
            let sample = UIStackView(arrangedSubviews: [
                DemoLabel(detail, font: .code, color: .textMuted),
                DemoLabel(DemoStrings.string(.pangram), font: font, color: .textPrimary)
            ])
            sample.axis = .vertical
            sample.spacing = TSSpacing.space1
            sample.isAccessibilityElement = true
            sample.accessibilityLabel = "\(detail), \(font.text(DemoStrings.string(.pangram)))"
            sample.accessibilityIdentifier = "demo.token.\(font.token.name)"
            return sample
        }
    }
}
