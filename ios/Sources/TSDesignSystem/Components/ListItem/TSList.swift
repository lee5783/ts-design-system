import SwiftUI

public struct TSList<Content: View>: View {
    let content: Content

    public init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    public var body: some View {
        let group = TSListItemStyleSpec.group
        let shape = RoundedRectangle(cornerRadius: group.cornerRadius, style: .continuous)
        LazyVStack(spacing: 0) {
            content
        }
        .environment(\.tsListShowsDividers, true)
        .background(shape.fill(group.fill.color))
        .clipShape(shape)
        .overlay(shape.strokeBorder(group.border.color, lineWidth: group.borderWidth).accessibilityHidden(true))
    }
}

public struct TSListSection<Content: View>: View {
    let header: String?
    let content: Content

    public init(_ header: String? = nil, @ViewBuilder content: () -> Content) {
        self.header = header
        self.content = content()
    }

    public var body: some View {
        let group = TSListItemStyleSpec.group
        VStack(alignment: .leading, spacing: group.headerSpacing) {
            if let header {
                Text(header)
                    .foregroundColor(group.headerColor.color)
                    .fixedSize(horizontal: false, vertical: true)
                    .tsFont(group.headerFont)
                    .padding(.horizontal, group.headerHorizontalPadding)
            }
            TSList {
                content
            }
        }
    }
}

struct TSListShowsDividersKey: EnvironmentKey {
    static let defaultValue = false
}

extension EnvironmentValues {
    var tsListShowsDividers: Bool {
        get { self[TSListShowsDividersKey.self] }
        set { self[TSListShowsDividersKey.self] = newValue }
    }
}
