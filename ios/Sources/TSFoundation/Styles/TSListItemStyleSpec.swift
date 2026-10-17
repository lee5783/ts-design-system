import CoreGraphics
import TSTokens

public enum TSListItemLayout: String, CaseIterable, Hashable, Sendable {
    case horizontal
    case stacked

    public static func make(isAccessibilitySize: Bool) -> TSListItemLayout {
        isAccessibilitySize ? .stacked : .horizontal
    }
}

public enum TSListItemStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let hasSubtitle: Bool
        public let isInteractive: Bool
        public let layout: TSListItemLayout
        public let state: TSControlState

        public init(
            hasSubtitle: Bool = false,
            isInteractive: Bool = false,
            layout: TSListItemLayout = .horizontal,
            state: TSControlState = .normal
        ) {
            self.hasSubtitle = hasSubtitle
            self.isInteractive = isInteractive
            self.layout = layout
            self.state = state
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let background: TSColor?
        public let titleFont: TSFont
        public let titleColor: TSColor
        public let titleLineLimit: Int?
        public let showsSubtitle: Bool
        public let subtitleFont: TSFont
        public let subtitleColor: TSColor
        public let subtitleLineLimit: Int?
        public let leadingColor: TSColor
        public let trailingFont: TSFont
        public let trailingColor: TSColor
        public let trailingSpacing: CGFloat
        public let chevron: TSIconName
        public let chevronSize: TSIconSize
        public let chevronColor: TSColor
        public let horizontalPadding: CGFloat
        public let verticalPadding: CGFloat
        public let spacing: CGFloat
        public let isStacked: Bool
        public let stackedSpacing: CGFloat
        public let minHeight: CGFloat
        public let opacity: Double
    }

    public struct Tile: Equatable, Sendable {
        public let size: CGFloat
        public let cornerRadius: CGFloat
        public let fill: TSColor
        public let foreground: TSColor
        public let iconSize: TSIconSize
    }

    public struct ListGroup: Equatable, Sendable {
        public let fill: TSColor
        public let border: TSColor
        public let borderWidth: CGFloat
        public let cornerRadius: CGFloat
        public let divider: TSColor
        public let dividerThickness: CGFloat
        public let dividerInset: CGFloat
        public let headerFont: TSFont
        public let headerColor: TSColor
        public let headerSpacing: CGFloat
        public let headerHorizontalPadding: CGFloat
        public let sectionSpacing: CGFloat
    }

    public static let states: [TSControlState] = [.normal, .pressed, .disabled, .focused]

    public static let allConfigurations: [Configuration] = [false, true].flatMap { hasSubtitle in
        [false, true].flatMap { isInteractive in
            TSListItemLayout.allCases.flatMap { layout in
                states.map { Configuration(hasSubtitle: hasSubtitle, isInteractive: isInteractive, layout: layout, state: $0) }
            }
        }
    }

    public static let tile = Tile(
        size: TSComponentTokens.listItemTileSize,
        cornerRadius: TSRadius.radiusMd,
        fill: .accentSubtle,
        foreground: .accentText,
        iconSize: .md
    )

    public static let group = ListGroup(
        fill: .surfaceCard,
        border: .borderSubtle,
        borderWidth: 1,
        cornerRadius: TSRadius.radiusLg,
        divider: .borderSubtle,
        dividerThickness: 1,
        dividerInset: 0,
        headerFont: .overline,
        headerColor: .textMuted,
        headerSpacing: TSSpacing.space2,
        headerHorizontalPadding: TSSpacing.space4,
        sectionSpacing: TSSpacing.space6
    )

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let state = configuration.state
        let isStacked = configuration.layout == .stacked
        return Resolved(
            background: background(isInteractive: configuration.isInteractive, state: state),
            titleFont: .body,
            titleColor: .textPrimary,
            titleLineLimit: isStacked ? nil : 1,
            showsSubtitle: configuration.hasSubtitle,
            subtitleFont: .bodySm,
            subtitleColor: .textMuted,
            subtitleLineLimit: isStacked ? nil : 1,
            leadingColor: .textSecondary,
            trailingFont: .bodySm,
            trailingColor: .textMuted,
            trailingSpacing: TSSpacing.space2,
            chevron: .chevronRight,
            chevronSize: .md,
            chevronColor: .textMuted,
            horizontalPadding: TSSpacing.space4,
            verticalPadding: TSSpacing.space3,
            spacing: TSSpacing.space3,
            isStacked: isStacked,
            stackedSpacing: TSSpacing.space2,
            minHeight: TSSize.controlMd,
            opacity: state.contains(.disabled) ? TSOpacity.opacityDisabled : 1
        )
    }

    static func background(isInteractive: Bool, state: TSControlState) -> TSColor? {
        guard isInteractive, state.isInteractive else { return nil }
        if state.contains(.pressed) {
            return .surfacePressed
        }
        if state.contains(.focused) {
            return .surfaceHover
        }
        return nil
    }
}
