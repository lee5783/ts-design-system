import SwiftUI

@MainActor
final class SelectionBox {
    var value: String

    init(_ value: String) {
        self.value = value
    }

    var binding: Binding<String> {
        Binding(get: { self.value }, set: { self.value = $0 })
    }
}
