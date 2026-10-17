import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class BadgeViewController: DemoPageViewController {
    private var tone: TSBadgeTone = .neutral
    private var showsIcon = false
    private lazy var playgroundBadge: TSBadgeView = {
        let badge = TSBadgeView(text: BadgeSample.text(.neutral))
        badge.accessibilityIdentifier = "demo.badge.playground.badge"
        return badge
    }()

    init() {
        super.init(page: .badge)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [tonesSection(), dotSection(), iconsSection(), truncationSection(), playgroundSection()]
    }

    private func tonesSection() -> UIView {
        let section = DemoComponentSectionView(.tones, views: [])
        TSBadgeTone.allCases.forEach { tone in
            section.addFullWidth(DemoComponentRowView([
                TSBadgeView(text: BadgeSample.text(tone), tone: tone),
                DemoPlayground.caption(tone.rawValue)
            ]))
        }
        return section
    }

    private func dotSection() -> UIView {
        DemoComponentSectionView(.withDot, views: TSBadgeTone.allCases.map { tone in
            TSBadgeView(text: BadgeSample.text(tone), tone: tone, showsDot: true)
        })
    }

    private func iconsSection() -> UIView {
        DemoComponentSectionView(.withIcons, views: TSBadgeTone.allCases.map { tone in
            TSBadgeView(text: BadgeSample.text(tone), tone: tone, icon: BadgeSample.icon(tone))
        })
    }

    private func truncationSection() -> UIView {
        let badge = TSBadgeView(text: DemoStrings.string(.sampleNeedsReview), tone: .warning, showsDot: true)
        let section = DemoComponentSectionView(.truncation, views: [badge])
        badge.widthAnchor.constraint(lessThanOrEqualTo: section.layoutMarginsGuide.widthAnchor).isActive = true
        return section
    }

    private func playgroundSection() -> UIView {
        let badge = playgroundBadge
        let section = DemoComponentSectionView(.playground, views: [])
        section.addFullWidth(DemoPlayground.segmented(TSBadgeTone.allCases.map(\.rawValue), identifier: "demo.badge.playground.tone") { [weak self] index in
            self?.tone = TSBadgeTone.allCases[index]
            self?.updatePlayground()
        })
        [
            DemoPlayground.toggle(.dot, identifier: "demo.badge.playground.dot") { badge.showsDot = $0 },
            DemoPlayground.toggle(.icon, identifier: "demo.badge.playground.icon") { [weak self] in
                self?.showsIcon = $0
                self?.updatePlayground()
            }
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(badge)
        return section
    }

    private func updatePlayground() {
        playgroundBadge.tone = tone
        playgroundBadge.text = BadgeSample.text(tone)
        playgroundBadge.icon = showsIcon ? BadgeSample.icon(tone) : nil
    }
}

enum BadgeSample {
    static func text(_ tone: TSBadgeTone) -> String {
        switch tone {
        case .neutral: DemoStrings.string(.sampleDraft)
        case .accent: DemoStrings.string(.sampleNew)
        case .success: DemoStrings.string(.sampleActive)
        case .warning: DemoStrings.string(.samplePending)
        case .danger: DemoStrings.string(.sampleFailed)
        case .info: DemoStrings.string(.sampleBeta)
        }
    }

    static func icon(_ tone: TSBadgeTone) -> TSIconName {
        switch tone {
        case .neutral: .edit
        case .accent: .star
        case .success: .checkCircle
        case .warning: .alert
        case .danger: .xCircle
        case .info: .info
        }
    }
}
