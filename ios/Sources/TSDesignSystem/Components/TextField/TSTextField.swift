import SwiftUI

public struct TSTextField<Trailing: View>: View {
    let label: String
    @Binding var text: String
    let placeholder: String
    let hint: String?
    let error: String?
    let leadingIcon: TSIconName?
    let isSecure: Bool
    let isReadOnly: Bool
    let isMultiline: Bool
    let inputTraits: TSTextInputTraits
    let contentStyle: TSTextFieldContentStyle
    let trailing: Trailing
    var stateOverride: TSControlState?
    var multilinePath: TSCompatPath = TSCompat.multilineTextField
    var announce: @MainActor (String) -> Void = { TSTextFieldAccessibility.post($0) }

    @State var isRevealed = false
    @State private var isEditingMultiline = false
    @FocusState private var isFocused: Bool
    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @Environment(\.layoutDirection) private var layoutDirection

    public init(
        _ label: String,
        text: Binding<String>,
        placeholder: String = "",
        hint: String? = nil,
        error: String? = nil,
        leadingIcon: TSIconName? = nil,
        isSecure: Bool = false,
        isReadOnly: Bool = false,
        isMultiline: Bool = false,
        inputTraits: TSTextInputTraits = .default,
        @ViewBuilder trailing: () -> Trailing
    ) {
        self.init(
            label,
            text: text,
            placeholder: placeholder,
            hint: hint,
            error: error,
            leadingIcon: leadingIcon,
            isSecure: isSecure,
            isReadOnly: isReadOnly,
            isMultiline: isMultiline,
            inputTraits: inputTraits,
            contentStyle: .default,
            trailing: trailing
        )
    }

    public init(
        _ label: String,
        text: Binding<String>,
        placeholder: String = "",
        hint: String? = nil,
        error: String? = nil,
        leadingIcon: TSIconName? = nil,
        isSecure: Bool = false,
        isReadOnly: Bool = false,
        isMultiline: Bool = false,
        inputTraits: TSTextInputTraits = .default,
        contentStyle: TSTextFieldContentStyle,
        @ViewBuilder trailing: () -> Trailing
    ) {
        self.label = label
        _text = text
        self.placeholder = placeholder
        self.hint = hint
        self.error = error
        self.leadingIcon = leadingIcon
        self.isSecure = isSecure
        self.isReadOnly = isReadOnly
        self.isMultiline = isMultiline
        self.inputTraits = inputTraits
        self.contentStyle = contentStyle
        self.trailing = trailing()
    }

    public var body: some View {
        let state = stateOverride ?? TSTextFieldStyleSpec.state(
            isEnabled: isEnabled,
            isFocused: isFocused || isEditingMultiline,
            hasError: error != nil
        )
        let resolved = TSTextFieldStyleSpec.resolve(.init(state: state, isReadOnly: isReadOnly, contentStyle: contentStyle))
        VStack(alignment: .leading, spacing: resolved.labelSpacing) {
            Text(label)
                .foregroundColor(resolved.labelColor.color)
                .fixedSize(horizontal: false, vertical: true)
                .tsFont(resolved.labelFont)
                .accessibilityHidden(true)
            box(resolved)
            if let message = error ?? hint {
                messageRow(message, resolved: resolved)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .tsOnChange(of: error) { oldValue, newValue in
            if let announcement = TSTextFieldAccessibility.announcement(from: oldValue, to: newValue) {
                announce(announcement)
            }
        }
    }

    func forcingState(_ state: TSControlState) -> Self {
        var copy = self
        copy.stateOverride = state
        return copy
    }

    func usingMultilinePath(_ path: TSCompatPath) -> Self {
        var copy = self
        copy.multilinePath = path
        return copy
    }

    func revealingPassword() -> Self {
        var copy = self
        copy._isRevealed = State(initialValue: true)
        return copy
    }

    func announcing(with announce: @escaping @MainActor (String) -> Void) -> Self {
        var copy = self
        copy.announce = announce
        return copy
    }

    private var showsPasswordToggle: Bool {
        isSecure && !isReadOnly && !isMultiline
    }

    private func box(_ resolved: TSTextFieldStyleSpec.Resolved) -> some View {
        HStack(spacing: resolved.spacing) {
            if let leadingIcon {
                TSIcon(leadingIcon, size: resolved.leadingIconSize)
                    .foregroundColor(resolved.leadingIconColor.color)
            }
            input(resolved)
                .frame(maxWidth: .infinity, alignment: .leading)
            if showsPasswordToggle {
                TSPasswordToggle(isRevealed: $isRevealed) {
                    isFocused = true
                }
            }
            trailing
        }
        .padding(.horizontal, resolved.horizontalPadding)
        .frame(minHeight: resolved.minHeight)
        .tsControlChrome(
            RoundedRectangle(cornerRadius: resolved.cornerRadius, style: .continuous),
            fill: resolved.fill,
            border: resolved.border,
            borderWidth: resolved.borderWidth,
            shadow: resolved.shadow
        )
        .opacity(resolved.opacity)
    }

    @ViewBuilder
    private func input(_ resolved: TSTextFieldStyleSpec.Resolved) -> some View {
        let value = TSTextFieldAccessibility.value(text: text, error: error, isSecure: isSecure)
        let accessibilityHint = TSTextFieldAccessibility.hint(hint, error: error)
        if isReadOnly {
            readOnlyText(resolved)
                .frame(maxWidth: .infinity, alignment: .leading)
                .contentShape(Rectangle())
                .accessibilityElement(children: .combine)
                .modifier(TSTextFieldAccessibilityModifier(label: label, hint: accessibilityHint, value: value ?? TSTextFieldStyleSpec.readOnlyText(text, isSecure: isSecure)))
        } else if isMultiline {
            multilineInput(resolved)
                .modifier(TSTextFieldAccessibilityModifier(label: label, hint: accessibilityHint, value: value))
        } else {
            singleLineInput(resolved)
                .foregroundColor(resolved.inputColor.color)
                .tsFont(resolved.inputFont)
                .padding(.vertical, resolved.verticalPadding)
                .focused($isFocused)
                .tsFocusEffectDisabled()
                .tsTextInputTraits(inputTraits)
                .modifier(TSTextFieldAccessibilityModifier(label: label, hint: accessibilityHint, value: value))
        }
    }

    @ViewBuilder
    private func singleLineInput(_ resolved: TSTextFieldStyleSpec.Resolved) -> some View {
        if isSecure && !isRevealed {
            SecureField(label, text: $text, prompt: prompt(resolved))
        } else {
            TextField(label, text: $text, prompt: prompt(resolved))
        }
    }

    @ViewBuilder
    private func multilineInput(_ resolved: TSTextFieldStyleSpec.Resolved) -> some View {
        if multilinePath == .native, #available(iOS 16, *) {
            TextField(label, text: $text, prompt: prompt(resolved), axis: .vertical)
                .lineLimit(TSTextFieldStyleSpec.minimumLines ... TSTextFieldStyleSpec.maximumLines)
                .foregroundColor(resolved.inputColor.color)
                .font(resolved.inputFont.font(for: UIContentSizeCategory(dynamicTypeSize)))
                .padding(.vertical, resolved.verticalPadding)
                .focused($isFocused)
                .tsFocusEffectDisabled()
                .tsTextInputTraits(inputTraits)
        } else {
            TSMultilineTextView(
                text: $text,
                isEditing: $isEditingMultiline,
                placeholder: placeholder,
                font: resolved.inputFont,
                textColor: resolved.inputColor,
                placeholderColor: resolved.placeholderColor,
                verticalInset: resolved.verticalPadding,
                isEditable: resolved.isEditable,
                inputTraits: inputTraits,
                sizeCategory: UIContentSizeCategory(dynamicTypeSize),
                layoutDirection: layoutDirection
            )
        }
    }

    private func readOnlyText(_ resolved: TSTextFieldStyleSpec.Resolved) -> some View {
        let display = TSTextFieldStyleSpec.readOnlyText(text, isSecure: isSecure)
        return Text(display.isEmpty ? placeholder : display)
            .foregroundColor((display.isEmpty ? resolved.placeholderColor : resolved.inputColor).color)
            .fixedSize(horizontal: false, vertical: true)
            .tsFont(resolved.inputFont)
            .textSelection(.enabled)
            .padding(.vertical, resolved.verticalPadding)
    }

    private func prompt(_ resolved: TSTextFieldStyleSpec.Resolved) -> Text {
        Text(placeholder).foregroundColor(resolved.placeholderColor.color)
    }

    private func messageRow(_ message: String, resolved: TSTextFieldStyleSpec.Resolved) -> some View {
        HStack(spacing: resolved.messageSpacing) {
            if resolved.showsErrorIcon {
                TSIcon(.alert, size: resolved.messageIconSize)
            }
            Text(message)
                .fixedSize(horizontal: false, vertical: true)
                .tsFont(resolved.messageFont)
        }
        .foregroundColor(resolved.messageColor.color)
        .accessibilityHidden(true)
    }
}

public extension TSTextField where Trailing == EmptyView {
    init(
        _ label: String,
        text: Binding<String>,
        placeholder: String = "",
        hint: String? = nil,
        error: String? = nil,
        leadingIcon: TSIconName? = nil,
        isSecure: Bool = false,
        isReadOnly: Bool = false,
        isMultiline: Bool = false,
        inputTraits: TSTextInputTraits = .default
    ) {
        self.init(
            label,
            text: text,
            placeholder: placeholder,
            hint: hint,
            error: error,
            leadingIcon: leadingIcon,
            isSecure: isSecure,
            isReadOnly: isReadOnly,
            isMultiline: isMultiline,
            inputTraits: inputTraits
        ) {
            EmptyView()
        }
    }

    init(
        _ label: String,
        text: Binding<String>,
        placeholder: String = "",
        hint: String? = nil,
        error: String? = nil,
        leadingIcon: TSIconName? = nil,
        isSecure: Bool = false,
        isReadOnly: Bool = false,
        isMultiline: Bool = false,
        inputTraits: TSTextInputTraits = .default,
        contentStyle: TSTextFieldContentStyle
    ) {
        self.init(
            label,
            text: text,
            placeholder: placeholder,
            hint: hint,
            error: error,
            leadingIcon: leadingIcon,
            isSecure: isSecure,
            isReadOnly: isReadOnly,
            isMultiline: isMultiline,
            inputTraits: inputTraits,
            contentStyle: contentStyle
        ) {
            EmptyView()
        }
    }
}

struct TSTextFieldAccessibilityModifier: ViewModifier {
    let label: String
    let hint: String?
    let value: String?

    func body(content: Content) -> some View {
        let labelled = content
            .accessibilityLabel(Text(label))
            .accessibilityHint(Text(hint ?? ""))
        if let value {
            labelled.accessibilityValue(Text(value))
        } else {
            labelled
        }
    }
}
