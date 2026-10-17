import UIKit
@testable import TSDesignSystemUIKit

enum TextFieldGalleryPage: String, CaseIterable {
    case states
    case content
}

enum TextFieldSample {
    static let longText = (1...9).map { "Line \($0) of a long note" }.joined(separator: "\n")
    static let shortText = "Loves maths and\nanalytical engines"
}

final class TextFieldGalleryUIView: GalleryStackView {
    init(page: TextFieldGalleryPage) {
        super.init(frame: .zero)
        stack.alignment = .fill
        stack.spacing = TSSpacing.space5
        switch page {
        case .states:
            addStates()
        case .content:
            addContent()
        }
    }

    private func addStates() {
        stack.addArrangedSubview(TSTextFieldView(label: "Default", placeholder: "you@example.com", hint: "We never share your email"))
        stack.addArrangedSubview(TSTextFieldView(label: "Filled", text: "ada@example.com"))
        let focused = TSTextFieldView(label: "Focused", text: "ada@exa")
        focused.forcedFocus = true
        stack.addArrangedSubview(focused)
        stack.addArrangedSubview(TSTextFieldView(label: "Error", text: "abc", hint: "At least 8 characters", errorMessage: "Must be at least 8 characters"))
        let errorFocused = TSTextFieldView(label: "Error focused", text: "abc", errorMessage: "Must be at least 8 characters")
        errorFocused.forcedFocus = true
        stack.addArrangedSubview(errorFocused)
        let disabled = TSTextFieldView(label: "Disabled", text: "Ada Lovelace", hint: "Managed by your organisation")
        disabled.isEnabled = false
        stack.addArrangedSubview(disabled)
        stack.addArrangedSubview(TSTextFieldView(label: "Read-only", text: "TS-2048-7731", hint: "Selectable, not editable", isReadOnly: true))
    }

    private func addContent() {
        let search = TSTextFieldView(label: "Search", text: "Analytical", placeholder: "Search", leadingIcon: .search)
        search.trailingView = TSIconButtonView(icon: .close, label: "Clear")
        stack.addArrangedSubview(search)
        stack.addArrangedSubview(TSTextFieldView(label: "Password", text: "hunter22", isSecure: true))
        let revealed = TSTextFieldView(label: "Password revealed", text: "hunter22", isSecure: true)
        revealed.isRevealed = true
        stack.addArrangedSubview(revealed)
        stack.addArrangedSubview(TSTextFieldView(label: "Bio", text: TextFieldSample.shortText, placeholder: "Tell us about yourself", isMultiline: true))
        stack.addArrangedSubview(TSTextFieldView(label: "Notes", text: TextFieldSample.longText, isMultiline: true))
    }
}
