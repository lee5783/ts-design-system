import Combine
import TSDemoSupport
import UIKit

final class DemoSettingsContainerViewController: UIViewController {
    let settings: DemoSettingsModel
    private let content: UIViewController
    private var subscription: AnyCancellable?

    init(settings: DemoSettingsModel, content: UIViewController) {
        self.settings = settings
        self.content = content
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        nil
    }

    static func settings(for controller: UIViewController) -> DemoSettingsModel? {
        sequence(first: controller, next: \.parent)
            .compactMap { $0 as? DemoSettingsContainerViewController }
            .first?.settings
    }

    override var childForStatusBarStyle: UIViewController? {
        content
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(content)
        content.view.frame = view.bounds
        content.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(content.view)
        content.didMove(toParent: self)
        subscription = Publishers.CombineLatest(settings.$textSize, settings.$isRightToLeft)
            .sink { [weak self] textSize, isRightToLeft in
                self?.apply(textSize: textSize, isRightToLeft: isRightToLeft)
            }
    }

    private func apply(textSize: DemoTextSize, isRightToLeft: Bool) {
        var traits: [UITraitCollection] = []
        if isRightToLeft {
            traits.append(UITraitCollection(layoutDirection: .rightToLeft))
        }
        if let category = textSize.contentSizeCategory {
            traits.append(UITraitCollection(preferredContentSizeCategory: category))
        }
        setOverrideTraitCollection(traits.isEmpty ? nil : UITraitCollection(traitsFrom: traits), forChild: content)
        let attribute: UISemanticContentAttribute = isRightToLeft ? .forceRightToLeft : .unspecified
        content.view.semanticContentAttribute = attribute
        (content as? UINavigationController)?.navigationBar.semanticContentAttribute = attribute
        setNeedsStatusBarAppearanceUpdate()
    }
}
