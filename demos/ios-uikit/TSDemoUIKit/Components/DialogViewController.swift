import Combine
import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class DialogViewController: DemoPageViewController {
    private var playgroundVariant: TSDialogVariant = .center
    private var isLongContent = false
    private var subscription: AnyCancellable?

    private lazy var resultLabel: DemoLabel = {
        let label = DemoPlayground.caption(DemoStrings.dialogResult(DemoStrings.string(.none)))
        label.accessibilityIdentifier = "demo.dialog.result"
        return label
    }()
    private lazy var refusedLabel: DemoLabel = {
        let label = DemoPlayground.caption(DemoStrings.refusedCount(TSDialogPresenter.shared.refusedCount))
        label.accessibilityIdentifier = "demo.dialog.refused"
        return label
    }()

    init() {
        super.init(page: .dialog)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        subscription = TSDialogPresenter.shared.$refusedCount.sink { [weak self] count in
            self?.refusedLabel.string = DemoStrings.refusedCount(count)
        }
    }

    override func makeContentViews() -> [UIView] {
        [variantsSection(), contentsSection(), stackingSection(), playgroundSection()]
    }

    private func variantsSection() -> UIView {
        let center = button(.openDialog, identifier: "demo.dialog.open.center") { [weak self] in
            self?.presentDelete(variant: .center)
        }
        let sheet = button(.openSheet, identifier: "demo.dialog.open.sheet") { [weak self] in
            self?.presentDelete(variant: .sheet)
        }
        return DemoComponentSectionView(.variants, views: [DemoComponentRowView([center, sheet]), resultLabel])
    }

    private func contentsSection() -> UIView {
        let input = button(.withInput, identifier: "demo.dialog.open.input") { [weak self] in
            self?.presentRename()
        }
        return DemoComponentSectionView(.contents, views: [input])
    }

    private func stackingSection() -> UIView {
        let stacking = button(.stacking, identifier: "demo.dialog.open.stacking") { [weak self] in
            self?.presentStacking()
        }
        let chain = button(.chain, identifier: "demo.dialog.open.chain") { [weak self] in
            self?.presentChain()
        }
        return DemoComponentSectionView(.stacking, views: [stacking, refusedLabel, chain])
    }

    private func playgroundSection() -> UIView {
        let section = DemoComponentSectionView(.playground, views: [])
        [
            DemoPlayground.segmented([DemoStrings.string(.center), DemoStrings.string(.sheet)], identifier: "demo.dialog.playground.variant") { [weak self] in
                self?.playgroundVariant = $0 == 0 ? .center : .sheet
            },
            DemoPlayground.toggle(.longContent, identifier: "demo.dialog.playground.long") { [weak self] in
                self?.isLongContent = $0
            }
        ].forEach(section.addFullWidth)
        let open = TSButtonView(title: DemoStrings.string(.openDialog), primaryAction: UIAction { [weak self] _ in self?.presentPlayground() })
        open.accessibilityIdentifier = "demo.dialog.playground.open"
        section.addArrangedSubview(open)
        return section
    }

    private func button(_ key: DemoStringKey, identifier: String, action: @escaping () -> Void) -> TSButtonView {
        let button = TSButtonView(title: DemoStrings.string(key), variant: .secondary, primaryAction: UIAction { _ in action() })
        button.accessibilityIdentifier = identifier
        return button
    }

    private func presentDelete(variant: TSDialogVariant) {
        makeDeleteDialog(variant: variant).present(from: self)
    }

    private func makeDeleteDialog(variant: TSDialogVariant) -> TSDialogViewController {
        TSDialogViewController(
            title: SampleMessages.confirmDelete.title,
            message: SampleMessages.confirmDelete.body,
            variant: variant,
            actions: [
                TSDialogAction(DemoStrings.string(.sampleCancel)) { [weak self] in self?.show(result: DemoStrings.string(.sampleCancel)) },
                TSDialogAction(DemoStrings.string(.sampleDelete), variant: .danger) { [weak self] in self?.show(result: DemoStrings.string(.sampleDelete)) }
            ],
            onDismiss: { [weak self] in self?.show(result: DemoStrings.string(.dismissed)) }
        )
    }

    private func presentRename() {
        let field = TSTextFieldView(label: DemoStrings.string(.sampleProjectName), text: DemoStrings.string(.sampleProjectNameValue))
        field.accessibilityIdentifier = "demo.dialog.input.field"
        let dialog = TSDialogViewController(
            title: DemoStrings.string(.sampleRenameTitle),
            actions: [
                TSDialogAction(DemoStrings.string(.sampleCancel)) { [weak self] in self?.show(result: DemoStrings.string(.sampleCancel)) },
                TSDialogAction(DemoStrings.string(.sampleRename), variant: .primary) { [weak self, weak field] in self?.show(result: field?.text ?? "") }
            ],
            onDismiss: { [weak self] in self?.show(result: DemoStrings.string(.dismissed)) }
        )
        dialog.bodyView = field
        dialog.present(from: self)
    }

    private func presentStacking() {
        let dialog = TSDialogViewController(
            title: DemoStrings.string(.sampleStackingTitle),
            message: DemoStrings.string(.sampleStackingBody),
            actions: [
                TSDialogAction(DemoStrings.string(.sampleClose)),
                TSDialogAction(DemoStrings.string(.openAnother), variant: .primary, dismisses: false) { [weak self] in
                    guard let self else { return }
                    if !self.makeDeleteDialog(variant: .center).present(from: self) {
                        self.show(result: DemoStrings.string(.refused))
                    }
                }
            ]
        )
        dialog.present(from: self)
    }

    private func presentChain() {
        TSDialogViewController(
            title: SampleMessages.confirmDelete.title,
            message: SampleMessages.confirmDelete.body,
            actions: [
                TSDialogAction(DemoStrings.string(.sampleCancel)) { [weak self] in self?.show(result: DemoStrings.string(.sampleCancel)) },
                TSDialogAction(DemoStrings.string(.sampleDelete), variant: .danger) { [weak self] in
                    self?.show(result: DemoStrings.string(.sampleDelete))
                    self?.presentUndo()
                }
            ],
            onDismiss: { [weak self] in self?.show(result: DemoStrings.string(.dismissed)) }
        )
        .present(from: self)
    }

    private func presentUndo() {
        TSDialogViewController(
            title: DemoStrings.string(.sampleDeletedTitle),
            actions: [
                TSDialogAction(DemoStrings.string(.sampleClose)),
                TSDialogAction(DemoStrings.string(.sampleUndo), variant: .primary) { [weak self] in self?.show(result: DemoStrings.string(.sampleUndo)) }
            ],
            onDismiss: { [weak self] in self?.show(result: DemoStrings.string(.dismissed)) }
        )
        .present(from: self)
    }

    private func presentPlayground() {
        let primary: DemoStringKey = isLongContent ? .sampleArchive : .sampleDelete
        TSDialogViewController(
            title: isLongContent ? DemoStrings.string(.sampleArchiveTitle) : SampleMessages.confirmDelete.title,
            message: isLongContent ? DemoStrings.string(.sampleLongDialogBody) : SampleMessages.confirmDelete.body,
            variant: playgroundVariant,
            actions: [
                TSDialogAction(DemoStrings.string(.sampleCancel)) { [weak self] in self?.show(result: DemoStrings.string(.sampleCancel)) },
                TSDialogAction(DemoStrings.string(primary), variant: isLongContent ? .primary : .danger) { [weak self] in
                    self?.show(result: DemoStrings.string(primary))
                }
            ],
            onDismiss: { [weak self] in self?.show(result: DemoStrings.string(.dismissed)) }
        )
        .present(from: self)
    }

    private func show(result: String) {
        resultLabel.string = DemoStrings.dialogResult(result)
    }
}
