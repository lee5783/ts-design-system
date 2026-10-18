import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class TextViewController: DemoPageViewController {
    init() {
        super.init(page: .text)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [rolesSection(), colorsSection(), truncationSection()]
    }

    private func rolesSection() -> UIView {
        let section = DemoComponentSectionView(.typeRoles, views: [])
        TSTypeRole.allCases.forEach { role in
            let label = DemoLabel(DemoStrings.string(.pangram), font: role.font, color: TSTextColor.primary.color)
            label.accessibilityIdentifier = "demo.text.role.\(role.rawValue)"
            section.addFullWidth(DemoPlayground.caption(role.rawValue))
            section.addFullWidth(label)
        }
        return section
    }

    private func colorsSection() -> UIView {
        let section = DemoComponentSectionView(.textColors, views: [])
        TSTextColor.allCases.forEach { color in
            let label = DemoLabel(color.rawValue, font: TSTypeRole.body.font, color: color.color)
            label.accessibilityIdentifier = "demo.text.color.\(color.rawValue)"
            let container = UIView()
            container.backgroundColor = TextSample.background(for: color)
            label.translatesAutoresizingMaskIntoConstraints = false
            container.addSubview(label)
            NSLayoutConstraint.activate([
                label.topAnchor.constraint(equalTo: container.topAnchor, constant: TSSpacing.space2),
                label.bottomAnchor.constraint(equalTo: container.bottomAnchor, constant: -TSSpacing.space2),
                label.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: TSSpacing.space2),
                label.trailingAnchor.constraint(equalTo: container.trailingAnchor, constant: -TSSpacing.space2)
            ])
            section.addFullWidth(container)
        }
        return section
    }

    private func truncationSection() -> UIView {
        let label = DemoLabel(DemoStrings.string(.sampleLongSetting), font: TSTypeRole.body.font, color: TSTextColor.primary.color)
        label.numberOfLines = 1
        label.truncatesTail = true
        label.accessibilityIdentifier = "demo.text.truncated"
        let section = DemoComponentSectionView(.truncation, views: [])
        section.addFullWidth(label)
        return section
    }
}

enum TextSample {
    static func background(for color: TSTextColor) -> UIColor {
        switch color {
        case .inverse: TSColor.surfaceInverse.uiColor
        case .onAccent: TSColor.accentPrimary.uiColor
        default: .clear
        }
    }
}
