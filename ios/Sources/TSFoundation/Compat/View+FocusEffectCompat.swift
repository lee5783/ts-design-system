import SwiftUI

package extension View {
    func tsFocusEffectDisabled() -> some View {
        modifier(TSFocusEffectDisabledModifier())
    }
}

struct TSFocusEffectDisabledModifier: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 17, macOS 14, *) {
            content.focusEffectDisabled()
        } else {
            content
        }
    }
}
