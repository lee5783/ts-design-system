import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
final class TextBox {
    var value: String

    init(_ value: String = "") {
        self.value = value
    }

    var binding: Binding<String> {
        Binding(get: { self.value }, set: { self.value = $0 })
    }
}

@MainActor
final class AnnouncementLog {
    private(set) var messages: [String] = []

    func record(_ message: String) {
        messages.append(message)
    }
}

@MainActor
@Suite("TSTextField behaviour")
struct TSTextFieldBehaviorTests {
    @Test("Typing writes through the text binding")
    func textBinding() throws {
        let box = TextBox()
        let sut = TSTextField("Email", text: box.binding, placeholder: "you@example.com")
        try sut.inspect().find(ViewType.TextField.self).setInput("ada@example.com")
        #expect(box.value == "ada@example.com")
        #expect(try sut.inspect().find(ViewType.TextField.self).input() == "ada@example.com")
    }

    @Test("The label is the accessibility label, not the placeholder, and the hint maps to the accessibility hint")
    func accessibilityAtRest() throws {
        let sut = TSTextField("Email", text: .constant(""), placeholder: "you@example.com", hint: "We never share it")
        let field = try sut.inspect().find(ViewType.TextField.self)
        #expect(try field.accessibilityLabel().string() == "Email")
        #expect(try field.accessibilityHint().string() == "We never share it")
        #expect(throws: (any Error).self) { try field.accessibilityValue() }
        #expect(try sut.inspect().find(text: "Email").string() == "Email")
        #expect(try sut.inspect().find(text: "We never share it").string() == "We never share it")
    }

    @Test("An error replaces the hint, shows the alert icon and sets the accessibility value")
    func errorState() throws {
        let sut = TSTextField("Password", text: .constant("abc"), hint: "At least 8 characters", error: "Must be at least 8 characters")
        let field = try sut.inspect().find(ViewType.TextField.self)
        #expect(try field.accessibilityValue().string() == "abc, Error: Must be at least 8 characters")
        #expect(try field.accessibilityHint().string() == "")
        #expect(throws: (any Error).self) { try sut.inspect().find(text: "At least 8 characters") }
        #expect(try sut.inspect().find(text: "Must be at least 8 characters").string() == "Must be at least 8 characters")
        let icon = try sut.inspect().find(TSIcon.self)
        #expect(try icon.actualView().name == .alert)
    }

    @available(iOS 17, *)
    @Test("A new error posts an announcement; clearing or repeating it does not")
    func errorAnnouncement() throws {
        let log = AnnouncementLog()
        let sut = TSTextField("Email", text: .constant(""), error: "Required").announcing { log.record($0) }
        let view = try sut.inspect().vStack()
        try view.callOnChange(oldValue: String?.none, newValue: String?.some("Required"))
        try view.callOnChange(oldValue: String?.some("Required"), newValue: String?.some("Required"))
        try view.callOnChange(oldValue: String?.some("Required"), newValue: String?.none)
        #expect(log.messages == ["Error: Required"])
    }

    @Test("Disabled blocks editing")
    func disabledBlocks() throws {
        let box = TextBox("ada")
        let sut = TSTextField("Name", text: box.binding).disabled(true)
        let field = try sut.inspect().find(ViewType.TextField.self)
        #expect(field.isDisabled())
        #expect(throws: (any Error).self) { try field.setInput("grace") }
        #expect(box.value == "ada")
    }

    @Test("Read-only renders selectable text and no editable field")
    func readOnly() throws {
        let sut = TSTextField("Account ID", text: .constant("TS-2048"), isReadOnly: true)
        #expect(throws: (any Error).self) { try sut.inspect().find(ViewType.TextField.self) }
        #expect(throws: (any Error).self) { try sut.inspect().find(ViewType.SecureField.self) }
        #expect(throws: Never.self) { try sut.inspect().find(text: "TS-2048") }
        let element = try sut.inspect().find(viewWithAccessibilityLabel: "Account ID")
        #expect(try element.accessibilityValue().string() == "TS-2048")
        #expect(throws: (any Error).self) { try sut.inspect().find(TSMultilineTextView.self) }
    }

    @Test("The read-only element spans the whole box, not just its text")
    func readOnlyElementFillsTheBox() throws {
        let sut = TSTextField("Account ID", text: .constant("TS-2048"), isReadOnly: true)
        let element = try sut.inspect().find(viewWithAccessibilityLabel: "Account ID")
        #expect(throws: Never.self) { try element.find(text: "TS-2048") }
        #expect(try element.flexFrame().maxWidth == .infinity)
    }

    @Test("Secure entry starts masked with a Show password toggle")
    func secureHidden() throws {
        let sut = TSTextField("Password", text: .constant("hunter22"), isSecure: true)
        #expect(throws: Never.self) { try sut.inspect().find(ViewType.SecureField.self) }
        #expect(throws: (any Error).self) { try sut.inspect().find(ViewType.TextField.self) }
        let toggle = try sut.inspect().find(TSPasswordToggle.self)
        #expect(try toggle.button().accessibilityLabel().string() == "Show password")
        #expect(try toggle.find(TSIcon.self).actualView().name == .eye)
    }

    @Test("Revealing the password switches to a plain field and a Hide password toggle")
    func secureRevealed() throws {
        let sut = TSTextField("Password", text: .constant("hunter22"), isSecure: true).revealingPassword()
        #expect(throws: Never.self) { try sut.inspect().find(ViewType.TextField.self) }
        #expect(throws: (any Error).self) { try sut.inspect().find(ViewType.SecureField.self) }
        let toggle = try sut.inspect().find(TSPasswordToggle.self)
        #expect(try toggle.button().accessibilityLabel().string() == "Hide password")
    }

    @Test("The toggle flips the reveal binding")
    func toggleFlips() throws {
        var isRevealed = false
        var toggled = 0
        let toggle = TSPasswordToggle(isRevealed: Binding(get: { isRevealed }, set: { isRevealed = $0 })) { toggled += 1 }
        try toggle.inspect().button().tap()
        #expect(isRevealed)
        #expect(toggled == 1)
    }

    @Test("Multiline uses TextField(axis:) on iOS 16+")
    func multilineNative() throws {
        let box = TextBox()
        let sut = TSTextField("Bio", text: box.binding, isMultiline: true).usingMultilinePath(.native)
        try sut.inspect().find(ViewType.TextField.self).setInput("Line one\nLine two")
        #expect(box.value == "Line one\nLine two")
        #expect(throws: (any Error).self) { try sut.inspect().find(TSMultilineTextView.self) }
    }

    @Test("Multiline on the iOS 15 path uses the UITextView bridge")
    func multilineFallback() throws {
        let sut = TSTextField("Bio", text: .constant(""), isMultiline: true).usingMultilinePath(.fallback)
        #expect(throws: Never.self) { try sut.inspect().find(TSMultilineTextView.self) }
        #expect(throws: (any Error).self) { try sut.inspect().find(ViewType.TextField.self) }
    }

    @Test("The bridge grows from 3 to 6 lines, then scrolls, and writes edits back")
    func bridgeGrows() throws {
        let box = TextBox()
        var isEditing = false
        let bridge = TSMultilineTextView(
            text: box.binding,
            isEditing: Binding(get: { isEditing }, set: { isEditing = $0 }),
            placeholder: "Tell us about yourself",
            font: .body,
            textColor: .textPrimary,
            placeholderColor: .textMuted,
            verticalInset: 10,
            isEditable: true,
            inputTraits: .default,
            sizeCategory: .large,
            layoutDirection: .leftToRight
        )
        let coordinator = bridge.makeCoordinator()
        let textView = TSGrowingTextView()
        textView.delegate = coordinator
        textView.verticalInset = 10
        textView.apply(.init(font: .body, color: .label, placeholder: "Tell us", placeholderColor: .gray, sizeCategory: .large))
        textView.frame = CGRect(x: 0, y: 0, width: 300, height: 100)
        #expect(textView.layout(forWidth: 300).height == 92)
        #expect(!textView.isScrollEnabled)
        textView.styledText = Array(repeating: "line", count: 5).joined(separator: "\n")
        #expect(textView.layout(forWidth: 300).height == 140)
        #expect(!textView.isScrollEnabled)
        textView.styledText = Array(repeating: "line", count: 10).joined(separator: "\n")
        #expect(textView.layout(forWidth: 300).height == 164)
        #expect(textView.isScrollEnabled)
        coordinator.textViewDidChange(textView)
        #expect(box.value == textView.styledText)
        coordinator.textViewDidBeginEditing(textView)
        #expect(isEditing)
    }

    @Test("A leading icon and a trailing slot render inside the field")
    func slots() throws {
        let sut = TSTextField("Search", text: .constant(""), leadingIcon: .search) {
            Text("Clear")
        }
        #expect(try sut.inspect().find(TSIcon.self).actualView().name == .search)
        #expect(try sut.inspect().find(text: "Clear").string() == "Clear")
    }

    @Test("The code style keeps a plain, visible field and applies the code keyboard traits")
    func codeStyle() throws {
        let box = TextBox("AB12CD")
        let sut = TSTextField("Join code", text: box.binding, inputTraits: .code, contentStyle: .code)
        let field = try sut.inspect().find(ViewType.TextField.self)
        #expect(try field.input() == "AB12CD")
        #expect(throws: (any Error).self) { try sut.inspect().find(ViewType.SecureField.self) }
        let traits = try sut.inspect()
            .find(where: { (try? $0.modifier(TSTextInputTraitsModifier.self)) != nil })
            .modifier(TSTextInputTraitsModifier.self)
            .actualView()
            .traits
        #expect(traits == .code)
        #expect(sut.contentStyle == .code)
        #expect(TSTextField("Email", text: .constant("")).contentStyle == .default)
    }
}
