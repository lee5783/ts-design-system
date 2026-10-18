import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class ButtonViewController: DemoPageViewController {
    private var tapCount = 0
    private lazy var counter = DemoPlayground.counter(identifier: "demo.button.playground.count")
    private lazy var playgroundButton: TSButtonView = {
        let button = TSButtonView(title: DemoStrings.string(.sampleSave), primaryAction: UIAction { [weak self] _ in self?.recordTap() })
        button.accessibilityIdentifier = "demo.button.playground.button"
        return button
    }()

    init() {
        super.init(page: .button)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [variantsSection(), sizesSection(), statesSection(), iconsSection(), consumerIconSection(), lineLimitSection(), playgroundSection()]
    }

    private func variantsSection() -> UIView {
        DemoComponentSectionView(.variants, views: [
            TSButtonView(title: DemoStrings.string(.sampleSave), variant: .primary),
            TSButtonView(title: DemoStrings.string(.sampleCancel), variant: .secondary),
            TSButtonView(title: DemoStrings.string(.sampleAddItem), variant: .ghost, leadingIcon: .plus),
            TSButtonView(title: DemoStrings.string(.sampleDelete), variant: .danger, leadingIcon: .trash),
            TSButtonView(title: DemoStrings.string(.sampleLearnMore), variant: .link)
        ])
    }

    private func sizesSection() -> UIView {
        let section = DemoComponentSectionView(.sizes, views: [
            TSButtonView(title: DemoStrings.string(.sampleContinue), size: .md, trailingIcon: .arrowRight),
            TSButtonView(title: DemoStrings.string(.sampleGetStarted), size: .lg)
        ])
        let block = TSButtonView(title: DemoStrings.string(.block), size: .lg)
        block.isBlock = true
        section.addFullWidth(block)
        return section
    }

    private func statesSection() -> UIView {
        let section = DemoComponentSectionView(.states, views: [])
        for variant in TSButtonVariant.allCases {
            for size in TSButtonSize.allCases {
                let disabled = TSButtonView(title: DemoStrings.string(.stateDisabled), variant: variant, size: size)
                disabled.isEnabled = false
                let loading = TSButtonView(title: DemoStrings.string(.stateLoading), variant: variant, size: size)
                loading.isLoading = true
                section.addArrangedSubview(DemoPlayground.caption("\(variant.rawValue) · \(size.rawValue)"))
                section.addFullWidth(DemoComponentRowView(
                    [TSButtonView(title: DemoStrings.string(.stateDefault), variant: variant, size: size), disabled, loading],
                    isStacked: size == .lg
                ))
            }
        }
        return section
    }

    private func iconsSection() -> UIView {
        DemoComponentSectionView(.withIcons, views: [
            TSButtonView(title: DemoStrings.string(.leadingIcon), variant: .secondary, leadingIcon: .download),
            TSButtonView(title: DemoStrings.string(.trailingIcon), variant: .secondary, trailingIcon: .chevronRight),
            TSButtonView(title: DemoStrings.string(.bothIcons), variant: .secondary, leadingIcon: .upload, trailingIcon: .arrowRight)
        ])
    }

    private func consumerIconSection() -> UIView {
        let primary = consumerIconButton(variant: .primary)
        primary.accessibilityIdentifier = "demo.button.consumer-icon"
        let loading = consumerIconButton(variant: .primary)
        loading.isLoading = true
        return DemoComponentSectionView(.consumerIcon, views: [primary, consumerIconButton(variant: .secondary), loading])
    }

    private func lineLimitSection() -> UIView {
        let button = TSButtonView(title: DemoStrings.string(.sampleJoinTournament), variant: .secondary)
        button.leadingIconImage = UIImage(named: "DemoController")
        button.numberOfLines = 1
        button.lineBreakMode = .byTruncatingTail
        button.accessibilityIdentifier = "demo.button.line-limit"
        button.widthAnchor.constraint(lessThanOrEqualToConstant: 240).isActive = true
        return DemoComponentSectionView(.lineLimit, views: [button])
    }

    private func consumerIconButton(variant: TSButtonVariant) -> TSButtonView {
        let button = TSButtonView(title: DemoStrings.string(.samplePlayTogether), variant: variant)
        button.leadingIconImage = UIImage(named: "DemoController")
        return button
    }

    private func playgroundSection() -> UIView {
        let button = playgroundButton
        let section = DemoComponentSectionView(.playground, views: [])
        let blockWidth = button.widthAnchor.constraint(equalTo: section.layoutMarginsGuide.widthAnchor)
        section.addFullWidth(DemoPlayground.segmented(TSButtonVariant.allCases.map(\.rawValue), identifier: "demo.button.playground.variant") { index in
            button.variant = TSButtonVariant.allCases[index]
        })
        section.addFullWidth(DemoPlayground.segmented(TSButtonSize.allCases.map(\.rawValue)) { index in
            button.size = TSButtonSize.allCases[index]
        })
        [
            DemoPlayground.toggle(.stateLoading, identifier: "demo.button.playground.loading") { button.isLoading = $0 },
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.button.playground.disabled") { button.isEnabled = !$0 },
            DemoPlayground.toggle(.block) { isOn in
                button.isBlock = isOn
                blockWidth.isActive = isOn
            },
            DemoPlayground.toggle(.leadingIcon) { button.leadingIcon = $0 ? .check : nil },
            DemoPlayground.toggle(.trailingIcon) { button.trailingIcon = $0 ? .arrowRight : nil }
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(button)
        section.addArrangedSubview(counter)
        return section
    }

    private func recordTap() {
        tapCount += 1
        counter.string = DemoStrings.tapCount(tapCount)
    }
}
