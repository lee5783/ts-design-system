import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct DemoComponentSection<Content: View>: View {
    let title: String
    private let content: Content

    init(_ key: DemoStringKey, @ViewBuilder content: () -> Content) {
        self.title = DemoStrings.string(key)
        self.content = content()
    }

    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            Text(title)
                .foregroundColor(TSColor.textMuted.color)
                .tsFont(.overline)
                .accessibilityAddTraits(.isHeader)
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.bottom, TSSpacing.space2)
    }
}

struct DemoComponentRow<Content: View>: View {
    private let isStacked: Bool
    private let content: Content

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    init(isStacked: Bool = false, @ViewBuilder content: () -> Content) {
        self.isStacked = isStacked
        self.content = content()
    }

    var body: some View {
        if isStacked || dynamicTypeSize.isAccessibilitySize {
            VStack(alignment: .leading, spacing: TSSpacing.space3) {
                content
            }
        } else {
            HStack(alignment: .center, spacing: TSSpacing.space3) {
                content
            }
        }
    }
}

struct DemoCaption: View {
    let text: String

    var body: some View {
        Text(text)
            .foregroundColor(TSColor.textSecondary.color)
            .tsFont(.code)
    }
}

struct DemoTapCounter: View {
    let count: Int
    let identifier: String

    var body: some View {
        Text(DemoStrings.tapCount(count))
            .foregroundColor(TSColor.textSecondary.color)
            .tsFont(.bodySm)
            .accessibilityIdentifier(identifier)
    }
}
