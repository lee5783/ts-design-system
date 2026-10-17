import SwiftUI
import TSDemoSupport

struct DemoWindowAppearance: UIViewRepresentable {
    let theme: DemoTheme

    func makeUIView(context: Context) -> DemoWindowAppearanceView {
        let view = DemoWindowAppearanceView()
        view.style = theme.userInterfaceStyle
        return view
    }

    func updateUIView(_ view: DemoWindowAppearanceView, context: Context) {
        view.style = theme.userInterfaceStyle
    }
}

final class DemoWindowAppearanceView: UIView {
    var style: UIUserInterfaceStyle = .unspecified {
        didSet { apply() }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        isUserInteractionEnabled = false
        isAccessibilityElement = false
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    override func didMoveToWindow() {
        super.didMoveToWindow()
        apply()
    }

    private func apply() {
        guard let window else { return }
        let windows = window.windowScene?.windows ?? [window]
        windows.forEach { $0.overrideUserInterfaceStyle = style }
    }
}

extension DemoTheme {
    var userInterfaceStyle: UIUserInterfaceStyle {
        switch self {
        case .system:
            .unspecified
        case .light:
            .light
        case .dark:
            .dark
        }
    }
}
