import CoreGraphics
import TSTokens

public enum TSDialogVariant: String, CaseIterable, Hashable, Sendable {
    case center
    case sheet
}

public enum TSDialogActionsAxis: String, Hashable, Sendable {
    case horizontal
    case vertical
}

public enum TSDialogStyleSpec: TSStyleSpec {
    public struct Configuration: Hashable, Sendable {
        public let variant: TSDialogVariant
        public let isAccessibilitySize: Bool

        public init(variant: TSDialogVariant = .center, isAccessibilitySize: Bool = false) {
            self.variant = variant
            self.isAccessibilitySize = isAccessibilitySize
        }
    }

    public struct Resolved: Equatable, Sendable {
        public let scrim: TSColor
        public let fill: TSColor
        public let cornerRadius: CGFloat
        public let roundsBottomCorners: Bool
        public let shadow: TSShadowToken?
        public let maxWidth: CGFloat?
        public let outerMargin: CGFloat
        public let paddingTop: CGFloat
        public let paddingHorizontal: CGFloat
        public let paddingBottom: CGFloat
        public let showsGrabber: Bool
        public let grabberWidth: CGFloat
        public let grabberHeight: CGFloat
        public let grabberColor: TSColor
        public let grabberSpacing: CGFloat
        public let contentTopInset: CGFloat
        public let titleFont: TSFont
        public let titleColor: TSColor
        public let titleSpacing: CGFloat
        public let bodyFont: TSFont
        public let bodyColor: TSColor
        public let actionsTopSpacing: CGFloat
        public let actionsSpacing: CGFloat
        public let actionsAxis: TSDialogActionsAxis
        public let primaryActionFirst: Bool
        public let blockActions: Bool
    }

    public static let allConfigurations: [Configuration] = TSDialogVariant.allCases.flatMap { variant in
        [false, true].map { Configuration(variant: variant, isAccessibilitySize: $0) }
    }

    public static func resolve(_ configuration: Configuration) -> Resolved {
        let isSheet = configuration.variant == .sheet
        let stacksActions = isSheet || configuration.isAccessibilitySize
        let paddingTop = isSheet ? TSSpacing.space3 : TSSpacing.space6
        let grabberHeight = TSComponentTokens.sheetGrabberHeight
        let grabberSpacing = TSSpacing.space4
        return Resolved(
            scrim: .overlay,
            fill: .surfaceElevated,
            cornerRadius: TSRadius.radiusXl,
            roundsBottomCorners: !isSheet,
            shadow: isSheet ? nil : TSShadowTokens.shadowLg,
            maxWidth: isSheet ? nil : TSComponentTokens.dialogMaxWidth,
            outerMargin: isSheet ? 0 : TSSpacing.space4,
            paddingTop: paddingTop,
            paddingHorizontal: isSheet ? TSSpacing.space4 : TSSpacing.space6,
            paddingBottom: TSSpacing.space6,
            showsGrabber: isSheet,
            grabberWidth: TSComponentTokens.sheetGrabberWidth,
            grabberHeight: grabberHeight,
            grabberColor: .borderStrong,
            grabberSpacing: grabberSpacing,
            contentTopInset: isSheet ? paddingTop + grabberHeight + grabberSpacing : paddingTop,
            titleFont: .title2,
            titleColor: .textPrimary,
            titleSpacing: TSSpacing.space2,
            bodyFont: .body,
            bodyColor: .textSecondary,
            actionsTopSpacing: TSSpacing.space6,
            actionsSpacing: TSSpacing.space2,
            actionsAxis: stacksActions ? .vertical : .horizontal,
            primaryActionFirst: stacksActions,
            blockActions: stacksActions
        )
    }
}
