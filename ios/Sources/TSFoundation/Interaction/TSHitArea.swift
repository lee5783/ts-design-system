import CoreGraphics
import TSTokens

public enum TSHitArea {
    public static let minimumSize: CGFloat = TSSize.controlMd

    public static func outset(for length: CGFloat, minimum: CGFloat = minimumSize) -> CGFloat {
        max(0, (minimum - length) / 2)
    }

    public static func expandedRect(_ rect: CGRect, minimum: CGFloat = minimumSize) -> CGRect {
        rect.insetBy(dx: -outset(for: rect.width, minimum: minimum), dy: -outset(for: rect.height, minimum: minimum))
    }
}
