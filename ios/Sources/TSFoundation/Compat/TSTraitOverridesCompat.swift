#if canImport(UIKit)
import UIKit
#endif

package struct TSOverriddenTraits: Hashable, Sendable {
    package var userInterfaceStyle = false
    package var preferredContentSizeCategory = false
    package var layoutDirection = false

    package init(userInterfaceStyle: Bool = false, preferredContentSizeCategory: Bool = false, layoutDirection: Bool = false) {
        self.userInterfaceStyle = userInterfaceStyle
        self.preferredContentSizeCategory = preferredContentSizeCategory
        self.layoutDirection = layoutDirection
    }
}

#if canImport(UIKit)
package enum TSTraitOverridesCompat {
    @MainActor
    package static func overriddenTraits(of controller: UIViewController, path: TSCompatPath = TSCompat.traitOverrides) -> TSOverriddenTraits {
        guard path == .native, #available(iOS 17, *) else { return TSOverriddenTraits() }
        let overrides = controller.traitOverrides
        return TSOverriddenTraits(
            userInterfaceStyle: overrides.contains(UITraitUserInterfaceStyle.self),
            preferredContentSizeCategory: overrides.contains(UITraitPreferredContentSizeCategory.self),
            layoutDirection: overrides.contains(UITraitLayoutDirection.self)
        )
    }
}
#endif
