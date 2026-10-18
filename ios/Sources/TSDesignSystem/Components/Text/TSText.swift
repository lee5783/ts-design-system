import SwiftUI

public struct TSText: View {
    let text: String
    let role: TSTypeRole
    let color: TSTextColor

    public init(_ text: String, role: TSTypeRole = .body, color: TSTextColor = .primary) {
        self.text = text
        self.role = role
        self.color = color
    }

    public var body: some View {
        Text(verbatim: text)
            .foregroundColor(color.color.color)
            .tsFont(role.font)
    }
}
