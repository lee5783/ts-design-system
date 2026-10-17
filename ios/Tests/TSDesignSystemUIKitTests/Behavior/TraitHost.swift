import UIKit

@MainActor
final class TraitHost {
    let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
    let child = UIViewController()

    var content: UIView {
        child.view
    }

    init(sizeCategory: UIContentSizeCategory) {
        let parent = UIViewController()
        parent.addChild(child)
        parent.view.addSubview(child.view)
        child.didMove(toParent: parent)
        parent.setOverrideTraitCollection(UITraitCollection(preferredContentSizeCategory: sizeCategory), forChild: child)
        window.rootViewController = parent
        window.isHidden = false
    }
}
