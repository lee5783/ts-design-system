import SwiftUI
import TSDemoSupport

extension View {
    func demoSettings(_ settings: DemoSettingsModel) -> some View {
        modifier(DemoSettingsModifier(settings: settings))
    }
}

private struct DemoSettingsModifier: ViewModifier {
    @ObservedObject var settings: DemoSettingsModel

    @Environment(\.dynamicTypeSize) private var systemDynamicTypeSize

    func body(content: Content) -> some View {
        content
            .preferredColorScheme(settings.theme.colorScheme)
            .background(
                DemoWindowAppearance(theme: settings.theme)
                    .frame(width: 0, height: 0)
                    .accessibilityHidden(true)
            )
            .dynamicTypeSize(settings.textSize.dynamicTypeSize ?? systemDynamicTypeSize)
            .environment(\.layoutDirection, settings.isRightToLeft ? .rightToLeft : .leftToRight)
            .transaction { transaction in
                if settings.reduceMotion {
                    transaction.disablesAnimations = true
                }
            }
    }
}

extension DemoTheme {
    var colorScheme: ColorScheme? {
        switch self {
        case .system:
            nil
        case .light:
            .light
        case .dark:
            .dark
        }
    }
}

extension DemoTextSize {
    var dynamicTypeSize: DynamicTypeSize? {
        switch self {
        case .system:
            nil
        case .extraSmall:
            .xSmall
        case .small:
            .small
        case .medium:
            .medium
        case .large:
            .large
        case .extraLarge:
            .xLarge
        case .extraExtraLarge:
            .xxLarge
        case .extraExtraExtraLarge:
            .xxxLarge
        case .accessibility1:
            .accessibility1
        case .accessibility2:
            .accessibility2
        case .accessibility3:
            .accessibility3
        case .accessibility4:
            .accessibility4
        case .accessibility5:
            .accessibility5
        }
    }
}
