import Combine
import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class DemoThemeBarButtonItem: UIBarButtonItem {
    private let settings: DemoSettingsModel
    private var subscription: AnyCancellable?

    init(settings: DemoSettingsModel) {
        self.settings = settings
        super.init()
        accessibilityIdentifier = "demo.action.theme"
        primaryAction = UIAction { [weak settings] _ in
            guard let settings else { return }
            settings.theme = settings.theme.next
        }
        subscription = settings.$theme.sink { [weak self] theme in
            self?.render(theme)
        }
    }

    required init?(coder: NSCoder) {
        nil
    }

    private func render(_ theme: DemoTheme) {
        image = UIImage(
            systemName: theme.symbolName,
            withConfiguration: UIImage.SymbolConfiguration(pointSize: TSSize.iconLg * 0.8, weight: .regular)
        )
        title = nil
        accessibilityLabel = DemoStrings.themeToggleLabel(theme)
    }
}
