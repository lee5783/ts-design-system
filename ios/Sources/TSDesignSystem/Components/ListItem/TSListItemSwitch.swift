import SwiftUI

public struct TSListItemSwitch: View {
    let label: String
    @Binding var isOn: Bool

    public init(_ label: String, isOn: Binding<Bool>) {
        self.label = label
        _isOn = isOn
    }

    public var body: some View {
        let resolved = TSSwitchStyleSpec.resolve(.init(isOn: isOn))
        TSSwitch(label, isOn: $isOn, isLabelHidden: true)
            .padding(.vertical, -TSHitArea.outset(for: resolved.trackHeight, minimum: resolved.minHeight))
    }
}
