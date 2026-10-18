import UIKit

public final class TSDialogViewController: UIViewController, UIAdaptivePresentationControllerDelegate, UIGestureRecognizerDelegate {
    public let variant: TSDialogVariant

    public var dialogTitle: String {
        didSet { panelView.title = dialogTitle }
    }

    public var message: String? {
        didSet { panelView.message = message }
    }

    public var bodyView: UIView? {
        didSet { panelView.bodyView = bodyView }
    }

    public var actions: [TSDialogAction] {
        didSet { panelView.actions = actions }
    }

    public var onDismiss: (() -> Void)?

    var presenter: TSDialogPresenter = .shared
    var isReduceMotionEnabled: @MainActor () -> Bool = { TSReduceMotion.isEnabled }

    let panelView: TSDialogPanelView
    private var animatesDismissal = true
    private var chainedPresentations: [() -> Void] = []
    private weak var chainedDialog: TSDialogViewController?
    private static weak var dialogRunningDismissingAction: TSDialogViewController?
    let scrollView = UIScrollView()
    let contentView = UIView()

    public init(
        title: String,
        message: String? = nil,
        variant: TSDialogVariant = .center,
        actions: [TSDialogAction] = [],
        onDismiss: (() -> Void)? = nil
    ) {
        self.variant = variant
        self.dialogTitle = title
        self.message = message
        self.actions = actions
        self.onDismiss = onDismiss
        self.panelView = TSDialogPanelView(title: title, message: message, variant: variant, actions: actions)
        super.init(nibName: nil, bundle: nil)
        configurePresentation(detentPath: TSCompat.customSheetDetents)
        panelView.onAction = { [weak self] in self?.perform($0) }
        panelView.onClose = { [weak self] in self?.userDidDismiss() }
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var resolved: TSDialogStyleSpec.Resolved {
        TSDialogStyleSpec.resolve(.init(variant: variant))
    }

    @discardableResult
    public func present(from source: UIViewController, animated: Bool = true, completion: (() -> Void)? = nil) -> Bool {
        guard presentingViewController == nil, !isBeingPresented else { return false }
        if let current = Self.dialogRunningDismissingAction, current !== self, current.chains(from: source) {
            if current.chainedDialog == nil {
                current.chainedDialog = self
            }
            current.chainedPresentations.append { [self] in
                if !present(from: source, animated: animated, completion: completion) {
                    completion?()
                }
            }
            return true
        }
        guard presenter.begin(self) else { return false }
        inheritTraits(from: source)
        animatesDismissal = animated
        if variant == .sheet {
            modalTransitionStyle = isReduceMotionEnabled() ? .crossDissolve : .coverVertical
        }
        source.present(self, animated: animated, completion: completion)
        guard presentingViewController != nil else {
            presenter.end(self)
            return false
        }
        return true
    }

    func configurePresentation(detentPath: TSCompatPath) {
        switch variant {
        case .center:
            modalPresentationStyle = .overFullScreen
            modalTransitionStyle = .crossDissolve
        case .sheet:
            modalPresentationStyle = .pageSheet
            if let sheet = sheetPresentationController {
                TSSheetCompat.configure(sheet, cornerRadius: resolved.cornerRadius, path: detentPath) { [weak self] in
                    self?.fitHeight() ?? 0
                }
            }
        }
        presentationController?.delegate = self
    }

    func fitHeight() -> CGFloat {
        let width = sheetPresentationController?.containerView?.bounds.width ?? view.bounds.width
        let size = panelView.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .required,
            verticalFittingPriority: .fittingSizeLevel
        )
        return size.height
    }

    override public func viewDidLoad() {
        super.viewDidLoad()
        view.accessibilityViewIsModal = true
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.alwaysBounceVertical = false
        contentView.translatesAutoresizingMaskIntoConstraints = false
        panelView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(scrollView)
        scrollView.addSubview(contentView)
        contentView.addSubview(panelView)
        let content = scrollView.contentLayoutGuide
        let frame = scrollView.frameLayoutGuide
        let safeArea = view.safeAreaLayoutGuide
        let isCenter = variant == .center
        scrollView.contentInsetAdjustmentBehavior = isCenter ? .never : .automatic
        NSLayoutConstraint.activate([
            scrollView.topAnchor.constraint(equalTo: isCenter ? safeArea.topAnchor : view.topAnchor),
            scrollView.bottomAnchor.constraint(equalTo: isCenter ? safeArea.bottomAnchor : view.bottomAnchor),
            scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            contentView.topAnchor.constraint(equalTo: content.topAnchor),
            contentView.bottomAnchor.constraint(equalTo: content.bottomAnchor),
            contentView.leadingAnchor.constraint(equalTo: content.leadingAnchor),
            contentView.trailingAnchor.constraint(equalTo: content.trailingAnchor),
            contentView.widthAnchor.constraint(equalTo: frame.widthAnchor)
        ])
        switch variant {
        case .center:
            layoutCenter(frame: frame)
        case .sheet:
            layoutSheet()
        }
        applyStyle()
    }

    override public var keyCommands: [UIKeyCommand]? {
        [escapeKeyCommand]
    }

    override public var canBecomeFirstResponder: Bool {
        true
    }

    var escapeKeyCommand: UIKeyCommand {
        let command = UIKeyCommand(input: UIKeyCommand.inputEscape, modifierFlags: [], action: #selector(escapePressed))
        command.wantsPriorityOverSystemBehavior = true
        return command
    }

    override public func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        guard !presenter.isActive(self) else { return }
        guard !presenter.isPresenting else {
            assertionFailure("TSDialogViewController was presented while another dialog is open; use present(from:)")
            return
        }
        presenter.begin(self)
    }

    override public func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        becomeFirstResponder()
        UIAccessibility.post(notification: .screenChanged, argument: panelView.titleLabel)
    }

    override public func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if isBeingDismissed || presentingViewController == nil {
            releasePresenter()
        }
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
        if variant == .sheet, let sheet = sheetPresentationController {
            TSSheetCompat.invalidateDetents(of: sheet)
        }
    }

    public func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        presenter.end(self)
        onDismiss?()
    }

    public func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        guard let view = touch.view else { return true }
        return !view.isDescendant(of: panelView)
    }

    func userDidDismiss() {
        dismissDialog()
        onDismiss?()
    }

    func perform(_ action: TSDialogAction) {
        guard !isBeingDismissed else { return }
        guard action.dismisses else {
            action.handler()
            return
        }
        let enclosing = Self.dialogRunningDismissingAction
        Self.dialogRunningDismissingAction = self
        action.handler()
        Self.dialogRunningDismissingAction = enclosing
        let chained = chainedPresentations
        chainedPresentations = []
        dismissDialog {
            chained.forEach { $0() }
            action.afterDismissal?()
        }
    }

    @objc func scrimTapped() {
        userDidDismiss()
    }

    @objc func escapePressed() {
        userDidDismiss()
    }

    private func dismissDialog(then completion: (() -> Void)? = nil) {
        guard presentingViewController != nil else {
            releasePresenter()
            completion?()
            return
        }
        dismiss(animated: animatesDismissal) { [weak self] in
            self?.releasePresenter()
            completion?()
        }
    }

    private func chains(from source: UIViewController) -> Bool {
        guard let presenting = presentingViewController else { return true }
        return source.view.window != nil && source.view.window === presenting.view.window
    }

    private func releasePresenter() {
        if let next = chainedDialog, presenter.handOver(from: self, to: next) {
            chainedDialog = nil
            return
        }
        presenter.end(self)
    }

    private func inheritTraits(from source: UIViewController) {
        let traits = source.traitCollection
        let explicit = source.view.window == nil ? ExplicitTraits.all : ExplicitTraits(above: source)
        overrideUserInterfaceStyle = explicit.userInterfaceStyle ? traits.userInterfaceStyle : .unspecified
        var overrides: [UITraitCollection] = []
        if explicit.preferredContentSizeCategory {
            overrides.append(UITraitCollection(preferredContentSizeCategory: traits.preferredContentSizeCategory))
        }
        if explicit.layoutDirection {
            overrides.append(UITraitCollection(layoutDirection: traits.layoutDirection))
        }
        presentationController?.overrideTraitCollection = overrides.isEmpty ? nil : UITraitCollection(traitsFrom: overrides)
        let attribute: UISemanticContentAttribute = source.view.effectiveUserInterfaceLayoutDirection == .rightToLeft ? .forceRightToLeft : .forceLeftToRight
        loadViewIfNeeded()
        applySemanticContentAttribute(attribute, to: view)
    }

    private func layoutCenter(frame: UILayoutGuide) {
        let resolved = resolved
        let margin = resolved.outerMargin
        let tap = UITapGestureRecognizer(target: self, action: #selector(scrimTapped))
        tap.delegate = self
        view.addGestureRecognizer(tap)
        let preferredWidth = panelView.widthAnchor.constraint(equalTo: contentView.widthAnchor, constant: -2 * margin)
        preferredWidth.priority = .defaultHigh
        NSLayoutConstraint.activate([
            contentView.heightAnchor.constraint(greaterThanOrEqualTo: frame.heightAnchor),
            panelView.centerXAnchor.constraint(equalTo: contentView.centerXAnchor),
            panelView.centerYAnchor.constraint(equalTo: contentView.centerYAnchor),
            panelView.topAnchor.constraint(greaterThanOrEqualTo: contentView.topAnchor, constant: margin),
            panelView.bottomAnchor.constraint(lessThanOrEqualTo: contentView.bottomAnchor, constant: -margin),
            panelView.leadingAnchor.constraint(greaterThanOrEqualTo: contentView.leadingAnchor, constant: margin),
            panelView.widthAnchor.constraint(lessThanOrEqualToConstant: resolved.maxWidth ?? .greatestFiniteMagnitude),
            preferredWidth
        ])
    }

    private func layoutSheet() {
        NSLayoutConstraint.activate([
            panelView.topAnchor.constraint(equalTo: contentView.topAnchor),
            panelView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor),
            panelView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            panelView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor)
        ])
    }

    private func applyStyle() {
        guard isViewLoaded else { return }
        let resolved = resolved
        let color = variant == .center ? resolved.scrim : resolved.fill
        view.backgroundColor = color.uiColor.resolvedColor(with: traitCollection)
    }

    private func applySemanticContentAttribute(_ attribute: UISemanticContentAttribute, to view: UIView) {
        view.semanticContentAttribute = attribute
        view.subviews.forEach { applySemanticContentAttribute(attribute, to: $0) }
    }
}

private struct ExplicitTraits {
    static let all = ExplicitTraits(userInterfaceStyle: true, preferredContentSizeCategory: true, layoutDirection: true)

    var userInterfaceStyle = false
    var preferredContentSizeCategory = false
    var layoutDirection = false

    init(userInterfaceStyle: Bool, preferredContentSizeCategory: Bool, layoutDirection: Bool) {
        self.userInterfaceStyle = userInterfaceStyle
        self.preferredContentSizeCategory = preferredContentSizeCategory
        self.layoutDirection = layoutDirection
    }

    @MainActor
    init(above source: UIViewController) {
        var controller: UIViewController? = source
        while let current = controller {
            record(current)
            controller = current.parent ?? current.presentingViewController
        }
    }

    @MainActor
    private mutating func record(_ controller: UIViewController) {
        var collections: [UITraitCollection] = []
        if let parent = controller.parent, let traits = parent.overrideTraitCollection(forChild: controller) {
            collections.append(traits)
        }
        if controller.parent == nil, controller.presentingViewController != nil, let traits = controller.presentationController?.overrideTraitCollection {
            collections.append(traits)
        }
        userInterfaceStyle = userInterfaceStyle || controller.overrideUserInterfaceStyle != .unspecified || collections.contains { $0.userInterfaceStyle != .unspecified }
        preferredContentSizeCategory = preferredContentSizeCategory || collections.contains { $0.preferredContentSizeCategory != .unspecified }
        layoutDirection = layoutDirection || collections.contains { $0.layoutDirection != .unspecified }
        let overridden = TSTraitOverridesCompat.overriddenTraits(of: controller)
        userInterfaceStyle = userInterfaceStyle || overridden.userInterfaceStyle
        preferredContentSizeCategory = preferredContentSizeCategory || overridden.preferredContentSizeCategory
        layoutDirection = layoutDirection || overridden.layoutDirection
    }
}
