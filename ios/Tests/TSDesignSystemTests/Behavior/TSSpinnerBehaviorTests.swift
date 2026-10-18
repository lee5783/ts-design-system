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
}
