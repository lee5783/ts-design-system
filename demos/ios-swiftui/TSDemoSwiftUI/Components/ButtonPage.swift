import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct ButtonPage: View {
    @State private var variant: TSButtonVariant = .primary
    @State private var size: TSButtonSize = .md
    @State private var isLoading = false
    @State private var isDisabled = false
    @State private var isBlock = false
    @State private var hasLeadingIcon = false
    @State private var hasTrailingIcon = false
    @State private var tapCount = 0

    var body: some View {
        DemoPage(page: .button) {
            DemoComponentSection(.variants) {
                TSButton(DemoStrings.string(.sampleSave), variant: .primary) {}
                TSButton(DemoStrings.string(.sampleCancel), variant: .secondary) {}
                TSButton(DemoStrings.string(.sampleAddItem), variant: .ghost, leadingIcon: .plus) {}
                TSButton(DemoStrings.string(.sampleDelete), variant: .danger, leadingIcon: .trash) {}
                TSButton(DemoStrings.string(.sampleLearnMore), variant: .link) {}
            }
            DemoComponentSection(.sizes) {
                TSButton(DemoStrings.string(.sampleContinue), size: .md, trailingIcon: .arrowRight) {}
                TSButton(DemoStrings.string(.sampleGetStarted), size: .lg) {}
                TSButton(DemoStrings.string(.block), size: .lg, isBlock: true) {}
            }
            DemoComponentSection(.states) {
                ForEach(TSButtonVariant.allCases, id: \.self) { variant in
                    ForEach(TSButtonSize.allCases, id: \.self) { size in
                        DemoCaption(text: "\(variant.rawValue) · \(size.rawValue)")
                        DemoComponentRow(isStacked: size == .lg) {
                            TSButton(DemoStrings.string(.stateDefault), variant: variant, size: size) {}
                            TSButton(DemoStrings.string(.stateDisabled), variant: variant, size: size) {}
                                .disabled(true)
                            TSButton(DemoStrings.string(.stateLoading), variant: variant, size: size, isLoading: true) {}
                        }
                    }
                }
            }
            DemoComponentSection(.withIcons) {
                TSButton(DemoStrings.string(.leadingIcon), variant: .secondary, leadingIcon: .download) {}
                TSButton(DemoStrings.string(.trailingIcon), variant: .secondary, trailingIcon: .chevronRight) {}
                TSButton(DemoStrings.string(.bothIcons), variant: .secondary, leadingIcon: .upload, trailingIcon: .arrowRight) {}
            }
            DemoComponentSection(.consumerIcon) {
                TSButton(DemoStrings.string(.samplePlayTogether), leadingIconSource: .image(Image("DemoController"))) {}
                    .accessibilityIdentifier("demo.button.consumer-icon")
                TSButton(DemoStrings.string(.samplePlayTogether), variant: .secondary, leadingIconSource: .image(Image("DemoController"))) {}
                TSButton(DemoStrings.string(.samplePlayTogether), leadingIconSource: .image(Image("DemoController")), isLoading: true) {}
            }
            DemoComponentSection(.lineLimit) {
                TSButton(DemoStrings.string(.sampleJoinTournament), variant: .secondary, leadingIconSource: .image(Image("DemoController"))) {}
                    .lineLimit(1)
                    .frame(maxWidth: 240)
                    .accessibilityIdentifier("demo.button.line-limit")
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.variant), selection: $variant) {
                    ForEach(TSButtonVariant.allCases, id: \.self) { Text($0.rawValue).tag($0) }
                }
                .pickerStyle(.segmented)
                .accessibilityIdentifier("demo.button.playground.variant")
                Picker(DemoStrings.string(.size), selection: $size) {
                    ForEach(TSButtonSize.allCases, id: \.self) { Text($0.rawValue).tag($0) }
                }
                .pickerStyle(.segmented)
                Toggle(DemoStrings.string(.stateLoading), isOn: $isLoading)
                    .accessibilityIdentifier("demo.button.playground.loading")
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.button.playground.disabled")
                Toggle(DemoStrings.string(.block), isOn: $isBlock)
                Toggle(DemoStrings.string(.leadingIcon), isOn: $hasLeadingIcon)
                Toggle(DemoStrings.string(.trailingIcon), isOn: $hasTrailingIcon)
                TSButton(
                    DemoStrings.string(.sampleSave),
                    variant: variant,
                    size: size,
                    leadingIcon: hasLeadingIcon ? .check : nil,
                    trailingIcon: hasTrailingIcon ? .arrowRight : nil,
                    isLoading: isLoading,
                    isBlock: isBlock
                ) {
                    tapCount += 1
                }
                .disabled(isDisabled)
                .accessibilityIdentifier("demo.button.playground.button")
                DemoTapCounter(count: tapCount, identifier: "demo.button.playground.count")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }
}
