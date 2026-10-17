import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct SwitchPage: View {
    @State private var isOn = false
    @State private var isDisabled = false
    @State private var isLabelHidden = false
    @State private var changeCount = 0
    @State private var samples = [false, true]
    @State private var hiddenSamples = [false, true]
    @State private var downloadsOverMobileData = false

    var body: some View {
        DemoPage(page: .switch) {
            DemoComponentSection(.states) {
                ForEach([false, true], id: \.self) { isDisabled in
                    ForEach([false, true], id: \.self) { isOn in
                        DemoComponentRow {
                            TSSwitch(DemoStrings.string(.sampleWifi), isOn: .constant(isOn))
                                .disabled(isDisabled)
                            DemoCaption(text: SwitchSample.caption(isOn: isOn, isDisabled: isDisabled))
                        }
                    }
                }
                ForEach(samples.indices, id: \.self) { index in
                    TSSwitch(SwitchSample.label(index), isOn: $samples[index])
                }
            }
            DemoComponentSection(.labelHidden) {
                HStack(spacing: TSSpacing.space4) {
                    ForEach(hiddenSamples.indices, id: \.self) { index in
                        TSSwitch(SwitchSample.label(index), isOn: $hiddenSamples[index], isLabelHidden: true)
                    }
                }
            }
            DemoComponentSection(.wrapping) {
                TSSwitch(DemoStrings.string(.sampleLongSetting), isOn: $downloadsOverMobileData)
            }
            DemoComponentSection(.playground) {
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.switch.playground.disabled")
                Toggle(DemoStrings.string(.labelHidden), isOn: $isLabelHidden)
                    .accessibilityIdentifier("demo.switch.playground.label-hidden")
                TSSwitch(DemoStrings.string(.sampleDarkMode), isOn: countedIsOn, isLabelHidden: isLabelHidden)
                    .disabled(isDisabled)
                    .accessibilityIdentifier("demo.switch.playground.switch")
                DemoCaption(text: DemoStrings.string(isOn ? .stateOn : .stateOff))
                    .accessibilityIdentifier("demo.switch.playground.value")
                DemoTapCounter(count: changeCount, identifier: "demo.switch.playground.count")
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

enum SwitchSample {
    static func label(_ index: Int) -> String {
        DemoStrings.string(index == 0 ? .sampleNotifications : .sampleDarkMode)
    }

    static func caption(isOn: Bool, isDisabled: Bool) -> String {
        let value = DemoStrings.string(isOn ? .stateOn : .stateOff)
        return isDisabled ? "\(value), \(DemoStrings.string(.stateDisabled))" : value
    }
}
