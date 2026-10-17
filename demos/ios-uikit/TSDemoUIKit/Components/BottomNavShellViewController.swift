import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

@MainActor
enum BottomNavShell {
    static func make(settings: DemoSettingsModel) -> TSTabBarController {
        var inboxBadge = 3
        let items = BottomNavSample.items(count: 4, inboxBadge: inboxBadge)
        let tabs = items.map(BottomNavShellTabViewController.init(item:))
        let navigationControllers = tabs.map { tab in
            tab.navigationItem.leftBarButtonItem = UIBarButtonItem(
                title: DemoStrings.string(.sampleClose),
                primaryAction: UIAction { [weak tab] _ in tab?.dismiss(animated: true) }
            )
            tab.navigationItem.leftBarButtonItem?.accessibilityIdentifier = "demo.bottom-nav.shell.close"
            tab.navigationItem.rightBarButtonItem = DemoThemeBarButtonItem(settings: settings)
            return UINavigationController(rootViewController: tab)
        }
        let controller = TSTabBarController(
            items: items,
            viewControllers: navigationControllers,
            label: DemoStrings.string(.sampleMainNavigation)
        )
        controller.view.accessibilityIdentifier = "demo.bottom-nav.shell"
        let update: (Int) -> Void = { [weak controller] badge in
            inboxBadge = badge
            controller?.items = BottomNavSample.items(count: 4, inboxBadge: inboxBadge)
        }
        tabs.forEach { tab in
            tab.onNewMessage = { update(inboxBadge + 1) }
            tab.onMarkAllRead = { update(0) }
        }
        return controller
    }
}

final class BottomNavShellTabViewController: UIViewController {
    let item: TSBottomNavItem
    var onNewMessage: () -> Void = {}
    var onMarkAllRead: () -> Void = {}

    init(item: TSBottomNavItem) {
        self.item = item
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = item.label
        navigationItem.largeTitleDisplayMode = .never
        view.backgroundColor = TSColor.backgroundPrimary.uiColor
        let title = DemoLabel(item.label, font: .title2, color: .textPrimary)
        title.accessibilityTraits = .header
        title.accessibilityIdentifier = "demo.bottom-nav.shell.title.\(item.id)"
        let body = DemoLabel(DemoStrings.string(.sampleShellBody), font: .body, color: .textSecondary)
        let add = TSButtonView(
            title: DemoStrings.string(.sampleNewMessage),
            variant: .secondary,
            primaryAction: UIAction { [weak self] _ in self?.onNewMessage() }
        )
        add.accessibilityIdentifier = "demo.bottom-nav.shell.add-one.\(item.id)"
        let clear = TSButtonView(
            title: DemoStrings.string(.sampleMarkAllRead),
            variant: .ghost,
            primaryAction: UIAction { [weak self] _ in self?.onMarkAllRead() }
        )
        clear.accessibilityIdentifier = "demo.bottom-nav.shell.clear.\(item.id)"

        let scrollView = UIScrollView()
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        let stackView = UIStackView(arrangedSubviews: [title, body, add, clear])
        stackView.axis = .vertical
        stackView.alignment = .leading
        stackView.spacing = TSSpacing.space4
        stackView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)
        scrollView.addSubview(stackView)
        let content = scrollView.contentLayoutGuide
        let frame = scrollView.frameLayoutGuide
        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.topAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            stackView.topAnchor.constraint(equalTo: content.topAnchor, constant: TSSpacing.space4),
            stackView.bottomAnchor.constraint(equalTo: content.bottomAnchor, constant: -TSSpacing.space4),
            stackView.leadingAnchor.constraint(equalTo: content.leadingAnchor, constant: TSSpacing.space4),
            stackView.trailingAnchor.constraint(equalTo: content.trailingAnchor, constant: -TSSpacing.space4),
            stackView.widthAnchor.constraint(equalTo: frame.widthAnchor, constant: -2 * TSSpacing.space4),
            title.widthAnchor.constraint(equalTo: stackView.widthAnchor),
            body.widthAnchor.constraint(equalTo: stackView.widthAnchor)
        ])
    }
}
