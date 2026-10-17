import Testing
import TSTokens
@testable import TSFoundation

private enum SampleStyleSpec: TSStyleSpec {
    struct Configuration: Hashable, Sendable {
        let isPrimary: Bool
        let state: TSControlState
    }

    static let allConfigurations: [Configuration] = [true, false].flatMap { isPrimary in
        [TSControlState.normal, .pressed, .disabled].map { Configuration(isPrimary: isPrimary, state: $0) }
    }

    static func resolve(_ configuration: Configuration) -> TSColor {
        switch (configuration.isPrimary, configuration.state.contains(.pressed)) {
        case (true, true): .accentPressed
        case (true, false): .accentPrimary
        case (false, true): .surfacePressed
        case (false, false): .surfaceCard
        }
    }
}

@Suite("TSStyleSpec")
struct TSStyleSpecTests {
    @Test("The matrix resolves every configuration exactly once")
    func matrix() {
        let matrix = SampleStyleSpec.matrix
        #expect(matrix.count == 6)
        #expect(Set(matrix.map(\.configuration)).count == 6)
        let consistent = matrix.allSatisfy { SampleStyleSpec.resolve($0.configuration) == $0.resolved }
        #expect(consistent)
    }

    @Test("Disabled and loading states are not interactive")
    func interactiveStates() {
        #expect(TSControlState.normal.isInteractive)
        let interactive = [TSControlState.pressed, .focused, .selected, .error].allSatisfy(\.isInteractive)
        #expect(interactive)
        #expect(!TSControlState.disabled.isInteractive)
        #expect(!TSControlState.loading.isInteractive)
        #expect(!TSControlState([.selected, .disabled]).isInteractive)
    }
}
