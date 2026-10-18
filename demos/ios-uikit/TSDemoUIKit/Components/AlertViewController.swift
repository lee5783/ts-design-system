import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class AlertViewController: DemoPageViewController {
    private var showsTitle = true
    private var isDismissible = true
    private var showsAction = false
    private var actionCount = 0

    private lazy var counter = DemoPlayground.counter(identifier: "demo.alert.playground.count")
    private lazy var retryButton: TSButtonView = {
        let button = TSButtonView(
            title: SampleMessages.offline.actionTitle ?? "",
            variant: .link,
            primaryAction: UIAction { [weak self] _ in self?.actionDidTap() }
        )
        button.accessibilityIdentifier = "demo.alert.playground.retry"
        return button
    }()
    private lazy var playgroundAlert: TSAlertView = {
        let alert = TSAlertView(tone: .info, title: SampleMessages.offline.title, message: SampleMessages.offline.body)
        alert.accessibilityIdentifier = "demo.alert.playground.alert"
        return alert
    }()
    private lazy var showButton: TSButtonView = {
        let button = TSButtonView(
            title: DemoStrings.string(.showAgain),
            variant: .secondary,
            primaryAction: UIAction { [weak self] _ in self?.showAlert() }
        )
        button.accessibilityIdentifier = "demo.alert.playground.show"
        button.isEnabled = false
        return button
    }()

    init() {
        super.init(page: .alert)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [tonesSection(), contentsSection(), playgroundSection()]
    }

    private func tonesSection() -> UIView {
        let info = TSAlertView(tone: .info, title: DemoStrings.string(.sampleSignInTitle), message: DemoStrings.string(.sampleSignInBody))
        info.accessibilityIdentifier = "demo.alert.tone.info"
        let section = DemoComponentSectionView(.tones, views: [])
        [
            info,
            TSAlertView(tone: .success, title: SampleMessages.saved.title, message: SampleMessages.saved.body),
            TSAlertView(tone: .warning, title: DemoStrings.string(.sampleStorageTitle), message: DemoStrings.string(.sampleStorageBody)),
            TSAlertView(tone: .danger, title: DemoStrings.string(.samplePaymentTitle), message: DemoStrings.string(.samplePaymentBody))
        ].forEach(section.addFullWidth)
        return section
    }

    private func contentsSection() -> UIView {
        let offline = TSAlertView(tone: .warning, title: SampleMessages.offline.title, message: SampleMessages.offline.body)
        offline.actionView = TSButtonView(title: SampleMessages.offline.actionTitle ?? "", variant: .link)
        let section = DemoComponentSectionView(.contents, views: [])
        [
            TSAlertView(tone: .info, message: SampleMessages.saved.body),
            offline,
            TSAlertView(tone: .success, title: SampleMessages.deleted.title, message: SampleMessages.deleted.body, icon: .trash)
        ].forEach(section.addFullWidth)
        return section
    }

    private func playgroundSection() -> UIView {
        let section = DemoComponentSectionView(.playground, views: [])
        [
            DemoPlayground.segmented(TSAlertTone.allCases.map(Self.title), identifier: "demo.alert.playground.tone") { [weak self] in
                self?.playgroundAlert.tone = TSAlertTone.allCases[$0]
            },
            DemoPlayground.toggle(.titleText, identifier: "demo.alert.playground.title", isOn: true) { [weak self] in
                self?.showsTitle = $0
                self?.updatePlayground()
            },
            DemoPlayground.toggle(.dismissible, identifier: "demo.alert.playground.dismissible", isOn: true) { [weak self] in
                self?.isDismissible = $0
                self?.updatePlayground()
            },
            DemoPlayground.toggle(.action, identifier: "demo.alert.playground.action") { [weak self] in
                self?.showsAction = $0
                self?.updatePlayground()
            },
            playgroundAlert
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(showButton)
        section.addArrangedSubview(counter)
        updatePlayground()
        return section
    }

    private func updatePlayground() {
        playgroundAlert.title = showsTitle ? SampleMessages.offline.title : nil
        playgroundAlert.onDismiss = isDismissible ? { [weak self] in self?.dismissAlert() } : nil
        playgroundAlert.actionView = showsAction ? retryButton : nil
    }

    private func dismissAlert() {
        playgroundAlert.isHidden = true
        showButton.isEnabled = true
    }

    private func showAlert() {
        playgroundAlert.isHidden = false
        showButton.isEnabled = false
    }

    private func actionDidTap() {
        actionCount += 1
        counter.string = DemoStrings.tapCount(actionCount)
    }

    private static func title(for tone: TSAlertTone) -> String {
        switch tone {
        case .info: DemoStrings.string(.sampleInformation)
        case .success: DemoStrings.string(.sampleSuccess)
        case .warning: DemoStrings.string(.sampleWarning)
        case .danger: DemoStrings.string(.sampleError)
        }
    }
}
