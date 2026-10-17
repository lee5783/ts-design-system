import UIKit

@MainActor
enum TSSnapshotMeasurement {
    static func applySemanticContentAttribute(_ attribute: UISemanticContentAttribute, to view: UIView) {
        view.semanticContentAttribute = attribute
        view.subviews.forEach { applySemanticContentAttribute(attribute, to: $0) }
    }

    static func fittingSize(of host: TSSnapshotHostViewController, width: CGFloat, traits: UITraitCollection) -> CGSize {
        hosting(host, size: CGSize(width: width, height: TSSnapshotDevice.maximumHeight), traits: traits) {
            host.fittingSize(width: width)
        }
    }

    static func image(of host: TSSnapshotHostViewController, size: CGSize, traits: UITraitCollection) -> UIImage {
        hosting(host, size: size, traits: traits) {
            host.prepareForRendering()
            let format = UIGraphicsImageRendererFormat(for: traits)
            format.scale = TSSnapshotDevice.displayScale
            return UIGraphicsImageRenderer(bounds: host.view.bounds, format: format).image { context in
                host.view.layer.render(in: context.cgContext)
            }
        }
    }

    private static func hosting<Result>(
        _ host: TSSnapshotHostViewController,
        size: CGSize,
        traits: UITraitCollection,
        _ body: () -> Result
    ) -> Result {
        let parent = UIViewController()
        let window = TSSnapshotWindow(frame: CGRect(origin: .zero, size: size))
        window.rootViewController = parent
        parent.addChild(host)
        parent.view.addSubview(host.view)
        host.view.frame = CGRect(origin: .zero, size: size)
        host.didMove(toParent: parent)
        parent.setOverrideTraitCollection(traits, forChild: host)
        window.isHidden = false
        window.layoutIfNeeded()
        host.view.layoutIfNeeded()
        let result = body()
        parent.setOverrideTraitCollection(nil, forChild: host)
        host.willMove(toParent: nil)
        host.view.removeFromSuperview()
        host.removeFromParent()
        window.isHidden = true
        window.rootViewController = nil
        return result
    }
}

private final class TSSnapshotWindow: UIWindow {
    override var safeAreaInsets: UIEdgeInsets {
        .zero
    }
}
