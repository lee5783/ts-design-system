import UIKit

extension UIBackgroundConfiguration {
    static func tsControl(fill: TSColor?, cornerRadius: CGFloat) -> UIBackgroundConfiguration {
        var background = UIBackgroundConfiguration.clear()
        background.backgroundColor = fill?.uiColor ?? .clear
        background.cornerRadius = cornerRadius
        return background
    }
}

extension UIView {
    func tsApplyBorder(_ border: TSColor?, width: CGFloat, cornerRadius: CGFloat) {
        layer.cornerRadius = cornerRadius
        layer.cornerCurve = .continuous
        layer.borderWidth = border == nil ? 0 : width
        layer.borderColor = border?.uiColor.resolvedColor(with: traitCollection).cgColor
    }
}
