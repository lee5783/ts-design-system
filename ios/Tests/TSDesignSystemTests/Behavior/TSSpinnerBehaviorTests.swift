import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSSpinner behaviour")
struct TSSpinnerBehaviorTests {
    private func size(of view: some View) -> CGSize {
        UIHostingController(rootView: view).sizeThatFits(in: CGSize(width: 390, height: 390))
    }

    @Test("Each size renders a frame of the icon token size", arguments: zip(TSSpinnerSize.allCases, [CGFloat(16), 20, 24]))
    func frame(size spinnerSize: TSSpinnerSize, expected: CGFloat) {
        #expect(size(of: TSSpinner(size: spinnerSize)) == CGSize(width: expected, height: expected))
    }

    @Test("The spinner is one element labelled Loading that updates frequently")
    func defaultLabel() throws {
        let sut = try TSSpinner().body.inspect()
        #expect(try sut.accessibilityLabel().string() == "Loading")
        #expect(try sut.accessibilityTraits().contains(.updatesFrequently))
    }

    @Test("A custom label replaces Loading")
    func customLabel() throws {
        #expect(try TSSpinner(label: "Loading messages").body.inspect().accessibilityLabel().string() == "Loading messages")
    }

    @Test("A nil label hides the spinner, as the Button spinner is hidden")
    func hidden() throws {
        #expect(try TSSpinner(label: nil).body.inspect().accessibilityHidden())
        #expect(try TSSpinner(size: 16, lineWidth: 2, color: .black).body.inspect().accessibilityHidden())
    }
}
