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
