import SwiftUI

public struct TSBadge: View {
    let text: String
    let tone: TSBadgeTone
    let showsDot: Bool
    let icon: TSIconName?

    public init(_ text: String, tone: TSBadgeTone = .neutral, showsDot: Bool = false, icon: TSIconName? = nil) {
        self.text = text
        self.tone = tone
        self.showsDot = showsDot
        self.icon = icon
    }

    public var body: some View {
        let resolved = TSBadgeStyleSpec.resolve(.init(tone: tone, hasDot: showsDot, hasIcon: icon != nil))
        HStack(spacing: resolved.spacing) {
            if resolved.showsDot {
                Circle()
                    .fill(resolved.foreground.color)
                    .frame(width: resolved.dotSize, height: resolved.dotSize)
            }
            if let icon, resolved.showsIcon {
                TSIcon(icon, size: resolved.iconSize)
            }
            Text(text)
                .lineLimit(1)
                .tsFont(resolved.font)
        }
        .foregroundColor(resolved.foreground.color)
        .padding(.horizontal, resolved.horizontalPadding)
        .padding(.vertical, resolved.verticalPadding)
        .frame(minHeight: resolved.minHeight)
        .tsControlChrome(
            RoundedRectangle(cornerRadius: resolved.cornerRadius, style: .continuous),
            fill: resolved.fill,
            border: resolved.border,
            borderWidth: resolved.borderWidth
        )
        .fixedSize(horizontal: false, vertical: true)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(text))
        .accessibilityAddTraits(.isStaticText)
    }
}
