import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct DialogPage: View {
    @State private var isCenterPresented = false
    @State private var isSheetPresented = false
    @State private var isInputPresented = false
    @State private var isStackingPresented = false
    @State private var isSecondPresented = false
    @State private var isChainPresented = false
    @State private var isUndoPresented = false
    @State private var isPlaygroundPresented = false
    @State private var playgroundVariant: TSDialogVariant = .center
    @State private var isLongContent = false
    @State private var projectName = DemoStrings.string(.sampleProjectNameValue)
    @State private var result = DemoStrings.string(.none)
    @ObservedObject private var presenter = TSDialogPresenter.shared

    var body: some View {
        DemoPage(page: .dialog) {
            DemoComponentSection(.variants) {
                DemoComponentRow {
                    TSButton(DemoStrings.string(.openDialog), variant: .secondary) { isCenterPresented = true }
                        .accessibilityIdentifier("demo.dialog.open.center")
                    TSButton(DemoStrings.string(.openSheet), variant: .secondary) { isSheetPresented = true }
                        .accessibilityIdentifier("demo.dialog.open.sheet")
                }
                DemoCaption(text: DemoStrings.dialogResult(result))
                    .accessibilityIdentifier("demo.dialog.result")
            }
            DemoComponentSection(.contents) {
                TSButton(DemoStrings.string(.withInput), variant: .secondary) { isInputPresented = true }
                    .accessibilityIdentifier("demo.dialog.open.input")
            }
            DemoComponentSection(.stacking) {
                TSButton(DemoStrings.string(.stacking), variant: .secondary) { isStackingPresented = true }
                    .accessibilityIdentifier("demo.dialog.open.stacking")
                DemoCaption(text: DemoStrings.queuedCount(presenter.pendingCount))
                    .accessibilityIdentifier("demo.dialog.queued")
                DemoCaption(text: DemoStrings.refusedCount(presenter.refusedCount))
                    .accessibilityIdentifier("demo.dialog.refused")
                TSButton(DemoStrings.string(.chain), variant: .secondary) { isChainPresented = true }
                    .accessibilityIdentifier("demo.dialog.open.chain")
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.variant), selection: $playgroundVariant) {
                    Text(DemoStrings.string(.center)).tag(TSDialogVariant.center)
                    Text(DemoStrings.string(.sheet)).tag(TSDialogVariant.sheet)
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("demo.dialog.playground.variant")
                Toggle(DemoStrings.string(.longContent), isOn: $isLongContent)
                    .accessibilityIdentifier("demo.dialog.playground.long")
                TSButton(DemoStrings.string(.openDialog)) { isPlaygroundPresented = true }
                    .accessibilityIdentifier("demo.dialog.playground.open")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
        .tsDialog(
            isPresented: $isCenterPresented,
            title: SampleMessages.confirmDelete.title,
            message: SampleMessages.confirmDelete.body,
            actions: deleteActions,
            onDismiss: dismissed
        )
        .tsDialog(
            isPresented: $isSheetPresented,
            title: SampleMessages.confirmDelete.title,
            message: SampleMessages.confirmDelete.body,
            variant: .sheet,
            actions: deleteActions,
            onDismiss: dismissed
        )
        .tsDialog(
            isPresented: $isInputPresented,
            title: DemoStrings.string(.sampleRenameTitle),
            actions: [
                TSDialogAction(DemoStrings.string(.sampleCancel)) { result = DemoStrings.string(.sampleCancel) },
                TSDialogAction(DemoStrings.string(.sampleRename), variant: .primary) { result = projectName },
            ],
            onDismiss: dismissed
        ) {
            TSTextField(DemoStrings.string(.sampleProjectName), text: $projectName)
                .accessibilityIdentifier("demo.dialog.input.field")
        }
        .tsDialog(
            isPresented: $isStackingPresented,
            title: DemoStrings.string(.sampleStackingTitle),
            message: DemoStrings.string(.sampleStackingQueueBody),
            actions: [
                TSDialogAction(DemoStrings.string(.sampleClose)),
                TSDialogAction(DemoStrings.string(.openAnother), variant: .primary, dismisses: false) { isSecondPresented = true },
            ]
        )
        .tsDialog(
            isPresented: $isSecondPresented,
            title: SampleMessages.confirmDelete.title,
            message: SampleMessages.confirmDelete.body,
            actions: deleteActions,
            onRefused: { result = DemoStrings.string(.refused) }
        )
        .tsDialog(
            isPresented: $isChainPresented,
            title: SampleMessages.confirmDelete.title,
            message: SampleMessages.confirmDelete.body,
            actions: [
                TSDialogAction(DemoStrings.string(.sampleCancel)) { result = DemoStrings.string(.sampleCancel) },
                TSDialogAction(DemoStrings.string(.sampleDelete), variant: .danger) {
                    result = DemoStrings.string(.sampleDelete)
                    isUndoPresented = true
                },
            ],
            onDismiss: dismissed
        )
        .tsDialog(
            isPresented: $isUndoPresented,
            title: DemoStrings.string(.sampleDeletedTitle),
            actions: [
                TSDialogAction(DemoStrings.string(.sampleClose)),
                TSDialogAction(DemoStrings.string(.sampleUndo), variant: .primary) { result = DemoStrings.string(.sampleUndo) },
            ],
            onDismiss: dismissed
        )
        .tsDialog(
            isPresented: $isPlaygroundPresented,
            title: isLongContent ? DemoStrings.string(.sampleArchiveTitle) : SampleMessages.confirmDelete.title,
            message: isLongContent ? DemoStrings.string(.sampleLongDialogBody) : SampleMessages.confirmDelete.body,
            variant: playgroundVariant,
            actions: [
                TSDialogAction(DemoStrings.string(.sampleCancel)) { result = DemoStrings.string(.sampleCancel) },
                TSDialogAction(DemoStrings.string(isLongContent ? .sampleArchive : .sampleDelete), variant: isLongContent ? .primary : .danger) {
                    result = DemoStrings.string(isLongContent ? .sampleArchive : .sampleDelete)
                },
            ],
            onDismiss: dismissed
        )
    }

    private var deleteActions: [TSDialogAction] {
        [
            TSDialogAction(DemoStrings.string(.sampleCancel)) { result = DemoStrings.string(.sampleCancel) },
            TSDialogAction(DemoStrings.string(.sampleDelete), variant: .danger) { result = DemoStrings.string(.sampleDelete) },
        ]
    }

    private func dismissed() {
        result = DemoStrings.string(.dismissed)
    }
}
