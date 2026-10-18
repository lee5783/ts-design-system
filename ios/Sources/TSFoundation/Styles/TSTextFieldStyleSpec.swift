import CoreGraphics
import TSTokens

public enum TSTextFieldContentStyle: String, CaseIterable, Hashable, Sendable {
    case `default`
    case code
}

public enum TSTextFieldStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let state: TSControlState
        public let isReadOnly: Bool
        public let contentStyle: TSTextFieldContentStyle

        public init(state: TSControlState = .normal, isReadOnly: Bool = false) {
            self.init(state: state, isReadOnly: isReadOnly, contentStyle: .default)
        }

        public init(state: TSControlState = .normal, isReadOnly: Bool = false, contentStyle: TSTextFieldContentStyle) {
            self.state = state
            self.isReadOnly = isReadOnly
            self.contentStyle = contentStyle
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let fill: TSColor
        public let border: TSColor
        public let borderWidth: CGFloat
        public let shadow: TSShadowToken?
        public let labelFont: TSFont
        public let labelColor: TSColor
        public let inputFont: TSFont
        public let inputColor: TSColor
        public let placeholderColor: TSColor
        public let messageFont: TSFont
        public let messageColor: TSColor
        public let showsErrorIcon: Bool
        public let messageIconSize: TSIconSize
        public let leadingIconSize: TSIconSize
        public let leadingIconColor: TSColor
        public let minHeight: CGFloat
        public let horizontalPadding: CGFloat
        public let verticalPadding: CGFloat
        public let spacing: CGFloat
        public let labelSpacing: CGFloat
        public let messageSpacing: CGFloat
        public let cornerRadius: CGFloat
        public let opacity: Double
        public let showsFocusRing: Bool
        public let isEditable: Bool
        public let isSelectable: Bool
    }

    public struct PasswordToggle: Equatable, Sendable {
        public let icon: TSIconName
        public let iconColor: TSColor
        public let fill: TSColor?
        public let size: CGFloat
        public let iconSize: TSIconSize
        public let cornerRadius: CGFloat
        public let minimumHitSize: CGFloat
    }

    public struct MultilineLayout: Equatable, Sendable {
        public let height: CGFloat
        public let isScrollEnabled: Bool
    }

    public static let states: [TSControlState] = [.normal, .focused, .error, [.error, .focused], .disabled]

    public static let allConfigurations: [Configuration] = TSTextFieldContentStyle.allCases.flatMap { contentStyle in
        states.flatMap { state in
            [false, true].map { Configuration(state: state, isReadOnly: $0, contentStyle: contentStyle) }
        }
    }

    public static let minimumLines = 3
    public static let maximumLines = 6
    public static let unfocusedBorderWidth: CGFloat = 1
    public static let focusedBorderWidth: CGFloat = 2

    public static func state(isEnabled: Bool = true, isFocused: Bool = false, hasError: Bool = false) -> TSControlState {
        var state = TSControlState.make(isEnabled: isEnabled, isFocused: isFocused)
        if hasError {
            state.insert(.error)
        }
        return state
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let state = configuration.state
        let isDisabled = state.contains(.disabled)
        let hasError = state.contains(.error)
        let isReadOnly = configuration.isReadOnly
        let showsFocusRing = state.contains(.focused) && !isDisabled && !isReadOnly
        return Resolved(
            fill: isDisabled || isReadOnly ? .backgroundSecondary : .surfaceCard,
            border: border(hasError: hasError, showsFocusRing: showsFocusRing),
            borderWidth: showsFocusRing ? focusedBorderWidth : unfocusedBorderWidth,
            shadow: isReadOnly ? nil : TSShadowTokens.shadowXs,
            labelFont: TSFont.label,
            labelColor: .textPrimary,
            inputFont: inputFont(configuration.contentStyle),
            inputColor: .textPrimary,
            placeholderColor: .textMuted,
            messageFont: TSFont.caption,
            messageColor: hasError ? .danger : .textMuted,
            showsErrorIcon: hasError,
            messageIconSize: .sm,
            leadingIconSize: .md,
            leadingIconColor: .textMuted,
            minHeight: TSSize.controlMd,
            horizontalPadding: TSSpacing.space3,
            verticalPadding: TSComponentTokens.textFieldPaddingVertical,
            spacing: TSSpacing.space2,
            labelSpacing: TSSpacing.space1,
            messageSpacing: TSSpacing.space1,
            cornerRadius: TSRadius.radiusMd,
            opacity: isDisabled ? TSOpacity.opacityDisabled : 1,
            showsFocusRing: showsFocusRing,
            isEditable: !isDisabled && !isReadOnly,
            isSelectable: !isDisabled
        )
    }

    public static func passwordToggle(isRevealed: Bool) -> PasswordToggle {
        PasswordToggle(
            icon: .eye,
            iconColor: isRevealed ? .accentText : .textMuted,
            fill: isRevealed ? .accentSubtle : nil,
            size: TSSize.controlSm,
            iconSize: .md,
            cornerRadius: TSRadius.radiusSm,
            minimumHitSize: TSHitArea.minimumSize
        )
    }

    public static func multilineLayout(
        contentHeight: CGFloat,
        lineHeight: CGFloat,
        verticalInset: CGFloat,
        minimumLines: Int = minimumLines,
        maximumLines: Int? = maximumLines
    ) -> MultilineLayout {
        let minimum = CGFloat(minimumLines) * lineHeight + 2 * verticalInset
        let fitted = max(minimum, contentHeight.rounded(.up))
        guard let maximumLines else {
            return MultilineLayout(height: fitted, isScrollEnabled: false)
        }
        let maximum = CGFloat(maximumLines) * lineHeight + 2 * verticalInset
        return MultilineLayout(height: min(fitted, maximum), isScrollEnabled: fitted > maximum + 0.5)
    }

    public static func readOnlyText(_ text: String, isSecure: Bool) -> String {
        isSecure ? String(repeating: "\u{2022}", count: text.count) : text
    }

    static func inputFont(_ contentStyle: TSTextFieldContentStyle) -> TSFont {
        switch contentStyle {
        case .default: TSFont.body
        case .code: TSFont.code
        }
    }

    static func border(hasError: Bool, showsFocusRing: Bool) -> TSColor {
        if hasError {
            return .danger
        }
        return showsFocusRing ? .focus : .borderStrong
    }
}
