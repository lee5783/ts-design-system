import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct TextFieldPage: View {
    @State private var search = TextFieldSample.search
    @State private var password = ""
    @State private var bio = TextFieldSample.bio
    @State private var email = ""
    @State private var phone = ""
    @State private var amount = ""
    @State private var website = ""
    @State private var code = ""
    @State private var joinCode = ""
    @State private var playgroundText = ""
    @State private var hasError = false
    @State private var isDisabled = false
    @State private var isReadOnly = false
    @State private var hasLeadingIcon = false
    @State private var isSecure = false
    @State private var isMultiline = false

    var body: some View {
        DemoPage(page: .textField) {
            DemoComponentSection(.states) {
                TSTextField(
                    DemoStrings.string(.stateDefault),
                    text: .constant(""),
                    placeholder: DemoStrings.string(.sampleEmailPlaceholder),
                    hint: DemoStrings.string(.sampleEmailHint)
                )
                TSTextField(DemoStrings.string(.stateFilled), text: .constant(TextFieldSample.email))
                TSTextField(
                    DemoStrings.string(.stateError),
                    text: .constant(TextFieldSample.shortPassword),
                    hint: DemoStrings.string(.samplePasswordHint),
                    error: DemoStrings.string(.samplePasswordError)
                )
                TSTextField(DemoStrings.string(.stateDisabled), text: .constant(TextFieldSample.name), hint: DemoStrings.string(.sampleManaged))
                    .disabled(true)
                TSTextField(
                    DemoStrings.string(.stateReadOnly),
                    text: .constant(TextFieldSample.accountID),
                    hint: DemoStrings.string(.sampleCopyHint),
                    isReadOnly: true
                )
            }
            DemoComponentSection(.withIcons) {
                TSTextField(
                    DemoStrings.string(.sampleSearch),
                    text: $search,
                    placeholder: DemoStrings.string(.sampleSearchPlaceholder),
                    leadingIcon: .search,
                    inputTraits: .search
                ) {
                    if !search.isEmpty {
                        TSIconButton(.close, label: DemoStrings.string(.sampleClear)) {
                            search = ""
                        }
                    }
                }
            }
            DemoComponentSection(.password) {
                TSTextField(
                    DemoStrings.string(.samplePassword),
                    text: $password,
                    hint: DemoStrings.string(.samplePasswordHint),
                    isSecure: true,
                    inputTraits: .newPassword
                )
            }
            DemoComponentSection(.multiline) {
                TSTextField(
                    DemoStrings.string(.sampleBio),
                    text: $bio,
                    placeholder: DemoStrings.string(.sampleBioPlaceholder),
                    hint: DemoStrings.string(.sampleBioHint),
                    isMultiline: true
                )
            }
            DemoComponentSection(.keyboardTypes) {
                TSTextField(DemoStrings.string(.sampleEmail), text: $email, placeholder: DemoStrings.string(.sampleEmailPlaceholder), inputTraits: .email)
                TSTextField(DemoStrings.string(.samplePhone), text: $phone, inputTraits: .phone)
                TSTextField(DemoStrings.string(.sampleAmount), text: $amount, inputTraits: .decimal)
                TSTextField(DemoStrings.string(.sampleWebsite), text: $website, inputTraits: .url)
                TSTextField(DemoStrings.string(.sampleCode), text: $code, inputTraits: .oneTimeCode)
            }
            DemoComponentSection(.codeEntry) {
                TSTextField(
                    DemoStrings.string(.sampleJoinCode),
                    text: $joinCode,
                    placeholder: DemoStrings.string(.sampleJoinCodePlaceholder),
                    hint: DemoStrings.string(.sampleJoinCodeHint),
                    inputTraits: .code,
                    contentStyle: .code
                )
                .accessibilityIdentifier("demo.text-field.code.field")
            }
            DemoComponentSection(.playground) {
                Toggle(DemoStrings.string(.stateError), isOn: $hasError)
                    .accessibilityIdentifier("demo.text-field.playground.error")
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.text-field.playground.disabled")
                Toggle(DemoStrings.string(.stateReadOnly), isOn: $isReadOnly)
                    .accessibilityIdentifier("demo.text-field.playground.read-only")
                Toggle(DemoStrings.string(.leadingIcon), isOn: $hasLeadingIcon)
                    .accessibilityIdentifier("demo.text-field.playground.leading-icon")
                Toggle(DemoStrings.string(.secure), isOn: $isSecure)
                    .accessibilityIdentifier("demo.text-field.playground.secure")
                Toggle(DemoStrings.string(.multiline), isOn: $isMultiline)
                    .accessibilityIdentifier("demo.text-field.playground.multiline")
                TSTextField(
                    DemoStrings.string(.sampleEmail),
                    text: $playgroundText,
                    placeholder: DemoStrings.string(.sampleEmailPlaceholder),
                    hint: DemoStrings.string(.sampleEmailHint),
                    error: hasError ? DemoStrings.string(.sampleEmailError) : nil,
                    leadingIcon: hasLeadingIcon ? .mail : nil,
                    isSecure: isSecure,
                    isReadOnly: isReadOnly,
                    isMultiline: isMultiline,
                    inputTraits: .email
                )
                .disabled(isDisabled)
                .accessibilityIdentifier("demo.text-field.playground.field")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }
}

enum TextFieldSample {
    static let email = "ada@example.com"
    static let shortPassword = "abc"
    static let name = "Ada Lovelace"
    static let accountID = "TS-2048-7731"
    static let search = "Analytical"
    static var bio: String {
        DemoStrings.string(.pangram)
    }
}
