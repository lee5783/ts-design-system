import SwiftUI
import TSDesignSystem

struct IconGalleryView: View {
    static let names: [TSIconName] = [.home, .bell, .chevronRight, .arrowLeft, .logout, .check]

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space4) {
            ForEach(TSIconSize.allCases, id: \.self) { size in
                HStack(spacing: TSSpacing.space3) {
                    ForEach(Self.names, id: \.self) { name in
                        TSIcon(name, size: size)
                    }
                }
                .foregroundColor(TSColor.textPrimary.color)
            }
            HStack(spacing: TSSpacing.space3) {
                TSIcon(.checkCircle, size: .lg, label: "Success")
                    .foregroundColor(TSColor.success.color)
                TSIcon(.alert, size: .lg, label: "Warning")
                    .foregroundColor(TSColor.warning.color)
                TSIcon(.xCircle, size: .lg, label: "Error")
                    .foregroundColor(TSColor.danger.color)
                TSIcon(.info, size: .lg, label: "Information")
                    .foregroundColor(TSColor.accentText.color)
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
