import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

class DemoPageViewController: UIViewController {
    let page: DemoPageID

    init(page: DemoPageID) {
        self.page = page
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = page.title
        navigationItem.largeTitleDisplayMode = .never
        view.backgroundColor = TSColor.backgroundPrimary.uiColor
        let scrollView = UIScrollView()
        scrollView.accessibilityIdentifier = page.accessibilityIdentifier
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        let stackView = UIStackView(arrangedSubviews: makeContentViews())
        stackView.axis = .vertical
        stackView.alignment = .fill
        stackView.spacing = TSSpacing.space4
        stackView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)
        scrollView.addSubview(stackView)
        let content = scrollView.contentLayoutGuide
        let frame = scrollView.frameLayoutGuide
        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: view.topAnchor),
            scrollView.bottomAnchor.constraint(equalTo: view.keyboardLayoutGuide.topAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            stackView.topAnchor.constraint(equalTo: content.topAnchor, constant: TSSpacing.space4),
            stackView.bottomAnchor.constraint(equalTo: content.bottomAnchor, constant: -TSSpacing.space4),
            stackView.leadingAnchor.constraint(equalTo: content.leadingAnchor, constant: TSSpacing.space4),
            stackView.trailingAnchor.constraint(equalTo: content.trailingAnchor, constant: -TSSpacing.space4),
            stackView.widthAnchor.constraint(equalTo: frame.widthAnchor, constant: -2 * TSSpacing.space4)
        ])
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        installThemeItem()
    }

    func makeContentViews() -> [UIView] {
        []
    }

    private func installThemeItem() {
        guard !(navigationItem.rightBarButtonItems ?? []).contains(where: { $0 is DemoThemeBarButtonItem }),
              let settings = DemoSettingsContainerViewController.settings(for: self)
        else { return }
        navigationItem.rightBarButtonItems = (navigationItem.rightBarButtonItems ?? []) + [DemoThemeBarButtonItem(settings: settings)]
    }
}
