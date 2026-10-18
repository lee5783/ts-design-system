import TSTokens
import UIKit

public extension TSIconName {
    func image(compatibleWith traitCollection: UITraitCollection? = nil) -> UIImage? {
        UIImage(named: assetName, in: TSFoundationResources.bundle, compatibleWith: traitCollection)
    }

    func templateImage(pointSize: CGFloat) -> UIImage? {
        guard let source = image() else { return nil }
        let size = CGSize(width: pointSize, height: pointSize)
        let format = UIGraphicsImageRendererFormat.preferred()
        let resized = UIGraphicsImageRenderer(size: size, format: format).image { _ in
            source.draw(in: CGRect(origin: .zero, size: size))
        }
        let template = resized.withRenderingMode(.alwaysTemplate)
        return flipsForRightToLeftLayoutDirection ? template.imageFlippedForRightToLeftLayoutDirection() : template
    }
}

package extension UIImage {
    func tsTemplateIcon(pointSize: CGFloat) -> UIImage {
        let side = CGSize(width: pointSize, height: pointSize)
        let scale = min(pointSize / max(size.width, 1), pointSize / max(size.height, 1))
        let fitted = CGSize(width: size.width * scale, height: size.height * scale)
        let origin = CGPoint(x: (pointSize - fitted.width) / 2, y: (pointSize - fitted.height) / 2)
        let rendered = UIGraphicsImageRenderer(size: side, format: UIGraphicsImageRendererFormat.preferred()).image { _ in
            draw(in: CGRect(origin: origin, size: fitted))
        }
        let template = rendered.withRenderingMode(.alwaysTemplate)
        return flipsForRightToLeftLayoutDirection ? template.imageFlippedForRightToLeftLayoutDirection() : template
    }
}
