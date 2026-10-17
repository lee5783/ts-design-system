import SwiftUI

struct TSDimmedByContainerKey: EnvironmentKey {
    static let defaultValue = false
}

extension EnvironmentValues {
    var tsDimmedByContainer: Bool {
        get { self[TSDimmedByContainerKey.self] }
        set { self[TSDimmedByContainerKey.self] = newValue }
    }
}
