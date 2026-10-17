import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct AboutView: View {
    private let buildInfo = DemoBuildInfo.load(from: .main)

    var body: some View {
        List {
            AboutRow(
                title: DemoStrings.string(.aboutLibraryVersion),
                value: buildInfo.libraryVersion,
                identifier: "demo.about.library-version"
            )
            AboutRow(
                title: DemoStrings.string(.aboutDesignSystemVersion),
                value: buildInfo.designSystemVersion,
                identifier: "demo.about.design-system-version"
            )
        }
        .listStyle(.insetGrouped)
        .navigationTitle(DemoStrings.string(.aboutTitle))
    }
}

private struct AboutRow: View {
    let title: String
    let value: String?
    let identifier: String

    var body: some View {
        HStack(alignment: .firstTextBaseline, spacing: TSSpacing.space3) {
            Text(title)
                .foregroundColor(TSColor.textPrimary.color)
                .tsFont(.body)
            Spacer(minLength: TSSpacing.space2)
            Text(value ?? DemoStrings.string(.aboutUnknown))
                .foregroundColor(TSColor.textSecondary.color)
                .tsFont(.code)
                .multilineTextAlignment(.trailing)
        }
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(identifier)
    }
}
