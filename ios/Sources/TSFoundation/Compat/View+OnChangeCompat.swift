import SwiftUI

package extension View {
    func tsOnChange<Value: Equatable>(
        of value: Value,
        initial: Bool = false,
        _ action: @escaping (_ oldValue: Value, _ newValue: Value) -> Void
    ) -> some View {
        modifier(TSOnChangeModifier(value: value, initial: initial, action: action))
    }
}

struct TSOnChangeModifier<Value: Equatable>: ViewModifier {
    let value: Value
    let initial: Bool
    let action: (Value, Value) -> Void

    func body(content: Content) -> some View {
        if #available(iOS 17, *) {
            content.onChange(of: value, initial: initial, action)
        } else {
            content
                .onAppear {
                    if initial {
                        action(value, value)
                    }
                }
                .onChange(of: value) { [value] newValue in
                    action(value, newValue)
                }
        }
    }
}
