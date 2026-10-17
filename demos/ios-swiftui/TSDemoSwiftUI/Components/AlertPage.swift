import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct AlertPage: View {
    @State private var tone: TSAlertTone = .info
    @State private var showsTitle = true
    @State private var isDismissible = true
    @State private var showsAction = false
    @State private var isShown = true
    @State private var hasBeenDismissed = false
    @State private var actionCount = 0

    var body: some View {
        DemoPage(page: .alert) {
            DemoComponentSection(.tones) {
                TSAlert(tone: .info, title: DemoStrings.string(.sampleSignInTitle), message: DemoStrings.string(.sampleSignInBody))
                    .accessibilityIdentifier("demo.alert.tone.info")
                TSAlert(tone: .success, title: SampleMessages.saved.title, message: SampleMessages.saved.body)
                TSAlert(tone: .warning, title: DemoStrings.string(.sampleStorageTitle), message: DemoStrings.string(.sampleStorageBody))
                TSAlert(tone: .danger, title: DemoStrings.string(.samplePaymentTitle), message: DemoStrings.string(.samplePaymentBody))
            }
            DemoComponentSection(.contents) {
                TSAlert(tone: .info, message: SampleMessages.saved.body)
                TSAlert(tone: .warning, title: SampleMessages.offline.title, message: SampleMessages.offline.body) {
                    TSButton(SampleMessages.offline.actionTitle ?? "", variant: .link) {}
                }
                TSAlert(tone: .success, title: SampleMessages.deleted.title, message: SampleMessages.deleted.body, icon: .trash)
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.tone), selection: $tone) {
                    ForEach(TSAlertTone.allCases, id: \.self) { Text(Self.title(for: $0)).tag($0) }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("demo.alert.playground.tone")
                Toggle(DemoStrings.string(.titleText), isOn: $showsTitle)
                    .accessibilityIdentifier("demo.alert.playground.title")
                Toggle(DemoStrings.string(.dismissible), isOn: $isDismissible)
                    .accessibilityIdentifier("demo.alert.playground.dismissible")
                Toggle(DemoStrings.string(.action), isOn: $showsAction)
                    .accessibilityIdentifier("demo.alert.playground.action")
                if isShown {
                    playgroundAlert
                        .accessibilityIdentifier("demo.alert.playground.alert")
                }
                TSButton(DemoStrings.string(.showAgain), variant: .secondary) {
                    isShown = true
                }
                .disabled(isShown)
                .accessibilityIdentifier("demo.alert.playground.show")
                DemoTapCounter(count: actionCount, identifier: "demo.alert.playground.count")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }

    @ViewBuilder
    private var playgroundAlert: some View {
        let title = showsTitle ? SampleMessages.offline.title : nil
        let onDismiss: (() -> Void)? = isDismissible ? { isShown = false; hasBeenDismissed = true } : nil
        if showsAction {
            TSAlert(tone: tone, title: title, message: SampleMessages.offline.body, announcesOnAppear: hasBeenDismissed, onDismiss: onDismiss) {
                TSButton(SampleMessages.offline.actionTitle ?? "", variant: .link) { actionCount += 1 }
                    .accessibilityIdentifier("demo.alert.playground.retry")
            }
        } else {
            TSAlert(tone: tone, title: title, message: SampleMessages.offline.body, announcesOnAppear: hasBeenDismissed, onDismiss: onDismiss)
        }
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
