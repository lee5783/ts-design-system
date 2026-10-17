import SwiftUI

public struct TSCheckbox: View {
    let label: String
    @Binding var isOn: Bool
    let isLabelHidden: Bool
    var stateOverride: TSControlState?
    var togglePath: TSCompatPath = TSCompat.toggleTrait
    var playHaptic: @MainActor (TSHapticEvent) -> Void = { TSHaptics.shared.play($0) }

    @FocusState private var isFocused: Bool

    public init(_ label: String, isOn: Binding<Bool>, isLabelHidden: Bool = false) {
        self.label = label
        _isOn = isOn
        self.isLabelHidden = isLabelHidden
    }

    public var body: some View {
        Button(action: toggle) {
            Text(label)
        }
        .buttonStyle(
            TSCheckboxStyle(
                isOn: isOn,
                isLabelHidden: isLabelHidden,
                isFocused: isFocused,
                stateOverride: stateOverride
            )
        )
        .focused($isFocused)
        .tsFocusEffectDisabled()
        .accessibilityLabel(Text(label))
        .tsToggleAccessibility(isOn: isOn, kind: .checkbox, path: togglePath)
    }

    func forcingState(_ state: TSControlState) -> Self {
        var copy = self
        copy.stateOverride = state
        return copy
    }

    func usingTogglePath(_ path: TSCompatPath) -> Self {
        var copy = self
        copy.togglePath = path
        return copy
    }

    func playingHaptics(with playHaptic: @escaping @MainActor (TSHapticEvent) -> Void) -> Self {
        var copy = self
        copy.playHaptic = playHaptic
        return copy
    }

    private func toggle() {
        isOn.toggle()
        playHaptic(.selection)
    }
}
