import SwiftUI
@testable import TSDesignSystem

enum ToggleGalleryKind: String, CaseIterable {
    case checkbox
    case `switch`
}

enum ToggleSample {
    static let states: [(name: String, state: TSControlState?)] = [
        ("Default", nil),
        ("Pressed", .pressed),
        ("Disabled", nil),
        ("Focused", .focused)
    ]
    static let longLabel = "Email me about product updates, tips and occasional offers"
}

struct ToggleGalleryView: View {
    let kind: ToggleGalleryKind

    var body: some View {
        VStack(alignment: .leading, spacing: TSSpacing.space2) {
            ForEach([false, true], id: \.self) { isOn in
                ForEach(ToggleSample.states, id: \.name) { sample in
                    control("\(sample.name) \(isOn ? "on" : "off")", isOn: isOn, state: sample.state)
                        .disabled(sample.name == "Disabled")
                }
            }
            control(ToggleSample.longLabel, isOn: true, state: nil)
            HStack(spacing: TSSpacing.space4) {
                control("Hidden off", isOn: false, state: nil, isLabelHidden: true)
                control("Hidden on", isOn: true, state: nil, isLabelHidden: true)
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private func control(_ label: String, isOn: Bool, state: TSControlState?, isLabelHidden: Bool = false) -> some View {
        switch kind {
        case .checkbox:
            if let state {
                TSCheckbox(label, isOn: .constant(isOn), isLabelHidden: isLabelHidden).forcingState(state)
            } else {
                TSCheckbox(label, isOn: .constant(isOn), isLabelHidden: isLabelHidden)
            }
        case .switch:
            if let state {
                TSSwitch(label, isOn: .constant(isOn), isLabelHidden: isLabelHidden).forcingState(state)
            } else {
                TSSwitch(label, isOn: .constant(isOn), isLabelHidden: isLabelHidden)
            }
        }
    }
}
