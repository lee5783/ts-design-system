import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSTextFieldView behaviour")
struct TSTextFieldViewBehaviorTests {
    private func makeField(_ field: TSTextFieldView, width: CGFloat = 320) -> TSTextFieldView {
        let size = field.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .required,
            verticalFittingPriority: .fittingSizeLevel
        )
        field.frame = CGRect(origin: .zero, size: size)
        field.layoutIfNeeded()
        return field
    }

    @Test("Editing the inner field updates text and fires .editingChanged")
    func editing() {
        let counter = TapCounter()
        let field = makeField(TSTextFieldView(label: "Email", placeholder: "you@example.com"))
        field.addAction(UIAction { _ in counter.tap() }, for: .editingChanged)
        field.textField.text = "ada@example.com"
        field.textField.sendActions(for: .editingChanged)
        #expect(field.text == "ada@example.com")
        #expect(counter.count == 1)
        field.text = "grace@example.com"
        #expect(field.textField.text == "grace@example.com")
        #expect(counter.count == 1)
    }

    @Test("The placeholder is the field's attributed placeholder")
    func placeholder() {
        let field = makeField(TSTextFieldView(label: "Email", placeholder: "you@example.com", hint: "We never share it"))
        #expect(field.textField.attributedPlaceholder?.string == "you@example.com")
    }

    @Test("An error replaces the hint and shows the alert icon")
    func errorState() {
        let field = makeField(TSTextFieldView(label: "Password", text: "abc", hint: "At least 8 characters"))
        field.errorMessage = "Must be at least 8 characters"
        #expect(field.messageLabel.attributedText?.string == "Must be at least 8 characters")
        #expect(!field.messageIconView.isHidden)
        #expect(field.boxView.layer.borderColor == TSColor.danger.uiColor.resolvedColor(with: field.traitCollection).cgColor)
        field.errorMessage = nil
        #expect(field.messageLabel.attributedText?.string == "At least 8 characters")
        #expect(field.messageIconView.isHidden)
    }

    @Test("Focus draws the 2 pt focus border; error keeps the danger colour")
    func focusBorder() {
        let field = makeField(TSTextFieldView(label: "Name"))
        #expect(field.boxView.layer.borderWidth == 1)
        field.forcedFocus = true
        #expect(field.boxView.layer.borderWidth == 2)
        #expect(field.boxView.layer.borderColor == TSColor.focus.uiColor.resolvedColor(with: field.traitCollection).cgColor)
        field.errorMessage = "Required"
        #expect(field.boxView.layer.borderWidth == 2)
        #expect(field.boxView.layer.borderColor == TSColor.danger.uiColor.resolvedColor(with: field.traitCollection).cgColor)
    }

    @Test("Becoming first responder in a window focuses the field")
    func realFocus() {
        let host = TraitHost(sizeCategory: .large)
        let field = TSTextFieldView(label: "Name")
        host.content.addSubview(field)
        field.frame = CGRect(x: 0, y: 0, width: 320, height: 80)
        field.layoutIfNeeded()
        field.becomeFirstResponder()
        #expect(field.isEditingText == field.textField.isFirstResponder)
        if field.textField.isFirstResponder {
            #expect(field.boxView.layer.borderWidth == 2)
        }
        field.resignFirstResponder()
    }

    @Test("Resigning a focused multiline field resigns its text view and ends editing")
    func multilineResigns() throws {
        let host = TraitHost(sizeCategory: .large)
        host.window.makeKeyAndVisible()
        let field = TSTextFieldView(label: "Bio", isMultiline: true)
        host.content.addSubview(field)
        field.frame = CGRect(x: 0, y: 0, width: 320, height: 160)
        field.layoutIfNeeded()
        field.becomeFirstResponder()
        try #require(field.textView.isFirstResponder)
        #expect(field.resignFirstResponder())
        #expect(!field.textView.isFirstResponder)
        #expect(!field.textField.isFirstResponder)
    }

    @Test("Disabled blocks editing and dims the box")
    func disabledBlocks() {
        let field = makeField(TSTextFieldView(label: "Name", text: "Ada"))
        field.isEnabled = false
        #expect(!field.textField.isEnabled)
        #expect(!field.textFieldShouldBeginEditing(field.textField))
        #expect(abs(field.boxView.alpha - 0.45) < 0.001)
        #expect(field.boxView.backgroundColor == TSColor.backgroundSecondary.uiColor.resolvedColor(with: field.traitCollection))
    }

    @Test("Read-only is selectable but not editable and never shows a focus border")
    func readOnly() {
        let field = makeField(TSTextFieldView(label: "Account ID", text: "TS-2048", isReadOnly: true))
        #expect(field.textField.isHidden)
        #expect(!field.textView.isHidden)
        #expect(!field.textView.isEditable)
        #expect(field.textView.isSelectable)
        #expect(!field.textViewShouldBeginEditing(field.textView))
        #expect(field.textView.styledText == "TS-2048")
        field.forcedFocus = true
        #expect(field.boxView.layer.borderWidth == 1)
    }

    @Test("The secure toggle switches between masked and plain entry")
    func secureToggle() {
        let field = makeField(TSTextFieldView(label: "Password", text: "hunter22", isSecure: true))
        #expect(field.textField.isSecureTextEntry)
        #expect(!field.passwordToggle.isHidden)
        field.passwordToggle.sendActions(for: .primaryActionTriggered)
        #expect(!field.textField.isSecureTextEntry)
        #expect(field.passwordToggle.isRevealed)
        field.passwordToggle.sendActions(for: .primaryActionTriggered)
        #expect(field.textField.isSecureTextEntry)
    }

    @Test("The password toggle keeps a 44 pt hit area")
    func toggleHitArea() {
        let toggle = TSPasswordToggleButton()
        toggle.frame = CGRect(x: 0, y: 0, width: 32, height: 32)
        #expect(toggle.point(inside: CGPoint(x: 16, y: -5.5), with: nil))
        #expect(!toggle.point(inside: CGPoint(x: 16, y: -7), with: nil))
    }

    @Test("The password toggle keeps its spec radius at AX5, revealed or not", arguments: [false, true])
    func passwordToggleFixedCorners(isRevealed: Bool) {
        let toggle = TSPasswordToggleButton()
        toggle.isRevealed = isRevealed
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        host.content.addSubview(toggle)
        host.window.layoutIfNeeded()
        toggle.updateConfiguration()
        #expect(toggle.configuration?.cornerStyle == .fixed)
        #expect(toggle.configuration?.background.cornerRadius == toggle.resolved.cornerRadius)
    }

    @Test("A disabled password toggle icon renders like the enabled one; the field's 0.45 box alpha is the only dimming", arguments: [false, true])
    func disabledIconDimmedOnce(isRevealed: Bool) throws {
        let enabled = try passwordToggleInk(isRevealed: isRevealed, isEnabled: true)
        let disabled = try passwordToggleInk(isRevealed: isRevealed, isEnabled: false)
        #expect(enabled.alpha == 255)
        #expect(RenderedInk.isClose(disabled, enabled))
    }

    private func passwordToggleInk(isRevealed: Bool, isEnabled: Bool) throws -> RenderedInk.Pixel {
        let toggle = TSPasswordToggleButton()
        toggle.isRevealed = isRevealed
        toggle.isEnabled = isEnabled
        toggle.frame = CGRect(x: 0, y: 0, width: 32, height: 32)
        toggle.updateConfiguration()
        toggle.layoutIfNeeded()
        let imageView = try #require(toggle.imageView)
        return try #require(RenderedInk.strongest(in: imageView))
    }

    @Test("Multiline grows from 3 to 6 lines, then scrolls")
    func multilineGrows() {
        let field = makeField(TSTextFieldView(label: "Bio", isMultiline: true))
        #expect(field.textView.layout(forWidth: 280).height == 92)
        field.text = Array(repeating: "line", count: 5).joined(separator: "\n")
        #expect(field.textView.layout(forWidth: 280).height == 140)
        #expect(!field.textView.isScrollEnabled)
        field.text = Array(repeating: "line", count: 9).joined(separator: "\n")
        _ = makeField(field)
        #expect(field.textView.layout(forWidth: 280).height == 164)
        #expect(field.textView.isScrollEnabled)
        #expect(field.textView.bounds.height == 164)
    }

    @Test("Typing in the multiline view updates text")
    func multilineEditing() {
        let field = makeField(TSTextFieldView(label: "Bio", isMultiline: true))
        field.textView.text = "Hello\nWorld"
        NotificationCenter.default.post(name: UITextView.textDidChangeNotification, object: field.textView)
        #expect(field.text == "Hello\nWorld")
    }

    @Test("Keyboard and content types are applied to the input")
    func inputTraits() {
        let field = makeField(TSTextFieldView(label: "Email", inputTraits: .email))
        #expect(field.textField.keyboardType == .emailAddress)
        #expect(field.textField.textContentType == .emailAddress)
        #expect(field.textField.autocorrectionType == .no)
    }

    @Test("The code style uses the code font for the input only and keeps the text visible with code keyboard traits")
    func codeStyle() {
        let plain = makeField(TSTextFieldView(label: "Join code", text: "AB12CD", hint: "6 characters"))
        let code = TSTextFieldView(label: "Join code", text: "AB12CD", hint: "6 characters", inputTraits: .code)
        #expect(code.contentStyle == .default)
        code.contentStyle = .code
        _ = makeField(code)
        #expect(code.resolved.inputFont == .code)
        #expect(code.textField.font == TSFont.code.uiFont(for: code.traitCollection.preferredContentSizeCategory))
        #expect(code.textField.font?.fontDescriptor.symbolicTraits.contains(.traitMonoSpace) == true)
        #expect(code.labelView.font == plain.labelView.font)
        #expect(code.messageLabel.font == plain.messageLabel.font)
        #expect(!code.textField.isSecureTextEntry)
        #expect(code.textField.text == "AB12CD")
        #expect(code.textField.keyboardType == .asciiCapable)
        #expect(code.textField.autocapitalizationType == .allCharacters)
        #expect(code.textField.autocorrectionType == .no)
        #expect(code.boxView.bounds.height == plain.boxView.bounds.height)
    }

    @Test("The field grows with Dynamic Type")
    func dynamicType() {
        let regular = makeField(TSTextFieldView(label: "Name", hint: "As on your passport"))
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        let large = TSTextFieldView(label: "Name", hint: "As on your passport")
        host.content.addSubview(large)
        _ = makeField(large)
        #expect(large.bounds.height > regular.bounds.height)
        #expect(regular.boxView.bounds.height == 44)
    }

    @Test("A trailing view is placed inside the box")
    func trailingSlot() {
        let field = TSTextFieldView(label: "Search", leadingIcon: .search)
        let clear = UIButton()
        field.trailingView = clear
        #expect(clear.superview === field.contentStack)
        #expect(!field.leadingIconView.isHidden)
        field.trailingView = nil
        #expect(clear.superview == nil)
    }
}
