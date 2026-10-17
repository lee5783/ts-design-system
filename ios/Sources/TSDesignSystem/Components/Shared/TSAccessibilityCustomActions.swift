import SwiftUI

struct TSAccessibilityCustomActions: ViewModifier {
    let actions: [TSCardAction]
    let isEnabled: Bool

    func body(content: Content) -> some View {
        let available = isEnabled ? actions : []
        return available.reduce(AnyView(content)) { view, action in
            AnyView(view.accessibilityAction(named: Text(action.label)) { action.handler() })
        }
    }
}
