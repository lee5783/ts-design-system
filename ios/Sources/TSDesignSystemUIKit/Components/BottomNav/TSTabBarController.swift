import UIKit

public final class TSTabBarController: UITabBarController {
    public let bottomNav: TSBottomNavView

    public var items: [TSBottomNavItem] {
        get { bottomNav.items }
        set { bottomNav.items = newValue }
    }

    override public var selectedIndex: Int {
        didSet { syncBottomNav() }
    }

    override public var selectedViewController: UIViewController? {
        didSet { syncBottomNav() }
    }

    public init(items: [TSBottomNavItem], viewControllers: [UIViewController], label: String? = nil) {
        bottomNav = TSBottomNavView(items: items, label: label)
        super.init(nibName: nil, bundle: nil)
        setViewControllers(viewControllers, animated: false)
    }

    @available(*, unavailable)
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        TSTabBarCompat.hideSystemTabBar(of: self)
        bottomNav.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(bottomNav)
        NSLayoutConstraint.activate([
            bottomNav.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            bottomNav.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            bottomNav.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        bottomNav.addAction(UIAction { [weak self] _ in self?.bottomNavDidChange() }, for: .valueChanged)
        syncBottomNav()
    }

    override public func setViewControllers(_ viewControllers: [UIViewController]?, animated: Bool) {
        super.setViewControllers(viewControllers, animated: animated)
        applyContentInsets()
        syncBottomNav()
    }

    override public func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        TSTabBarCompat.hideSystemTabBar(of: self)
        applyContentInsets()
    }

    override public func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        view.bringSubviewToFront(bottomNav)
    }

    func bottomNavDidChange() {
        guard let index = items.firstIndex(where: { $0.id == bottomNav.selectedID }),
              let controllers = viewControllers, controllers.indices.contains(index) else { return }
        let target = controllers[index]
        guard delegate?.tabBarController?(self, shouldSelect: target) ?? true else {
            syncBottomNav()
            return
        }
        selectedIndex = index
        delegate?.tabBarController?(self, didSelect: target)
    }

    private func syncBottomNav() {
        guard items.indices.contains(selectedIndex) else { return }
        let id = items[selectedIndex].id
        if bottomNav.selectedID != id {
            bottomNav.selectedID = id
        }
    }

    private func applyContentInsets() {
        let height = TSBottomNavStyleSpec.container.height
        viewControllers?.forEach {
            if $0.additionalSafeAreaInsets.bottom != height {
                $0.additionalSafeAreaInsets.bottom = height
            }
        }
    }
}
