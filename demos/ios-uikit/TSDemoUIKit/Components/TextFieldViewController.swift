import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class TextFieldViewController: DemoPageViewController {
    private lazy var playgroundField: TSTextFieldView = {
        let field = TSTextFieldView(
            label: DemoStrings.string(.sampleEmail),
            placeholder: DemoStrings.string(.sampleEmailPlaceholder),
            hint: DemoStrings.string(.sampleEmailHint),
            inputTraits: .email
        )
        field.accessibilityIdentifier = "demo.text-field.playground.field"
        field.addAction(UIAction { [unowned field] _ in field.resignFirstResponder() }, for: .primaryActionTriggered)
        return field
    }()

    init() {
        super.init(page: .textField)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [statesSection(), iconsSection(), passwordSection(), multilineSection(), keyboardTypesSection(), codeSection(), playgroundSection()]
    }

    private func statesSection() -> UIView {
        let disabled = TSTextFieldView(label: DemoStrings.string(.stateDisabled), text: TextFieldSample.name, hint: DemoStrings.string(.sampleManaged))
        disabled.isEnabled = false
        return fieldsSection(.states, fields: [
            TSTextFieldView(
                label: DemoStrings.string(.stateDefault),
                placeholder: DemoStrings.string(.sampleEmailPlaceholder),
                hint: DemoStrings.string(.sampleEmailHint)
            ),
            TSTextFieldView(label: DemoStrings.string(.stateFilled), text: TextFieldSample.email),
            TSTextFieldView(
                label: DemoStrings.string(.stateError),
                text: TextFieldSample.shortPassword,
                hint: DemoStrings.string(.samplePasswordHint),
                errorMessage: DemoStrings.string(.samplePasswordError)
            ),
            disabled,
            TSTextFieldView(
                label: DemoStrings.string(.stateReadOnly),
                text: TextFieldSample.accountID,
                hint: DemoStrings.string(.sampleCopyHint),
                isReadOnly: true
            )
        ])
    }

    private func iconsSection() -> UIView {
        let field = TSTextFieldView(
            label: DemoStrings.string(.sampleSearch),
            text: TextFieldSample.search,
            placeholder: DemoStrings.string(.sampleSearchPlaceholder),
            leadingIcon: .search,
            inputTraits: .search
        )
        let clear = TSIconButtonView(icon: .close, label: DemoStrings.string(.sampleClear), primaryAction: UIAction { [unowned field] _ in
            field.text = ""
            field.trailingView?.isHidden = true
        })
        field.trailingView = clear
        field.addAction(UIAction { [unowned field] _ in field.trailingView?.isHidden = field.text.isEmpty }, for: .editingChanged)
        return fieldsSection(.withIcons, fields: [field])
    }

    private func passwordSection() -> UIView {
        fieldsSection(.password, fields: [
            TSTextFieldView(
                label: DemoStrings.string(.samplePassword),
                hint: DemoStrings.string(.samplePasswordHint),
                isSecure: true,
                inputTraits: .newPassword
            )
        ])
    }

    private func multilineSection() -> UIView {
        fieldsSection(.multiline, fields: [
            TSTextFieldView(
                label: DemoStrings.string(.sampleBio),
                text: DemoStrings.string(.pangram),
                placeholder: DemoStrings.string(.sampleBioPlaceholder),
                hint: DemoStrings.string(.sampleBioHint),
                isMultiline: true
            )
        ])
    }

    private func keyboardTypesSection() -> UIView {
        fieldsSection(.keyboardTypes, fields: [
            TSTextFieldView(label: DemoStrings.string(.sampleEmail), placeholder: DemoStrings.string(.sampleEmailPlaceholder), inputTraits: .email),
            TSTextFieldView(label: DemoStrings.string(.samplePhone), inputTraits: .phone),
            TSTextFieldView(label: DemoStrings.string(.sampleAmount), inputTraits: .decimal),
            TSTextFieldView(label: DemoStrings.string(.sampleWebsite), inputTraits: .url),
            TSTextFieldView(label: DemoStrings.string(.sampleCode), inputTraits: .oneTimeCode)
        ])
    }

    private func codeSection() -> UIView {
        let field = TSTextFieldView(
            label: DemoStrings.string(.sampleJoinCode),
            placeholder: DemoStrings.string(.sampleJoinCodePlaceholder),
            hint: DemoStrings.string(.sampleJoinCodeHint),
            inputTraits: .code
        )
        field.contentStyle = .code
        field.accessibilityIdentifier = "demo.text-field.code.field"
        field.addAction(UIAction { [unowned field] _ in field.resignFirstResponder() }, for: .primaryActionTriggered)
        return fieldsSection(.codeEntry, fields: [field])
    }

    private func playgroundSection() -> UIView {
        let field = playgroundField
        let section = DemoComponentSectionView(.playground, views: [])
        [
            DemoPlayground.toggle(.stateError, identifier: "demo.text-field.playground.error") {
                field.errorMessage = $0 ? DemoStrings.string(.sampleEmailError) : nil
            },
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.text-field.playground.disabled") { field.isEnabled = !$0 },
            DemoPlayground.toggle(.stateReadOnly, identifier: "demo.text-field.playground.read-only") { field.isReadOnly = $0 },
            DemoPlayground.toggle(.leadingIcon, identifier: "demo.text-field.playground.leading-icon") { field.leadingIcon = $0 ? .mail : nil },
            DemoPlayground.toggle(.secure, identifier: "demo.text-field.playground.secure") { field.isSecure = $0 },
            DemoPlayground.toggle(.multiline, identifier: "demo.text-field.playground.multiline") { field.isMultiline = $0 },
            field
        ].forEach(section.addFullWidth)
        return section
    }

    private func fieldsSection(_ key: DemoStringKey, fields: [UIView]) -> UIView {
        let section = DemoComponentSectionView(key, views: [])
        fields.forEach(section.addFullWidth)
        return section
    }
}

enum TextFieldSample {
    static let email = "ada@example.com"
    static let shortPassword = "abc"
    static let name = "Ada Lovelace"
    static let accountID = "TS-2048-7731"
    static let search = "Analytical"
}
