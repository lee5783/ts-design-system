import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct CheckboxPage: View {
    @State private var isOn = false
    @State private var isDisabled = false
    @State private var isLabelHidden = false
    @State private var changeCount = 0
    @State private var samples = [false, true]
    @State private var hiddenSamples = [false, true]
    @State private var acceptsLongConsent = true

    var body: some View {
        DemoPage(page: .checkbox) {
            DemoComponentSection(.states) {
                ForEach([false, true], id: \.self) { isDisabled in
                    ForEach([false, true], id: \.self) { isOn in
                        DemoComponentRow {
                            TSCheckbox(DemoStrings.string(.sampleEmailUpdates), isOn: .constant(isOn))
                                .disabled(isDisabled)
                            DemoCaption(text: CheckboxSample.caption(isOn: isOn, isDisabled: isDisabled))
                        }
                    }
                }
                ForEach(samples.indices, id: \.self) { index in
                    TSCheckbox(CheckboxSample.label(index), isOn: $samples[index])
                }
            }
            DemoComponentSection(.labelHidden) {
                HStack(spacing: TSSpacing.space4) {
                    ForEach(hiddenSamples.indices, id: \.self) { index in
                        TSCheckbox(CheckboxSample.label(index), isOn: $hiddenSamples[index], isLabelHidden: true)
                    }
                }
            }
            DemoComponentSection(.wrapping) {
                TSCheckbox(DemoStrings.string(.sampleLongConsent), isOn: $acceptsLongConsent)
            }
            DemoComponentSection(.playground) {
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.checkbox.playground.disabled")
                Toggle(DemoStrings.string(.labelHidden), isOn: $isLabelHidden)
                    .accessibilityIdentifier("demo.checkbox.playground.label-hidden")
                TSCheckbox(DemoStrings.string(.sampleAcceptTerms), isOn: countedIsOn, isLabelHidden: isLabelHidden)
                    .disabled(isDisabled)
                    .accessibilityIdentifier("demo.checkbox.playground.checkbox")
                DemoCaption(text: DemoStrings.string(isOn ? .stateChecked : .stateUnchecked))
                    .accessibilityIdentifier("demo.checkbox.playground.value")
                DemoTapCounter(count: changeCount, identifier: "demo.checkbox.playground.count")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }

    private var countedIsOn: Binding<Bool> {
        Binding(
            get: { isOn },
            set: { newValue in
                isOn = newValue
                changeCount += 1
            }
        )
    }
}

enum CheckboxSample {
    static func label(_ index: Int) -> String {
        DemoStrings.string(index == 0 ? .sampleRememberMe : .sampleAcceptTerms)
    }

    static func caption(isOn: Bool, isDisabled: Bool) -> String {
        let value = DemoStrings.string(isOn ? .stateChecked : .stateUnchecked)
        return isDisabled ? "\(value), \(DemoStrings.string(.stateDisabled))" : value
    }
}
