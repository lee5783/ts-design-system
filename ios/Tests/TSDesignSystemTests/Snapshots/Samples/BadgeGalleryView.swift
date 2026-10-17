import SwiftUI
import TSDesignSystem

struct BadgeGalleryView: View {
    static let samples: [(tone: TSBadgeTone, text: String, icon: TSIconName)] = [
        (.neutral, "Draft", .edit),
        (.accent, "New", .star),
        (.success, "Active", .checkCircle),
        (.warning, "Pending", .alert),
        (.danger, "Failed", .xCircle),
        (.info, "Beta", .info),
    ]

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            ForEach(Self.samples, id: \.tone) { sample in
                row {
                    TSBadge(sample.text, tone: sample.tone)
                    TSBadge(sample.text, tone: sample.tone, showsDot: true)
                    TSBadge(sample.text, tone: sample.tone, icon: sample.icon)
                }
            }
            TSBadge("Needs review by the design team", tone: .warning, showsDot: true)
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private func row<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        if dynamicTypeSize.isAccessibilitySize {
            VStack(alignment: .leading, spacing: TSSpacing.space2) { content() }
        } else {
            HStack(spacing: TSSpacing.space2) { content() }
        }
    }
}
