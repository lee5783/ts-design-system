import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct DemoThemeToggle: View {
    @EnvironmentObject private var settings: DemoSettingsModel

    var body: some View {
        Button {
            settings.theme = settings.theme.next
        } label: {
            Image(systemName: settings.theme.symbolName)
                .font(.system(size: TSSize.iconLg * 0.8, weight: .regular))
                .frame(width: TSSize.iconLg, height: TSSize.iconLg)
                .accessibilityHidden(true)
        }
        .accessibilityLabel(DemoStrings.themeToggleLabel(settings.theme))
        .accessibilityIdentifier("demo.action.theme")
    }
}

extension View {
    func demoThemeToolbar() -> some View {
        toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                DemoThemeToggle()
            }
        }
    }
}
