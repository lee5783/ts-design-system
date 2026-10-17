import SwiftUI
import TSTokens
import UIKit

@MainActor
final class TSSnapshotHostViewController: UIViewController {
    private let content: UIViewController?
    private let contentView: UIView
    private let measure: (CGFloat) -> CGFloat

    init<Content: View>(hosting: UIHostingController<Content>) {
        self.content = hosting
        self.contentView = hosting.view
        self.measure = { width in
            hosting.sizeThatFits(in: CGSize(width: width, height: TSSnapshotDevice.maximumHeight)).height
        }
        super.init(nibName: nil, bundle: nil)
    }

    init(contentView: UIView) {
        self.content = nil
        self.contentView = contentView
        self.measure = { width in
            contentView.systemLayoutSizeFitting(
                CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
                withHorizontalFittingPriority: .required,
                verticalFittingPriority: .fittingSizeLevel
            ).height
        }
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = TSColor.backgroundPrimary.uiColor
        if let content {
            addChild(content)
        }
        contentView.backgroundColor = .clear
        contentView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(contentView)
        NSLayoutConstraint.activate([
            contentView.topAnchor.constraint(equalTo: view.topAnchor),
            contentView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            contentView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            contentView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        content?.didMove(toParent: self)
    }

    func fittingSize(width: CGFloat) -> CGSize {
        let height = measure(width)
        return CGSize(width: width, height: min(ceil(height), TSSnapshotDevice.maximumHeight))
    }

    func prepareForRendering() {
        view.layoutIfNeeded()
        Self.drawTextViews(in: contentView)
    }

    private static func drawTextViews(in view: UIView) {
        guard let textView = view as? UITextView else {
            view.subviews.forEach(drawTextViews)
            return
        }
        if #available(iOS 16, *), textView.textLayoutManager != nil {
            display(textView)
            return
        }
        textView.layoutManager.ensureLayout(for: textView.textContainer)
        display(textView)
    }

    private static func display(_ view: UIView) {
        view.setNeedsDisplay()
        view.layer.displayIfNeeded()
        view.subviews.forEach(display)
    }
}
