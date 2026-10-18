import UIKit

public final class TSTextFieldView: UIControl {
    public var label: String {
        didSet { applyStyle() }
    }

    public var text: String {
        didSet {
            if !isSyncingText {
                pushText()
            }
            applyStyle()
        }
    }

    public var placeholder: String {
        didSet { applyStyle() }
    }

    public var hint: String? {
        didSet { applyStyle() }
    }

    public var errorMessage: String? {
        didSet { applyStyle() }
    }

    public var leadingIcon: TSIconName? {
        didSet { applyStyle() }
    }

    public var trailingView: UIView? {
        didSet {
            oldValue?.removeFromSuperview()
            if let trailingView {
                contentStack.addArrangedSubview(trailingView)
            }
        }
    }

    public var isSecure: Bool {
        didSet { applyStyle() }
    }

    public var isReadOnly: Bool {
        didSet { applyStyle() }
    }

    public var isMultiline: Bool {
        didSet { applyStyle() }
    }

    public var inputTraits: TSTextInputTraits {
        didSet { applyStyle() }
    }

    public var contentStyle: TSTextFieldContentStyle = .default {
        didSet { applyStyle() }
    }

    override public var isEnabled: Bool {
        didSet { applyStyle() }
    }

    override public var accessibilityIdentifier: String? {
        didSet {
            textField.accessibilityIdentifier = accessibilityIdentifier
            textView.accessibilityIdentifier = accessibilityIdentifier
        }
    }

    let stackView = UIStackView()
    let labelView = UILabel()
    let boxView = UIView()
    let contentStack = UIStackView()
    let leadingIconView = TSIconView(.search, size: .md)
    let textField = UITextField()
    let textView = TSGrowingTextView()
    let passwordToggle = TSPasswordToggleButton()
    let messageStack = UIStackView()
    let messageIconView = TSIconView(.alert, size: .sm)
    let messageLabel = UILabel()

    var isRevealed = false {
        didSet { applyStyle() }
    }

    var forcedFocus = false {
        didSet { applyStyle() }
    }

    private var isSyncingText = false
    private lazy var textFieldHeight = textField.heightAnchor.constraint(greaterThanOrEqualToConstant: TSSize.controlMd)

    public init(
        label: String,
        text: String = "",
        placeholder: String = "",
        hint: String? = nil,
        errorMessage: String? = nil,
        leadingIcon: TSIconName? = nil,
        isSecure: Bool = false,
        isReadOnly: Bool = false,
        isMultiline: Bool = false,
        inputTraits: TSTextInputTraits = .default
    ) {
        self.label = label
        self.text = text
        self.placeholder = placeholder
        self.hint = hint
        self.errorMessage = errorMessage
        self.leadingIcon = leadingIcon
        self.isSecure = isSecure
        self.isReadOnly = isReadOnly
        self.isMultiline = isMultiline
        self.inputTraits = inputTraits
        super.init(frame: .zero)
        configureHierarchy()
        pushText()
        applyStyle()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var usesTextView: Bool {
        isMultiline || isReadOnly
    }

    var isEditingText: Bool {
        forcedFocus || textField.isFirstResponder || textView.isFirstResponder
    }

    var controlState: TSControlState {
        TSTextFieldStyleSpec.state(isEnabled: isEnabled, isFocused: isEditingText, hasError: errorMessage != nil)
    }

    var resolved: TSTextFieldStyleSpec.Resolved {
        TSTextFieldStyleSpec.resolve(.init(state: controlState, isReadOnly: isReadOnly, contentStyle: contentStyle))
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    @discardableResult
    override public func becomeFirstResponder() -> Bool {
        usesTextView ? textView.becomeFirstResponder() : textField.becomeFirstResponder()
    }

    @discardableResult
    override public func resignFirstResponder() -> Bool {
        let input: UIView = textView.isFirstResponder ? textView : textField
        return input.resignFirstResponder()
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        applyShadow(resolved)
    }

    private func applyShadow(_ resolved: TSTextFieldStyleSpec.Resolved) {
        if let shadow = resolved.shadow {
            TSShadow.apply(shadow, style: traitCollection.userInterfaceStyle, to: [boxView.layer], cornerRadius: resolved.cornerRadius)
        } else {
            TSShadow.apply(nil, to: boxView.layer, cornerRadius: resolved.cornerRadius)
        }
    }

    private func configureHierarchy() {
        let resolved = resolved
        stackView.axis = .vertical
        stackView.alignment = .fill
        stackView.spacing = resolved.labelSpacing
        stackView.translatesAutoresizingMaskIntoConstraints = false
        [labelView, boxView, messageStack].forEach(stackView.addArrangedSubview)
        addSubview(stackView)

        labelView.numberOfLines = 0

        contentStack.axis = .horizontal
        contentStack.alignment = .center
        contentStack.spacing = resolved.spacing
        contentStack.translatesAutoresizingMaskIntoConstraints = false
        [leadingIconView, textField, textView, passwordToggle].forEach(contentStack.addArrangedSubview)
        boxView.addSubview(contentStack)
        boxView.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(focusInput)))

        textField.borderStyle = .none
        textField.adjustsFontForContentSizeCategory = false
        textField.delegate = self
        textField.setContentHuggingPriority(.defaultLow, for: .horizontal)
        textField.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        textField.addAction(UIAction { [weak self] _ in self?.inputDidChange() }, for: .editingChanged)
        textView.delegate = self
        textView.verticalInset = resolved.verticalPadding
        textView.onTextChange = { [weak self] _ in self?.inputDidChange() }
        passwordToggle.addAction(UIAction { [weak self] _ in self?.togglePassword() }, for: .primaryActionTriggered)

        messageStack.axis = .horizontal
        messageStack.alignment = .center
        messageStack.spacing = resolved.messageSpacing
        [messageIconView, messageLabel].forEach(messageStack.addArrangedSubview)
        messageLabel.numberOfLines = 0
        messageIconView.isAccessibilityElement = false

        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: topAnchor),
            stackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: trailingAnchor),
            stackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            contentStack.leadingAnchor.constraint(equalTo: boxView.leadingAnchor, constant: resolved.horizontalPadding),
            contentStack.trailingAnchor.constraint(equalTo: boxView.trailingAnchor, constant: -resolved.horizontalPadding),
            contentStack.topAnchor.constraint(greaterThanOrEqualTo: boxView.topAnchor),
            contentStack.bottomAnchor.constraint(lessThanOrEqualTo: boxView.bottomAnchor),
            contentStack.centerYAnchor.constraint(equalTo: boxView.centerYAnchor),
            boxView.heightAnchor.constraint(greaterThanOrEqualToConstant: resolved.minHeight)
        ])
        let hugsContent = boxView.heightAnchor.constraint(equalTo: contentStack.heightAnchor)
        hugsContent.priority = .defaultLow
        hugsContent.isActive = true
    }

    private func applyStyle() {
        let resolved = resolved
        let traits = traitCollection
        let sizeCategory = traits.preferredContentSizeCategory
        let inputColor = resolved.inputColor.uiColor.resolvedColor(with: traits)
        let placeholderColor = resolved.placeholderColor.uiColor.resolvedColor(with: traits)
        let messageColor = resolved.messageColor.uiColor.resolvedColor(with: traits)

        let alignment: NSTextAlignment = effectiveUserInterfaceLayoutDirection == .rightToLeft ? .right : .left
        labelView.attributedText = resolved.labelFont.attributedString(
            label,
            sizeCategory: sizeCategory,
            color: resolved.labelColor.uiColor.resolvedColor(with: traits),
            alignment: alignment
        )

        boxView.backgroundColor = resolved.fill.uiColor.resolvedColor(with: traits)
        boxView.tsApplyBorder(resolved.border, width: resolved.borderWidth, cornerRadius: resolved.cornerRadius)
        boxView.alpha = resolved.opacity
        applyShadow(resolved)

        leadingIconView.isHidden = leadingIcon == nil
        if let leadingIcon {
            leadingIconView.name = leadingIcon
        }
        leadingIconView.size = resolved.leadingIconSize
        leadingIconView.tintColor = resolved.leadingIconColor.uiColor.resolvedColor(with: traits)

        let usesTextView = usesTextView
        textField.isHidden = usesTextView
        textView.isHidden = !usesTextView
        textFieldHeight.isActive = !usesTextView
        textFieldHeight.constant = resolved.inputFont.lineHeight(for: sizeCategory) + 2 * resolved.verticalPadding

        let inputFont = resolved.inputFont.uiFont(for: sizeCategory)
        textField.font = inputFont
        textField.textColor = inputColor
        textField.tintColor = TSColor.accentPrimary.uiColor.resolvedColor(with: traits)
        textField.attributedPlaceholder = NSAttributedString(string: placeholder, attributes: [.font: inputFont, .foregroundColor: placeholderColor])
        textField.isSecureTextEntry = isSecure && !isRevealed
        textField.isEnabled = isEnabled
        inputTraits.apply(to: textField)

        textView.minimumLines = isMultiline ? TSTextFieldStyleSpec.minimumLines : 1
        textView.maximumLines = isMultiline ? TSTextFieldStyleSpec.maximumLines : nil
        textView.verticalInset = resolved.verticalPadding
        textView.tintColor = textField.tintColor
        textView.apply(TSGrowingTextView.Typography(
            font: resolved.inputFont,
            color: inputColor,
            placeholder: placeholder,
            placeholderColor: placeholderColor,
            sizeCategory: sizeCategory,
            alignment: alignment
        ))
        textView.isEditable = resolved.isEditable
        textView.isSelectable = resolved.isSelectable
        inputTraits.apply(to: textView)
        textView.styledText = isReadOnly ? TSTextFieldStyleSpec.readOnlyText(text, isSecure: isSecure) : text

        passwordToggle.isHidden = !(isSecure && !isReadOnly && !isMultiline)
        passwordToggle.isRevealed = isRevealed
        passwordToggle.isEnabled = isEnabled

        let message = errorMessage ?? hint
        messageStack.isHidden = message == nil
        messageIconView.isHidden = !resolved.showsErrorIcon
        messageIconView.tintColor = messageColor
        messageLabel.attributedText = resolved.messageFont.attributedString(message ?? "", sizeCategory: sizeCategory, color: messageColor, alignment: alignment)

        let input: UIView = usesTextView ? textView : textField
        input.isAccessibilityElement = true
    }

    private func pushText() {
        if textField.text != text {
            textField.text = text
        }
    }

    private func inputDidChange() {
        isSyncingText = true
        text = usesTextView ? textView.styledText : (textField.text ?? "")
        isSyncingText = false
        sendActions(for: .editingChanged)
    }

    private func togglePassword() {
        isRevealed.toggle()
    }

    @objc
    private func focusInput() {
        guard resolved.isEditable else { return }
        becomeFirstResponder()
    }
}

extension TSTextFieldView: UITextFieldDelegate {
    public func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {
        resolved.isEditable
    }

    public func textFieldDidBeginEditing(_ textField: UITextField) {
        applyStyle()
        sendActions(for: .editingDidBegin)
    }

    public func textFieldDidEndEditing(_ textField: UITextField) {
        applyStyle()
        sendActions(for: .editingDidEnd)
    }

    public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        sendActions(for: .primaryActionTriggered)
        return true
    }
}

extension TSTextFieldView: UITextViewDelegate {
    public func textViewShouldBeginEditing(_ textView: UITextView) -> Bool {
        resolved.isEditable
    }

    public func textViewDidBeginEditing(_ textView: UITextView) {
        applyStyle()
        sendActions(for: .editingDidBegin)
    }

    public func textViewDidEndEditing(_ textView: UITextView) {
        applyStyle()
        sendActions(for: .editingDidEnd)
    }
}
