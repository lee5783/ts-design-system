import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class CardViewController: DemoPageViewController {
    private var stateTapCount = 0
    private var tapCount = 0
    private var showsSubtitle = true
    private var showsFooter = false

    private lazy var stateCounter = DemoPlayground.counter(identifier: "demo.card.state.count")
    private lazy var lastActionCaption: DemoLabel = {
        let caption = DemoPlayground.caption(DemoStrings.string(.none))
        caption.accessibilityIdentifier = "demo.card.state.last-action"
        return caption
    }()
    private lazy var counter = DemoPlayground.counter(identifier: "demo.card.playground.count")
    private lazy var playgroundCard: TSCardView = {
        let card = TSCardView(
            title: DemoStrings.string(.sampleWeeklyReport),
            subtitle: DemoStrings.string(.sampleUpdatedToday),
            bodyText: DemoStrings.string(.sampleReportBody),
            isInteractive: true
        )
        card.accessibilityIdentifier = "demo.card.playground.card"
        card.addAction(UIAction { [weak self] _ in self?.playgroundDidTap() }, for: .primaryActionTriggered)
        return card
    }()

    init() {
        super.init(page: .card)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [variantsSection(), statesSection(), playgroundSection()]
    }

    private func variantsSection() -> UIView {
        let report = TSCardView(
            title: DemoStrings.string(.sampleWeeklyReport),
            subtitle: DemoStrings.string(.sampleUpdatedToday),
            bodyText: DemoStrings.string(.sampleReportBody)
        )
        report.accessoryView = TSBadgeView(text: DemoStrings.string(.sampleNew), tone: .accent)
        let raised = TSCardView(
            title: DemoStrings.string(.raised),
            bodyText: DemoStrings.string(.sampleReportBody),
            isRaised: true,
            accessory: .iconButton(icon: .more, label: DemoStrings.string(.sampleMoreOptions)) {}
        )
        let invite = TSCardView(
            title: DemoStrings.string(.sampleInviteTeam),
            bodyText: DemoStrings.string(.sampleInviteBody),
            footerActions: [
                TSCardAction(DemoStrings.string(.sampleCancel)) {},
                TSCardAction(DemoStrings.string(.sampleSendInvite), variant: .primary) {}
            ]
        )
        let section = DemoComponentSectionView(.variants, views: [])
        [report, raised, invite].forEach {
            section.addFullWidth($0)
            section.setCustomSpacing(TSCardStyleSpec.stackSpacing, after: $0)
        }
        return section
    }

    private func statesSection() -> UIView {
        let defaultCard = TSCardView(
            title: DemoStrings.string(.stateDefault),
            subtitle: DemoStrings.string(.interactive),
            bodyText: DemoStrings.string(.sampleReportBody),
            isInteractive: true,
            footerActions: [
                TSCardAction(DemoStrings.string(.sampleShare)) { [weak self] in self?.lastActionCaption.string = DemoStrings.string(.sampleShare) },
                TSCardAction(DemoStrings.string(.sampleOpen), variant: .primary) { [weak self] in self?.lastActionCaption.string = DemoStrings.string(.sampleOpen) }
            ]
        )
        defaultCard.accessibilityIdentifier = "demo.card.state.default"
        defaultCard.addAction(UIAction { [weak self] _ in self?.stateDidTap() }, for: .primaryActionTriggered)
        let disabledCard = TSCardView(
            title: DemoStrings.string(.stateDisabled),
            subtitle: DemoStrings.string(.interactive),
            bodyText: DemoStrings.string(.sampleReportBody),
            isInteractive: true
        )
        disabledCard.isEnabled = false
        disabledCard.accessibilityIdentifier = "demo.card.state.disabled"
        disabledCard.addAction(UIAction { [weak self] _ in self?.stateDidTap() }, for: .primaryActionTriggered)
        let section = DemoComponentSectionView(.states, views: [])
        [defaultCard, disabledCard].forEach {
            section.addFullWidth($0)
            section.setCustomSpacing(TSCardStyleSpec.stackSpacing, after: $0)
        }
        section.addArrangedSubview(stateCounter)
        section.addArrangedSubview(lastActionCaption)
        return section
    }

    private func playgroundSection() -> UIView {
        let section = DemoComponentSectionView(.playground, views: [])
        [
            DemoPlayground.toggle(.subtitle, identifier: "demo.card.playground.subtitle", isOn: true) { [weak self] in
                self?.showsSubtitle = $0
                self?.updatePlayground()
            },
            DemoPlayground.toggle(.raised, identifier: "demo.card.playground.raised") { [weak self] in
                self?.playgroundCard.isRaised = $0
            },
            DemoPlayground.toggle(.interactive, identifier: "demo.card.playground.interactive", isOn: true) { [weak self] in
                self?.playgroundCard.isInteractive = $0
            },
            DemoPlayground.toggle(.footer, identifier: "demo.card.playground.footer") { [weak self] in
                self?.showsFooter = $0
                self?.updatePlayground()
            },
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.card.playground.disabled") { [weak self] in
                self?.playgroundCard.isEnabled = !$0
            },
            playgroundCard
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(counter)
        return section
    }

    private func updatePlayground() {
        playgroundCard.subtitle = showsSubtitle ? DemoStrings.string(.sampleUpdatedToday) : nil
        playgroundCard.footerActions = showsFooter
            ? [TSCardAction(DemoStrings.string(.sampleOpen), variant: .primary) { [weak self] in self?.playgroundDidTap() }]
            : []
    }

    private func playgroundDidTap() {
        tapCount += 1
        counter.string = DemoStrings.tapCount(tapCount)
    }

    private func stateDidTap() {
        stateTapCount += 1
        stateCounter.string = DemoStrings.tapCount(stateTapCount)
    }
}
