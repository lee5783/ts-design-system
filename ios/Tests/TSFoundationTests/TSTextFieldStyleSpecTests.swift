import Foundation
import Testing
import UIKit
@testable import TSFoundation
import TSTokens

@Suite("TSTextFieldStyleSpec")
struct TSTextFieldStyleSpecTests {
    static let expectedBorders: [TSControlState: (border: TSColor, width: CGFloat)] = [
        .normal: (.borderStrong, 1),
        .focused: (.focus, 2),
        .error: (.danger, 1),
        [.error, .focused]: (.danger, 2),
        .disabled: (.borderStrong, 1),
    ]

    @Test("The matrix covers 5 states × read-only × content style once each")
    func matrixIsComplete() {
        let configurations = TSTextFieldStyleSpec.allConfigurations
        #expect(configurations.count == 20)
        #expect(Set(configurations).count == 20)
    }

    @Test("The code content style changes only the input font", arguments: TSTextFieldStyleSpec.allConfigurations.filter { $0.contentStyle == .code })
    func codeContentStyle(configuration: TSTextFieldStyleSpec.Configuration) {
        let code = TSTextFieldStyleSpec.resolve(configuration)
        let standard = TSTextFieldStyleSpec.resolve(.init(state: configuration.state, isReadOnly: configuration.isReadOnly))
        #expect(code.inputFont == .code)
        #expect(standard.inputFont == .body)
        #expect(code.labelFont == standard.labelFont)
        #expect(code.messageFont == standard.messageFont)
        #expect(code.border == standard.border)
        #expect(code.borderWidth == standard.borderWidth)
        #expect(code.fill == standard.fill)
        #expect(code.inputColor == standard.inputColor)
        #expect(code.placeholderColor == standard.placeholderColor)
        #expect(code.messageColor == standard.messageColor)
        #expect(code.minHeight == standard.minHeight)
        #expect(code.opacity == standard.opacity)
        #expect(code.showsFocusRing == standard.showsFocusRing)
    }

    @Test("Every configuration maps its state to border, ring, fill and opacity", arguments: TSTextFieldStyleSpec.allConfigurations)
    func stateTokens(configuration: TSTextFieldStyleSpec.Configuration) throws {
        let resolved = TSTextFieldStyleSpec.resolve(configuration)
        let state = configuration.state
        let isDisabled = state.contains(.disabled)
        if configuration.isReadOnly {
            #expect(resolved.border == (state.contains(.error) ? .danger : .borderStrong))
            #expect(resolved.borderWidth == 1)
            #expect(!resolved.showsFocusRing)
            #expect(resolved.fill == .backgroundSecondary)
            #expect(resolved.shadow == nil)
            #expect(!resolved.isEditable)
        } else {
            let expected = try #require(Self.expectedBorders[state])
            #expect(resolved.border == expected.border)
            #expect(resolved.borderWidth == expected.width)
            #expect(resolved.showsFocusRing == state.contains(.focused))
            #expect(resolved.fill == (isDisabled ? .backgroundSecondary : .surfaceCard))
            #expect(resolved.shadow == TSShadowTokens.shadowXs)
            #expect(resolved.isEditable == !isDisabled)
        }
        #expect(resolved.opacity == (isDisabled ? TSOpacity.opacityDisabled : 1))
        #expect(resolved.isSelectable == !isDisabled)
        #expect(resolved.messageColor == (state.contains(.error) ? .danger : .textMuted))
        #expect(resolved.showsErrorIcon == state.contains(.error))
    }

    @Test("State precedence: disabled drops focus, error survives focus")
    func statePrecedence() {
        #expect(TSTextFieldStyleSpec.state() == .normal)
        #expect(TSTextFieldStyleSpec.state(isFocused: true) == .focused)
        #expect(TSTextFieldStyleSpec.state(isFocused: true, hasError: true) == [.error, .focused])
        #expect(TSTextFieldStyleSpec.state(isEnabled: false, isFocused: true) == .disabled)
    }

    @Test("Anatomy: 44 min height, 12/10 padding, gap 8, label gap 4, radius-md, label 14/20 500, input body 16/24, caption message, 20 pt leading icon, 16 pt error icon")
    func anatomy() {
        let resolved = TSTextFieldStyleSpec.resolve(.init())
        #expect(resolved.minHeight == 44)
        #expect(resolved.horizontalPadding == 12)
        #expect(resolved.verticalPadding == 10)
        #expect(resolved.spacing == 8)
        #expect(resolved.labelSpacing == 4)
        #expect(resolved.messageSpacing == 4)
        #expect(resolved.cornerRadius == TSRadius.radiusMd)
        #expect(resolved.labelFont == .label)
        #expect(resolved.labelColor == .textPrimary)
        #expect(resolved.inputFont == .body)
        #expect(resolved.inputColor == .textPrimary)
        #expect(resolved.placeholderColor == .textMuted)
        #expect(resolved.messageFont == .caption)
        #expect(resolved.leadingIconSize.pointSize == 20)
        #expect(resolved.leadingIconColor == .textMuted)
        #expect(resolved.messageIconSize.pointSize == 16)
        #expect(resolved.inputFont.token.lineHeight + 2 * resolved.verticalPadding == resolved.minHeight)
    }

    @Test("The password toggle uses eye, muted when hidden and accent-selected when revealed (C-14)")
    func passwordToggle() {
        let hidden = TSTextFieldStyleSpec.passwordToggle(isRevealed: false)
        let revealed = TSTextFieldStyleSpec.passwordToggle(isRevealed: true)
        #expect(hidden.icon == .eye)
        #expect(revealed.icon == .eye)
        #expect(hidden.iconColor == .textMuted)
        #expect(hidden.fill == nil)
        #expect(revealed.iconColor == .accentText)
        #expect(revealed.fill == .accentSubtle)
        #expect(hidden.size == 32)
        #expect(hidden.minimumHitSize == 44)
    }

    @Test(
        "Multiline height grows from 3 to 6 lines, then scrolls",
        arguments: [
            (CGFloat(24), CGFloat(92), false),
            (CGFloat(116), CGFloat(116), false),
            (CGFloat(164), CGFloat(164), false),
            (CGFloat(240), CGFloat(164), true),
        ]
    )
    func multilineLayout(contentHeight: CGFloat, height: CGFloat, scrolls: Bool) {
        let layout = TSTextFieldStyleSpec.multilineLayout(contentHeight: contentHeight, lineHeight: 24, verticalInset: 10)
        #expect(layout.height == height)
        #expect(layout.isScrollEnabled == scrolls)
    }

    @Test("Without a maximum the height follows the content and never scrolls")
    func unboundedLayout() {
        let layout = TSTextFieldStyleSpec.multilineLayout(contentHeight: 500, lineHeight: 24, verticalInset: 10, minimumLines: 1, maximumLines: nil)
        #expect(layout.height == 500)
        #expect(!layout.isScrollEnabled)
        #expect(TSTextFieldStyleSpec.multilineLayout(contentHeight: 0, lineHeight: 24, verticalInset: 10, minimumLines: 1, maximumLines: nil).height == 44)
    }

    @Test("Read-only secure text is masked")
    func readOnlyText() {
        #expect(TSTextFieldStyleSpec.readOnlyText("hunter2", isSecure: false) == "hunter2")
        #expect(TSTextFieldStyleSpec.readOnlyText("hunter2", isSecure: true) == "•••••••")
    }
}

@Suite("TSTextFieldAccessibility")
struct TSTextFieldAccessibilityTests {
    @Test("The value is untouched without an error and carries the error otherwise")
    func value() {
        #expect(TSTextFieldAccessibility.value(text: "ada", error: nil, isSecure: false) == nil)
        #expect(TSTextFieldAccessibility.value(text: "ada", error: "", isSecure: false) == nil)
        #expect(TSTextFieldAccessibility.value(text: "ada", error: "Too short", isSecure: false) == "ada, Error: Too short")
        #expect(TSTextFieldAccessibility.value(text: "", error: "Required", isSecure: false) == "Error: Required")
        #expect(TSTextFieldAccessibility.value(text: "secret", error: "Too short", isSecure: true) == "Error: Too short")
    }

    @Test("The hint is dropped while an error replaces it")
    func hint() {
        #expect(TSTextFieldAccessibility.hint("We never share it", error: nil) == "We never share it")
        #expect(TSTextFieldAccessibility.hint("We never share it", error: "Required") == nil)
        #expect(TSTextFieldAccessibility.hint(nil, error: nil) == nil)
    }

    @Test("An announcement is made only when a new error appears or changes")
    func announcement() {
        #expect(TSTextFieldAccessibility.announcement(from: nil, to: "Required") == "Error: Required")
        #expect(TSTextFieldAccessibility.announcement(from: "Required", to: "Too short") == "Error: Too short")
        #expect(TSTextFieldAccessibility.announcement(from: "Required", to: "Required") == nil)
        #expect(TSTextFieldAccessibility.announcement(from: "Required", to: nil) == nil)
        #expect(TSTextFieldAccessibility.announcement(from: nil, to: "") == nil)
    }

    @Test("Toggle labels and English strings")
    func strings() {
        #expect(TSTextFieldAccessibility.toggleLabel(isRevealed: false) == "Show password")
        #expect(TSTextFieldAccessibility.toggleLabel(isRevealed: true) == "Hide password")
        #expect(TSStrings.textFieldError("Required", locale: Locale(identifier: "en_US")) == "Error: Required")
    }

    @Test("Multiline picks TextField(axis:) on iOS 16+ and the UITextView bridge on iOS 15")
    func multilinePath() {
        if #available(iOS 16, *) {
            #expect(TSCompat.multilineTextField == .native)
        } else {
            #expect(TSCompat.multilineTextField == .fallback)
        }
    }
}

@MainActor
@Suite("TSTextInputTraits")
struct TSTextInputTraitsTests {
    @Test("Presets map to keyboard, content type, capitalization, autocorrection and return key")
    func presets() {
        #expect(TSTextInputTraits.email.keyboardType == .emailAddress)
        #expect(TSTextInputTraits.email.textContentType == .emailAddress)
        #expect(TSTextInputTraits.email.autocapitalization == .none)
        #expect(TSTextInputTraits.password.textContentType == .password)
        #expect(TSTextInputTraits.oneTimeCode.keyboardType == .numberPad)
        #expect(TSTextInputTraits.search.returnKey == .search)
        #expect(TSTextInputTraits.default.isAutocorrectionEnabled)
        #expect(TSTextInputTraits.code.keyboardType == .asciiCapable)
        #expect(TSTextInputTraits.code.autocapitalization == .allCharacters)
        #expect(!TSTextInputTraits.code.isAutocorrectionEnabled)
        #expect(TSTextInputTraits.code.textContentType == nil)
        #expect(TSTextInputTraits.code.returnKey == .default)
    }

    @Test("Applying traits configures UITextField and UITextView alike")
    func apply() {
        let traits = TSTextInputTraits(keyboardType: .URL, textContentType: .URL, autocapitalization: .none, isAutocorrectionEnabled: false, returnKey: .go)
        let field = UITextField()
        let textView = UITextView()
        traits.apply(to: field)
        traits.apply(to: textView)
        #expect(field.keyboardType == .URL)
        #expect(field.textContentType == .URL)
        #expect(field.autocapitalizationType == .none)
        #expect(field.autocorrectionType == .no)
        #expect(field.returnKeyType == .go)
        #expect(textView.keyboardType == .URL)
        #expect(textView.returnKeyType == .go)
        #expect(textView.autocorrectionType == .no)
    }
}
