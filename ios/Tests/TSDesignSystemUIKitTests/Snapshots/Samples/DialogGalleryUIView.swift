import UIKit
@testable import TSDesignSystemUIKit

final class DialogGalleryUIView: UIView {
    let panelView: TSDialogPanelView

    init(variant: TSDialogVariant) {
        panelView = TSDialogPanelView(
            title: "Delete this project?",
            message: "This removes the project and its files for everyone. This cannot be undone.",
            variant: variant,
            actions: [
                TSDialogAction("Cancel"),
                TSDialogAction("Delete", variant: .danger)
            ]
        )
        super.init(frame: .zero)
        let resolved = TSDialogStyleSpec.resolve(.init(variant: variant))
        let margin = variant == .center ? resolved.outerMargin : 0
        let scrimView = UIView()
        scrimView.backgroundColor = resolved.scrim.uiColor
        scrimView.translatesAutoresizingMaskIntoConstraints = false
        panelView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(scrimView)
        addSubview(panelView)
        NSLayoutConstraint.activate([
            scrimView.topAnchor.constraint(equalTo: topAnchor),
            scrimView.bottomAnchor.constraint(equalTo: bottomAnchor),
            scrimView.leadingAnchor.constraint(equalTo: leadingAnchor),
            scrimView.trailingAnchor.constraint(equalTo: trailingAnchor),
            panelView.topAnchor.constraint(equalTo: topAnchor, constant: variant == .center ? margin : TSSpacing.space6),
            panelView.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -margin),
            panelView.leadingAnchor.constraint(equalTo: leadingAnchor, constant: margin),
            panelView.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -margin)
        ])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }
}
