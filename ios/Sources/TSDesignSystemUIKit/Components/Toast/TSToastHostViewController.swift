import Combine
import UIKit

final class TSToastHostViewController: UIViewController {
    let queue: TSToastQueue
    var onCurrentChange: ((TSToastItem?) -> Void)?
    var contentSemanticAttribute: UISemanticContentAttribute = .unspecified {
        didSet { applySemanticContentAttribute(to: view) }
    }

    var bottomInset: CGFloat = 0 {
        didSet { bottomConstraint?.constant = -bottomSpacing }
    }

    private(set) var toastView: TSToastView?
    private var shownID: TSToastItem.ID?
    private let effects: TSToastShowEffects
    private var bottomConstraint: NSLayoutConstraint?
    private var subscription: AnyCancellable?

    init(queue: TSToastQueue, effects: TSToastShowEffects) {
        self.queue = queue
        self.effects = effects
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    private var resolved: TSToastStyleSpec.Resolved {
        TSToastStyleSpec.resolve(.init())
    }

    private var bottomSpacing: CGFloat {
        resolved.outerMargin + bottomInset
    }

    override func loadView() {
        let view = UIView()
        view.backgroundColor = .clear
        self.view = view
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        subscription = queue.$current.sink { [weak self] item in
            self?.currentDidChange(item)
        }
    }

    private func currentDidChange(_ item: TSToastItem?) {
        effects.didChange(to: item)
        guard item?.id != shownID else { return }
        if let toastView {
            remove(toastView)
        }
        toastView = nil
        shownID = item?.id
        if let item {
            show(item)
        }
        onCurrentChange?(item)
    }

    private func show(_ item: TSToastItem) {
        let toast = TSToastView(item: item) { [weak queue] in
            queue?.performAction()
        }
        toast.translatesAutoresizingMaskIntoConstraints = false
        applySemanticContentAttribute(to: toast)
        view.addSubview(toast)
        let margin = resolved.outerMargin
        let fill = toast.widthAnchor.constraint(equalTo: view.widthAnchor, constant: -2 * margin)
        fill.priority = .defaultHigh
        let bottom = toast.bottomAnchor.constraint(equalTo: view.keyboardLayoutGuide.topAnchor, constant: -bottomSpacing)
        NSLayoutConstraint.activate([
            toast.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            toast.leadingAnchor.constraint(greaterThanOrEqualTo: view.safeAreaLayoutGuide.leadingAnchor, constant: margin),
            toast.topAnchor.constraint(greaterThanOrEqualTo: view.safeAreaLayoutGuide.topAnchor, constant: margin),
            fill,
            bottom
        ])
        bottomConstraint = bottom
        toastView = toast
        view.layoutIfNeeded()
        let reduceMotion = TSReduceMotion.isEnabled
        toast.alpha = 0
        if !reduceMotion {
            toast.transform = CGAffineTransform(translationX: 0, y: toast.bounds.height / 2)
        }
        TSReduceMotion.animate(reduceMotion: false) {
            toast.alpha = 1
            toast.transform = .identity
        }
    }

    private func remove(_ toast: TSToastView) {
        toast.isUserInteractionEnabled = false
        toast.messageView.isAccessibilityElement = false
        toast.accessibilityElementsHidden = true
        let reduceMotion = TSReduceMotion.isEnabled
        TSReduceMotion.animate(reduceMotion: false, animations: {
            toast.alpha = 0
            if !reduceMotion {
                toast.transform = CGAffineTransform(translationX: 0, y: toast.bounds.height / 2)
            }
        }, completion: {
            toast.removeFromSuperview()
        })
    }

    private func applySemanticContentAttribute(to view: UIView) {
        view.semanticContentAttribute = contentSemanticAttribute
        view.subviews.forEach(applySemanticContentAttribute(to:))
    }
}
