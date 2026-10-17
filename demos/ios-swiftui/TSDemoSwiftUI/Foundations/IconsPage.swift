import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct IconsPage: View {
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        DemoPage(page: .icons) {
            DemoComponentSection(.sizes) {
                ForEach(TSIconSize.allCases, id: \.self) { size in
                    HStack(spacing: TSSpacing.space3) {
                        TSIcon(.home, size: size)
                        TSIcon(.chevronRight, size: size)
                        TSIcon(.bell, size: size)
                        DemoCaption(text: "\(size.rawValue) · \(Int(size.pointSize)) pt")
                    }
                    .foregroundColor(TSColor.textPrimary.color)
                }
            }
            DemoComponentSection(.labelled) {
                HStack(spacing: TSSpacing.space3) {
                    TSIcon(.checkCircle, size: .lg, label: DemoStrings.string(.sampleSuccess))
                        .foregroundColor(TSColor.success.color)
                    TSIcon(.alert, size: .lg, label: DemoStrings.string(.sampleWarning))
                        .foregroundColor(TSColor.warning.color)
                    TSIcon(.xCircle, size: .lg, label: DemoStrings.string(.sampleError))
                        .foregroundColor(TSColor.danger.color)
                    TSIcon(.info, size: .lg, label: DemoStrings.string(.sampleInformation))
                        .foregroundColor(TSColor.info.color)
                }
            }
            LazyVGrid(columns: [GridItem(.adaptive(minimum: dynamicTypeSize.isAccessibilitySize ? 280 : 88), spacing: TSSpacing.space3)], spacing: TSSpacing.space4) {
                ForEach(TSIconName.allCases, id: \.self) { icon in
                    VStack(spacing: TSSpacing.space2) {
                        TSIcon(icon, size: .lg)
                            .foregroundColor(TSColor.textPrimary.color)
                        Text(icon.assetName)
                            .foregroundColor(TSColor.textSecondary.color)
                            .tsFont(.caption)
                            .multilineTextAlignment(.center)
                        if icon.flipsForRightToLeftLayoutDirection {
                            Text(DemoStrings.string(.mirrorsInRightToLeft))
                                .foregroundColor(TSColor.accentText.color)
                                .tsFont(.overline)
                                .multilineTextAlignment(.center)
                        }
                    }
                    .frame(maxWidth: .infinity, minHeight: TSSpacing.space16, alignment: .top)
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("demo.token.\(icon.assetName)")
                }
            }
        }
    }
}
