import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class IconButtonViewController: DemoPageViewController {
    private var tapCount = 0
    private lazy var counter = DemoPlayground.counter(identifier: "demo.icon-button.playground.count")
    private lazy var playgroundButton: TSIconButtonView = {
        let button = TSIconButtonView(
            icon: .settings,
            label: DemoStrings.string(.sampleMoreOptions),
            primaryAction: UIAction { [weak self] _ in self?.recordTap() }
        )
        button.accessibilityIdentifier = "demo.icon-button.playground.button"
        return button
    }()

    init() {
        super.init(page: .iconButton)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [variantsSection(), shapesSection(), consumerIconSection(), statesSection(), playgroundSection()]
    }

    private func variantsSection() -> UIView {
        DemoComponentSectionView(.variants, views: TSIconButtonVariant.allCases.map { variant in
            row([
                TSIconButtonView(icon: .search, label: DemoStrings.string(.sampleSearch), variant: variant),
                DemoPlayground.caption(variant.rawValue)
            ])
        })
    }

    private func shapesSection() -> UIView {
        DemoComponentSectionView(.shapes, views: [
            row([
                TSIconButtonView(icon: .plus, label: DemoStrings.string(.sampleAdd), variant: .primary),
                TSIconButtonView(icon: .plus, label: DemoStrings.string(.sampleAdd), variant: .primary, isRound: true),
                TSIconButtonView(icon: .more, label: DemoStrings.string(.sampleMoreOptions), variant: .secondary, isRound: true)
            ])
        ])
    }

    private func consumerIconSection() -> UIView {
        let controller = UIImage(named: "DemoController") ?? UIImage()
        let label = DemoStrings.string(.sampleController)
        let ghost = TSIconButtonView(iconImage: controller, label: label)
        ghost.accessibilityIdentifier = "demo.icon-button.consumer-icon"
        return DemoComponentSectionView(.consumerIcon, views: [
            row([
                ghost,
                TSIconButtonView(iconImage: controller, label: label, variant: .secondary),
                TSIconButtonView(iconImage: controller, label: label, variant: .primary, isRound: true)
            ])
        ])
    }

    private func statesSection() -> UIView {
        let rows = TSIconButtonVariant.allCases.flatMap { variant in
            [false, true].flatMap { isRound -> [UIView] in
                let disabled = TSIconButtonView(icon: .close, label: DemoStrings.string(.sampleClose), variant: variant, isRound: isRound)
                disabled.isEnabled = false
                return [
                    DemoPlayground.caption(isRound ? "\(variant.rawValue) · \(DemoStrings.string(.round))" : variant.rawValue),
                    row([TSIconButtonView(icon: .close, label: DemoStrings.string(.sampleClose), variant: variant, isRound: isRound), disabled])
                ]
            }
        }
        return DemoComponentSectionView(.states, views: rows)
    }

    private func playgroundSection() -> UIView {
        let button = playgroundButton
        let section = DemoComponentSectionView(.playground, views: [])
        section.addFullWidth(DemoPlayground.segmented(TSIconButtonVariant.allCases.map(\.rawValue)) { index in
            button.variant = TSIconButtonVariant.allCases[index]
        })
        [
            DemoPlayground.toggle(.round) { button.isRound = $0 },
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.icon-button.playground.disabled") { button.isEnabled = !$0 }
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(button)
        section.addArrangedSubview(counter)
        return section
    }

    private func row(_ views: [UIView]) -> UIStackView {
        let stack = UIStackView(arrangedSubviews: views)
        stack.axis = .horizontal
        stack.alignment = .center
        stack.spacing = TSSpacing.space3
        return stack
    }

    private func recordTap() {
        tapCount += 1
        counter.string = DemoStrings.tapCount(tapCount)
    }
}
