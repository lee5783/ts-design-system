import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct TextPage: View {
    var body: some View {
        DemoPage(page: .text) {
            DemoComponentSection(.typeRoles) {
                ForEach(TSTypeRole.allCases, id: \.self) { role in
                    VStack(alignment: .leading, spacing: TSSpacing.space1) {
                        DemoCaption(text: role.rawValue)
                        TSText(DemoStrings.string(.pangram), role: role)
                            .accessibilityIdentifier("demo.text.role.\(role.rawValue)")
                    }
                }
            }
            DemoComponentSection(.textColors) {
                ForEach(TSTextColor.allCases, id: \.self) { color in
                    TSText(color.rawValue, color: color)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(TSSpacing.space2)
                        .background(TextSample.background(for: color))
                        .accessibilityIdentifier("demo.text.color.\(color.rawValue)")
                }
            }
            DemoComponentSection(.truncation) {
                TSText(DemoStrings.string(.sampleLongSetting), role: .body)
                    .lineLimit(1)
                    .accessibilityIdentifier("demo.text.truncated")
            }
        }
    }
}

enum TextSample {
    static func background(for color: TSTextColor) -> Color {
        switch color {
        case .inverse: TSColor.surfaceInverse.color
        case .onAccent: TSColor.accentPrimary.color
        default: Color.clear
        }
    }
}
