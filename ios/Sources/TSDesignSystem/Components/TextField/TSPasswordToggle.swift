import SwiftUI

struct TSPasswordToggle: View {
    @Binding var isRevealed: Bool
    let onToggle: () -> Void

    var body: some View {
        let resolved = TSTextFieldStyleSpec.passwordToggle(isRevealed: isRevealed)
        Button {
            isRevealed.toggle()
            onToggle()
        } label: {
            TSIcon(resolved.icon, size: resolved.iconSize)
                .foregroundColor(resolved.iconColor.color)
                .frame(width: resolved.size, height: resolved.size)
                .tsControlChrome(
                    RoundedRectangle(cornerRadius: resolved.cornerRadius, style: .continuous),
                    fill: resolved.fill,
                    border: nil,
                    borderWidth: 0
                )
                .tsMinimumHitArea(outset: TSHitArea.outset(for: resolved.size, minimum: resolved.minimumHitSize))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(TSTextFieldAccessibility.toggleLabel(isRevealed: isRevealed)))
    }
}
