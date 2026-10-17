import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct IconButtonPage: View {
    @State private var variant: TSIconButtonVariant = .ghost
    @State private var isRound = false
    @State private var isDisabled = false
    @State private var tapCount = 0

    var body: some View {
        DemoPage(page: .iconButton) {
            DemoComponentSection(.variants) {
                ForEach(TSIconButtonVariant.allCases, id: \.self) { variant in
                    HStack(spacing: TSSpacing.space3) {
                        TSIconButton(.search, label: DemoStrings.string(.sampleSearch), variant: variant) {}
                        DemoCaption(text: variant.rawValue)
                    }
                }
            }
            DemoComponentSection(.shapes) {
                HStack(spacing: TSSpacing.space3) {
                    TSIconButton(.plus, label: DemoStrings.string(.sampleAdd), variant: .primary) {}
                    TSIconButton(.plus, label: DemoStrings.string(.sampleAdd), variant: .primary, isRound: true) {}
                    TSIconButton(.more, label: DemoStrings.string(.sampleMoreOptions), variant: .secondary, isRound: true) {}
                }
            }
            DemoComponentSection(.states) {
                ForEach(TSIconButtonVariant.allCases, id: \.self) { variant in
                    ForEach([false, true], id: \.self) { isRound in
                        DemoCaption(text: isRound ? "\(variant.rawValue) · \(DemoStrings.string(.round))" : variant.rawValue)
                        HStack(spacing: TSSpacing.space3) {
                            TSIconButton(.close, label: DemoStrings.string(.sampleClose), variant: variant, isRound: isRound) {}
                            TSIconButton(.close, label: DemoStrings.string(.sampleClose), variant: variant, isRound: isRound) {}
                                .disabled(true)
                        }
                    }
                }
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.variant), selection: $variant) {
                    ForEach(TSIconButtonVariant.allCases, id: \.self) { Text($0.rawValue).tag($0) }
                }
                .pickerStyle(.segmented)
                Toggle(DemoStrings.string(.round), isOn: $isRound)
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.icon-button.playground.disabled")
                TSIconButton(.settings, label: DemoStrings.string(.sampleMoreOptions), variant: variant, isRound: isRound) {
                    tapCount += 1
                }
                .disabled(isDisabled)
                .accessibilityIdentifier("demo.icon-button.playground.button")
                DemoTapCounter(count: tapCount, identifier: "demo.icon-button.playground.count")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }
}
