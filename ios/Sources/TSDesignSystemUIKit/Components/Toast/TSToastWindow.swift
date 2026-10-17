import UIKit

public final class TSToastWindow: UIWindow {
    public let queue: TSToastQueue

    public var bottomInset: CGFloat {
        get { hostController.bottomInset }
        set { hostController.bottomInset = newValue }
    }

    let rootController: TSToastRootViewController
    let scheduler: any TSToastScheduling
    private var hideTimer: (any TSToastCancellable)?

    var hostController: TSToastHostViewController {
        rootController.hostController
    }

    public init(windowScene: UIWindowScene, queue: TSToastQueue = .shared) {
        self.queue = queue
        self.scheduler = TSToastTaskScheduler()
        rootController = TSToastRootViewController(hostController: TSToastHostViewController(queue: queue, effects: TSToastShowEffects()))
        super.init(windowScene: windowScene)
        configure()
    }

    init(frame: CGRect, queue: TSToastQueue, effects: TSToastShowEffects, scheduler: any TSToastScheduling) {
        self.queue = queue
        self.scheduler = scheduler
        rootController = TSToastRootViewController(hostController: TSToastHostViewController(queue: queue, effects: effects))
        super.init(frame: frame)
        configure()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    public func applyTraits(from traitCollection: UITraitCollection) {
        overrideUserInterfaceStyle = traitCollection.userInterfaceStyle
        rootController.applyTraits(
            UITraitCollection(traitsFrom: [
                UITraitCollection(preferredContentSizeCategory: traitCollection.preferredContentSizeCategory),
                UITraitCollection(layoutDirection: traitCollection.layoutDirection)
            ])
        )
    }

    override public func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        guard let hit = super.hitTest(point, with: event), let toastView = hostController.toastView, hit.isDescendant(of: toastView) else {
            return nil
        }
        return hit
    }

    private func configure() {
        windowLevel = TSToastWindowing.windowLevel
        backgroundColor = .clear
        rootController.sourceWindow = { [weak self] in
            guard let self else { return nil }
            return TSToastWindowing.sourceWindow(in: self.windowScene, excluding: self)
        }
        rootViewController = rootController
        isHidden = true
        hostController.onCurrentChange = { [weak self] item in
            self?.currentDidChange(item)
        }
        hostController.loadViewIfNeeded()
    }

    private func currentDidChange(_ item: TSToastItem?) {
        hideTimer?.cancel()
        hideTimer = nil
        if item != nil {
            isHidden = false
        } else {
            hideTimer = scheduler.schedule(after: TSToastWindowing.hideDelay) { [weak self] in
                guard let self, self.queue.current == nil else { return }
                self.isHidden = true
            }
        }
    }
}

final class TSToastRootViewController: UIViewController {
    let hostController: TSToastHostViewController
    var sourceWindow: () -> UIWindow? = { nil }

    init(hostController: TSToastHostViewController) {
        self.hostController = hostController
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        TSToastWindowing.statusBarController(of: sourceWindow())?.preferredStatusBarStyle ?? super.preferredStatusBarStyle
    }

    override var prefersStatusBarHidden: Bool {
        TSToastWindowing.statusBarController(of: sourceWindow())?.prefersStatusBarHidden ?? super.prefersStatusBarHidden
    }

    override func loadView() {
        let view = UIView()
        view.backgroundColor = .clear
        self.view = view
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(hostController)
        hostController.view.frame = view.bounds
        hostController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(hostController.view)
        hostController.didMove(toParent: self)
    }

    func applyTraits(_ traits: UITraitCollection) {
        loadViewIfNeeded()
        setOverrideTraitCollection(traits, forChild: hostController)
        switch traits.layoutDirection {
        case .rightToLeft:
            hostController.contentSemanticAttribute = .forceRightToLeft
        case .leftToRight:
            hostController.contentSemanticAttribute = .forceLeftToRight
        default:
            hostController.contentSemanticAttribute = .unspecified
        }
    }
}
