import SwiftUI
import TSDesignSystem

struct ColorSwatchGridView: View {
    static let columnCount = 3

    private var rows: [[TSColor]] {
        stride(from: 0, to: TSColor.allTokens.count, by: Self.columnCount).map {
            Array(TSColor.allTokens[$0..<min($0 + Self.columnCount, TSColor.allTokens.count)])
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space3) {
            ForEach(rows, id: \.first?.name) { row in
                HStack(alignment: .top, spacing: TSSpacing.space3) {
                    ForEach(row, id: \.name) { token in
                        swatch(token)
                    }
                    ForEach(row.count..<Self.columnCount, id: \.self) { _ in
                        Color.clear.frame(maxWidth: .infinity, maxHeight: 0)
                    }
                }
            }
        }
        .padding(TSSpacing.space4)
    }

    private func swatch(_ token: TSColor) -> some View {
        VStack(alignment: .leading, spacing: TSSpacing.space1) {
            RoundedRectangle(cornerRadius: TSRadius.radiusMd)
                .fill(token.color)
                .overlay(
                    RoundedRectangle(cornerRadius: TSRadius.radiusMd)
                        .stroke(TSColor.borderSubtle.color, lineWidth: 1)
                )
                .frame(height: 40)
            Text(token.name)
                .foregroundColor(TSColor.textSecondary.color)
                .tsFont(.caption)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
