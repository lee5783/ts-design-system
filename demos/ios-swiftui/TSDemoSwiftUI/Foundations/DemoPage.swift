import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct DemoPage<Content: View>: View {
    let page: DemoPageID
    private let content: Content

    init(page: DemoPageID, @ViewBuilder content: () -> Content) {
        self.page = page
        self.content = content()
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: TSSpacing.space4) {
                content
            }
            .padding(TSSpacing.space4)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(TSColor.backgroundPrimary.color.edgesIgnoringSafeArea(.all))
        .navigationTitle(page.title)
        .navigationBarTitleDisplayMode(.inline)
        .demoThemeToolbar()
        .accessibilityIdentifier(page.accessibilityIdentifier)
    }
}

struct TokenRow<Visual: View>: View {
    let name: String
    let detail: String
    private let visual: Visual

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    init(name: String, detail: String, @ViewBuilder visual: () -> Visual) {
        self.name = name
        self.detail = detail
        self.visual = visual()
    }

    var body: some View {
        Group {
            if dynamicTypeSize.isAccessibilitySize {
                VStack(alignment: .leading, spacing: TSSpacing.space2) {
                    visual
                    labels
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            } else {
                HStack(alignment: .center, spacing: TSSpacing.space3) {
                    visual
                    labels
                    Spacer(minLength: 0)
                }
            }
        }
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("demo.token.\(name)")
    }

    private var labels: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space0_5) {
            Text(name)
                .foregroundColor(TSColor.textPrimary.color)
                .tsFont(.label)
            Text(detail)
                .foregroundColor(TSColor.textMuted.color)
                .tsFont(.code)
        }
    }
}

enum TokenFormat {
    static func points(_ value: CGFloat) -> String {
        value.rounded() == value ? "\(Int(value)) pt" : "\(value) pt"
    }

    static func hex(_ rgba: TSRGBA) -> String {
        let rgb = String(format: "#%02X%02X%02X", rgba.red, rgba.green, rgba.blue)
        return rgba.alpha == 0xFF ? rgb : rgb + String(format: " %d%%", Int((rgba.opacity * 100).rounded()))
    }
}
