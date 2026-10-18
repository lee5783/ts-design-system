import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSIcon behaviour")
struct TSIconBehaviorTests {
    @Test("An unlabelled icon is hidden from accessibility")
    func decorative() throws {
        let sut = TSIcon(.home)
        #expect(try sut.body.inspect().accessibilityHidden())
    }

    @Test("Each size renders a frame of the token size", arguments: TSIconSize.allCases)
    func size(size: TSIconSize) throws {
        let sut = TSIcon(.star, size: size)
        let frame = try sut.body.inspect().fixedFrame()
        #expect(frame.width == size.pointSize)
        #expect(frame.height == size.pointSize)
    }
}
