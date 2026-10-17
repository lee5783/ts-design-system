import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class ToastViewController: DemoPageViewController {
    private var tone: TSToastTone = .neutral
    private var showsAction = true
    private var undoCount = 0

    private lazy var counter = DemoPlayground.counter(identifier: "demo.toast.playground.count")

    private var queue: TSToastQueue {
        .shared
    }

    init() {
        super.init(page: .toast)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [tonesSection(), contentsSection(), queueSection(), sheetSection(), typingSection(), playgroundSection()]
    }

    private func tonesSection() -> UIView {
        let section = DemoComponentSectionView(.tones, views: [])
        [
            TSToastView(DemoStrings.string(.sampleProjectArchived)),
            TSToastView(SampleMessages.saved.title, tone: .success),
            TSToastView(DemoStrings.string(.sampleStorageTitle), tone: .warning),
            TSToastView(DemoStrings.string(.samplePaymentTitle), tone: .danger),
            TSToastView(DemoStrings.string(.sampleSignInTitle), tone: .info)
        ].forEach(section.addFullWidth)
        return section
    }

    private func contentsSection() -> UIView {
        let section = DemoComponentSectionView(.contents, views: [])
        [
            TSToastView(DemoStrings.string(.sampleProjectArchived), actionTitle: DemoStrings.string(.sampleUndo), onAction: {}),
            TSToastView(DemoStrings.string(.sampleLongToast), tone: .info, actionTitle: DemoStrings.string(.sampleOpen), onAction: {})
        ].forEach(section.addFullWidth)
        return section
    }

    private func queueSection() -> UIView {
        let show = button(.showThreeToasts, identifier: "demo.toast.queue.show") { [weak self] in
            [DemoStringKey.samplePhotoUploaded, .sampleAlbumShared, .sampleBackupComplete].forEach {
                self?.queue.enqueue(TSToastItem(message: DemoStrings.string($0), tone: .success))
            }
        }
        return DemoComponentSectionView(.queue, views: [show])
    }

    private func sheetSection() -> UIView {
        let open = button(.openSheet, identifier: "demo.toast.sheet.open") { [weak self] in
            self?.presentSheet()
        }
        return DemoComponentSectionView(.overSheet, views: [open])
    }

    private func typingSection() -> UIView {
        let show = button(.showToast, identifier: "demo.toast.typing.show") { [weak self] in
            self?.queue.enqueue(TSToastItem(message: SampleMessages.saved.title, tone: .success))
        }
        let field = TSTextFieldView(label: DemoStrings.string(.sampleSearch), placeholder: DemoStrings.string(.sampleSearchPlaceholder))
        field.accessibilityIdentifier = "demo.toast.typing.field"
        let section = DemoComponentSectionView(.whileTyping, views: [show])
        section.addFullWidth(field)
        return section
    }

    private func playgroundSection() -> UIView {
        let section = DemoComponentSectionView(.playground, views: [])
        [
            DemoPlayground.segmented(TSToastTone.allCases.map(Self.title), identifier: "demo.toast.playground.tone") { [weak self] in
                self?.tone = TSToastTone.allCases[$0]
            },
            DemoPlayground.toggle(.action, identifier: "demo.toast.playground.action", isOn: true) { [weak self] in
                self?.showsAction = $0
            }
        ].forEach(section.addFullWidth)
        let show = TSButtonView(title: DemoStrings.string(.showToast), primaryAction: UIAction { [weak self] _ in self?.showPlaygroundToast() })
        show.accessibilityIdentifier = "demo.toast.playground.show"
        section.addArrangedSubview(show)
        section.addArrangedSubview(counter)
        return section
    }

    private func button(_ key: DemoStringKey, identifier: String, action: @escaping () -> Void) -> TSButtonView {
        let button = TSButtonView(title: DemoStrings.string(key), variant: .secondary, primaryAction: UIAction { _ in action() })
        button.accessibilityIdentifier = identifier
        return button
    }

    private func presentSheet() {
        TSDialogViewController(
            title: DemoStrings.string(.sampleToastSheetTitle),
            message: DemoStrings.string(.sampleToastSheetBody),
            variant: .sheet,
            actions: [
                TSDialogAction(DemoStrings.string(.sampleClose)),
                TSDialogAction(DemoStrings.string(.sampleCopyLink), variant: .primary, dismisses: false) { [weak self] in
                    self?.queue.enqueue(TSToastItem(message: DemoStrings.string(.sampleLinkCopied), tone: .success))
                }
            ]
        )
        .present(from: self)
    }

    private func showPlaygroundToast() {
        let action = showsAction ? TSToastAction(title: DemoStrings.string(.sampleUndo)) { [weak self] in self?.undoDidTap() } : nil
        queue.enqueue(TSToastItem(message: SampleMessages.deleted.title, tone: tone, action: action))
    }

    private func undoDidTap() {
        undoCount += 1
        counter.string = DemoStrings.tapCount(undoCount)
    }

    private static func title(for tone: TSToastTone) -> String {
        switch tone {
        case .neutral: DemoStrings.string(.sampleNeutral)
        case .info: DemoStrings.string(.sampleInformation)
        case .success: DemoStrings.string(.sampleSuccess)
        case .warning: DemoStrings.string(.sampleWarning)
        case .danger: DemoStrings.string(.sampleError)
        }
    }
}
