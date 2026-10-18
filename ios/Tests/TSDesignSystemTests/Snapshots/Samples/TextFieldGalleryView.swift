import SwiftUI
@testable import TSDesignSystem

enum TextFieldGalleryPage: String, CaseIterable {
    case states
    case content
    case code
}

enum TextFieldSample {
    static let longText = (1...9).map { "Line \($0) of a long note" }.joined(separator: "\n")
    static let shortText = "Loves maths and\nanalytical engines"
}

struct TextFieldGalleryView: View {
    let page: TextFieldGalleryPage

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space5) {
            switch page {
            case .states:
                states
            case .content:
                content
            case .code:
                code
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private var states: some View {
        TSTextField("Default", text: .constant(""), placeholder: "you@example.com", hint: "We never share your email")
        TSTextField("Filled", text: .constant("ada@example.com"))
        TSTextField("Focused", text: .constant("ada@exa"))
            .forcingState(.focused)
        TSTextField("Error", text: .constant("abc"), hint: "At least 8 characters", error: "Must be at least 8 characters")
        TSTextField("Error focused", text: .constant("abc"), error: "Must be at least 8 characters")
            .forcingState([.error, .focused])
        TSTextField("Disabled", text: .constant("Ada Lovelace"), hint: "Managed by your organisation")
            .disabled(true)
        TSTextField("Read-only", text: .constant("TS-2048-7731"), hint: "Selectable, not editable", isReadOnly: true)
    }

    @ViewBuilder
    private var content: some View {
        TSTextField("Search", text: .constant("Analytical"), placeholder: "Search", leadingIcon: .search) {
            TSIconButton(.close, label: "Clear") {}
        }
        TSTextField("Password", text: .constant("hunter22"), isSecure: true)
        TSTextField("Password revealed", text: .constant("hunter22"), isSecure: true)
            .revealingPassword()
        TSTextField("Bio", text: .constant(TextFieldSample.shortText), placeholder: "Tell us about yourself", isMultiline: true)
            .usingMultilinePath(.native)
        TSTextField("Notes", text: .constant(TextFieldSample.longText), isMultiline: true)
            .usingMultilinePath(.native)
        TSTextField("Notes (iOS 15)", text: .constant(TextFieldSample.longText), isMultiline: true)
            .usingMultilinePath(.fallback)
    }

    @ViewBuilder
    private var code: some View {
        TSTextField("Idle", text: .constant(""), placeholder: "ABCDEF", hint: "6 characters, letters and digits", inputTraits: .code, contentStyle: .code)
        TSTextField("Filled", text: .constant("0O1I5S"), hint: "6 characters, letters and digits", inputTraits: .code, contentStyle: .code)
        TSTextField("Focused", text: .constant("0O1I5S"), hint: "6 characters, letters and digits", inputTraits: .code, contentStyle: .code)
            .forcingState(.focused)
        TSTextField("Error", text: .constant("0O1"), hint: "6 characters, letters and digits", error: "Enter all 6 characters", inputTraits: .code, contentStyle: .code)
        TSTextField("Disabled", text: .constant("0O1I5S"), hint: "6 characters, letters and digits", inputTraits: .code, contentStyle: .code)
            .disabled(true)
    }
}
