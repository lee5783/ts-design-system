import CoreGraphics
import TSTokens

public enum TSAvatarSize: String, CaseIterable, Hashable, Sendable {
    case xs
    case sm
    case md
    case lg
}

public enum TSAvatarContent: String, CaseIterable, Hashable, Sendable {
    case initials
    case image
    case placeholder

    public static func make(initials: String, hasImage: Bool) -> TSAvatarContent {
        if hasImage {
            return .image
        }
        return initials.isEmpty ? .placeholder : .initials
    }
}

public enum TSAvatarStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let size: TSAvatarSize
        public let content: TSAvatarContent

        public init(size: TSAvatarSize = .md, content: TSAvatarContent = .initials) {
            self.size = size
            self.content = content
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let diameter: CGFloat
        public let cornerRadius: CGFloat
        public let fill: TSColor
        public let foreground: TSColor
        public let initialsFontSize: CGFloat
        public let initialsFontWeight: Int
        public let initialsMinimumScaleFactor: CGFloat
        public let contentInset: CGFloat
        public let showsInitials: Bool
        public let showsImage: Bool
        public let showsPlaceholder: Bool
        public let placeholderIconSize: TSIconSize
    }

    public static let allConfigurations: [Configuration] = TSAvatarSize.allCases.flatMap { size in
        TSAvatarContent.allCases.map { Configuration(size: size, content: $0) }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let size = configuration.size
        let content = configuration.content
        let diameter = diameter(size)
        return Resolved(
            diameter: diameter,
            cornerRadius: diameter / 2,
            fill: .accentSubtle,
            foreground: .accentText,
            initialsFontSize: initialsFontSize(size),
            initialsFontWeight: 600,
            initialsMinimumScaleFactor: 0.5,
            contentInset: TSSpacing.space0_5,
            showsInitials: content == .initials,
            showsImage: content == .image,
            showsPlaceholder: content == .placeholder,
            placeholderIconSize: placeholderIconSize(size)
        )
    }

    static func diameter(_ size: TSAvatarSize) -> CGFloat {
        switch size {
        case .xs: 24
        case .sm: 32
        case .md: 40
        case .lg: 56
        }
    }

    static func initialsFontSize(_ size: TSAvatarSize) -> CGFloat {
        switch size {
        case .xs: TSComponentTokens.avatarXsFontSize
        case .sm: TSComponentTokens.avatarSmFontSize
        case .md: TSComponentTokens.avatarMdFontSize
        case .lg: TSComponentTokens.avatarLgFontSize
        }
    }

    static func placeholderIconSize(_ size: TSAvatarSize) -> TSIconSize {
        switch size {
        case .xs, .sm: .sm
        case .md: .md
        case .lg: .lg
        }
    }
}
