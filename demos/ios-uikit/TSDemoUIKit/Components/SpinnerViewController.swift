import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class SpinnerViewController: DemoPageViewController {
    init() {
        super.init(page: .spinner)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [sizesSection(), tonesSection(), inContextSection()]
    }

    private func sizesSection() -> UIView {
        let spinners = TSSpinnerSize.allCases.map { size in
            let spinner = TSSpinnerView(size: size)
            spinner.accessibilityIdentifier = "demo.spinner.size.\(size.rawValue)"
            return spinner
        }
        let section = DemoComponentSectionView(.sizes, views: [])
        section.addFullWidth(DemoComponentRowView(spinners))
        return section
    }

    private func tonesSection() -> UIView {
        let section = DemoComponentSectionView(.tones, views: [])
        TSSpinnerTone.allCases.forEach { tone in
            let spinner = TSSpinnerView(tone: tone)
            spinner.accessibilityIdentifier = "demo.spinner.tone.\(tone.rawValue)"
            section.addFullWidth(DemoComponentRowView([spinner, DemoPlayground.caption(tone.rawValue)]))
        }
        return section
    }

    private func inContextSection() -> UIView {
        let loading = DemoStrings.string(.sampleLoadingMessages)
        let quiet = TSSpinnerView(size: .sm, tone: .secondary)
        quiet.isAccessibilityElement = false
        let row = DemoComponentRowView([quiet, DemoLabel(loading, font: TSTypeRole.bodySm.font, color: TSTextColor.secondary.color)])
        row.isAccessibilityElement = true
        row.accessibilityLabel = loading
        row.accessibilityIdentifier = "demo.spinner.in-context"
        let labelled = TSSpinnerView(size: .lg, tone: .accent)
        labelled.accessibilityLabel = loading
        labelled.accessibilityIdentifier = "demo.spinner.labelled"
        let section = DemoComponentSectionView(.inContext, views: [])
        section.addFullWidth(row)
        section.addArrangedSubview(labelled)
        return section
    }
}
