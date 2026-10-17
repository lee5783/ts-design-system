import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct DemoSettingsSheet: View {
    @EnvironmentObject private var settings: DemoSettingsModel
    @Environment(\.presentationMode) private var presentationMode

    var body: some View {
        DemoNavigationContainer {
            Form {
                Section(header: Text(DemoStrings.string(.settingsTheme))) {
                    Picker(DemoStrings.string(.settingsTheme), selection: $settings.theme) {
                        ForEach(DemoTheme.allCases) { theme in
                            Text(theme.title).tag(theme)
                        }
                    }
                    .pickerStyle(.segmented)
                    .accessibilityIdentifier("demo.settings.theme")
                }
                Section(header: Text(DemoStrings.string(.settingsTextSize))) {
                    Picker(DemoStrings.string(.settingsTextSize), selection: $settings.textSize) {
                        ForEach(DemoTextSize.allCases) { textSize in
                            Text(textSize.title).tag(textSize)
                        }
                    }
                    .accessibilityIdentifier("demo.settings.text-size")
                    Text(DemoStrings.string(.settingsTextSizePreview))
                        .foregroundColor(TSColor.textSecondary.color)
                        .tsFont(.body)
                        .accessibilityIdentifier("demo.settings.text-size.preview")
                }
                Section {
                    Toggle(DemoStrings.string(.settingsRightToLeft), isOn: $settings.isRightToLeft)
                        .accessibilityIdentifier("demo.settings.right-to-left")
                    Toggle(DemoStrings.string(.settingsReduceMotion), isOn: $settings.reduceMotion)
                        .accessibilityIdentifier("demo.settings.reduce-motion")
                }
                Section {
                    Button(DemoStrings.string(.settingsReset)) {
                        settings.reset()
                    }
                    .disabled(settings.isDefault)
                    .accessibilityIdentifier("demo.settings.reset")
                }
            }
            .navigationTitle(DemoStrings.string(.settingsTitle))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(DemoStrings.string(.done)) {
                        presentationMode.wrappedValue.dismiss()
                    }
                    .accessibilityIdentifier("demo.settings.done")
                }
            }
        }
    }
}
