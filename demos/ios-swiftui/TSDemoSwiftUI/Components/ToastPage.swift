import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct ToastPage: View {
    @State private var tone: TSToastTone = .neutral
    @State private var showsAction = true
    @State private var undoCount = 0
    @State private var isSheetPresented = false
    @State private var draft = ""

    private var queue: TSToastQueue { .shared }

    var body: some View {
        DemoPage(page: .toast) {
            DemoComponentSection(.tones) {
                TSToast(DemoStrings.string(.sampleProjectArchived))
                TSToast(SampleMessages.saved.title, tone: .success)
                TSToast(DemoStrings.string(.sampleStorageTitle), tone: .warning)
                TSToast(DemoStrings.string(.samplePaymentTitle), tone: .danger)
                TSToast(DemoStrings.string(.sampleSignInTitle), tone: .info)
            }
            DemoComponentSection(.contents) {
                TSToast(DemoStrings.string(.sampleProjectArchived), actionTitle: DemoStrings.string(.sampleUndo), onAction: {})
                TSToast(DemoStrings.string(.sampleLongToast), tone: .info, actionTitle: DemoStrings.string(.sampleOpen), onAction: {})
            }
            DemoComponentSection(.queue) {
                TSButton(DemoStrings.string(.showThreeToasts), variant: .secondary) {
                    [DemoStringKey.samplePhotoUploaded, .sampleAlbumShared, .sampleBackupComplete].forEach {
                        queue.enqueue(TSToastItem(message: DemoStrings.string($0), tone: .success))
                    }
                }
                .accessibilityIdentifier("demo.toast.queue.show")
            }
            DemoComponentSection(.overSheet) {
                TSButton(DemoStrings.string(.openSheet), variant: .secondary) { isSheetPresented = true }
                    .accessibilityIdentifier("demo.toast.sheet.open")
            }
            DemoComponentSection(.whileTyping) {
                TSButton(DemoStrings.string(.showToast), variant: .secondary) {
                    queue.enqueue(TSToastItem(message: SampleMessages.saved.title, tone: .success))
                }
                .accessibilityIdentifier("demo.toast.typing.show")
                TSTextField(DemoStrings.string(.sampleSearch), text: $draft, placeholder: DemoStrings.string(.sampleSearchPlaceholder))
                    .accessibilityIdentifier("demo.toast.typing.field")
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.tone), selection: $tone) {
                    ForEach(TSToastTone.allCases, id: \.self) { Text(Self.title(for: $0)).tag($0) }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("demo.toast.playground.tone")
                Toggle(DemoStrings.string(.action), isOn: $showsAction)
                    .accessibilityIdentifier("demo.toast.playground.action")
                TSButton(DemoStrings.string(.showToast)) { showPlaygroundToast() }
                    .accessibilityIdentifier("demo.toast.playground.show")
                DemoTapCounter(count: undoCount, identifier: "demo.toast.playground.count")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
        .tsDialog(
            isPresented: $isSheetPresented,
            title: DemoStrings.string(.sampleToastSheetTitle),
            message: DemoStrings.string(.sampleToastSheetBody),
            variant: .sheet,
            actions: [
                TSDialogAction(DemoStrings.string(.sampleClose)),
                TSDialogAction(DemoStrings.string(.sampleCopyLink), variant: .primary, dismisses: false) {
                    queue.enqueue(TSToastItem(message: DemoStrings.string(.sampleLinkCopied), tone: .success))
                },
            ]
        )
    }

    private func showPlaygroundToast() {
        let action = showsAction ? TSToastAction(title: DemoStrings.string(.sampleUndo)) { undoCount += 1 } : nil
        queue.enqueue(TSToastItem(message: SampleMessages.deleted.title, tone: tone, action: action))
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
