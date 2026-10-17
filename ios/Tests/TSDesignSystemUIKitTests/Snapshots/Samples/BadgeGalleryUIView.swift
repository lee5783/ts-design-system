import TSDesignSystemUIKit
import UIKit

final class BadgeGalleryUIView: GalleryStackView {
    static let samples: [(tone: TSBadgeTone, text: String, icon: TSIconName)] = [
        (.neutral, "Draft", .edit),
        (.accent, "New", .star),
        (.success, "Active", .checkCircle),
        (.warning, "Pending", .alert),
        (.danger, "Failed", .xCircle),
        (.info, "Beta", .info)
    ]

    private var rows: [UIStackView] = []

    init() {
        super.init(frame: .zero)
        for sample in Self.samples {
            addRow([
                TSBadgeView(text: sample.text, tone: sample.tone),
                TSBadgeView(text: sample.text, tone: sample.tone, showsDot: true),
                TSBadgeView(text: sample.text, tone: sample.tone, icon: sample.icon)
            ], spacing: TSSpacing.space2)
        }
        rows = stack.arrangedSubviews.compactMap { $0 as? UIStackView }
        let long = TSBadgeView(text: "Needs review by the design team", tone: .warning, showsDot: true)
        stack.addArrangedSubview(long)
        long.widthAnchor.constraint(lessThanOrEqualTo: stack.layoutMarginsGuide.widthAnchor).isActive = true
        updateAxis()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        updateAxis()
    }

    private func updateAxis() {
        let isStacked = traitCollection.preferredContentSizeCategory.isAccessibilityCategory
        rows.forEach {
            $0.axis = isStacked ? .vertical : .horizontal
            $0.alignment = isStacked ? .leading : .center
        }
    }
}
