import Foundation

public extension TSControlState {
    static func make(isPressed: Bool = false, isEnabled: Bool = true, isLoading: Bool = false, isFocused: Bool = false) -> TSControlState {
        var state: TSControlState = []
        if isLoading {
            state.insert(.loading)
        } else if !isEnabled {
            state.insert(.disabled)
        } else if isPressed {
            state.insert(.pressed)
        }
        if isFocused, isEnabled {
            state.insert(.focused)
        }
        return state
    }
}
