import SwiftUI
import Testing
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSBadge behaviour")
struct TSBadgeBehaviorTests {
    @Test("The text is the accessibility label of one static element", arguments: TSBadgeTone.allCases)
    func accessibility(tone: TSBadgeTone) throws {
        let sut = TSBadge("Failed", tone: tone, showsDot: true, icon: .xCircle)
        let traits = try sut.body.inspect().accessibilityTraits()
        #expect(try sut.body.inspect().accessibilityLabel().string() == "Failed")
        #expect(traits.contains(.isStaticText))
        #expect(!traits.contains(.isButton))
    }

    @Test("A badge is not a button")
    func notAButton() throws {
        let sut = TSBadge("New", tone: .accent)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(ViewType.Button.self)
        }
    }

    @Test("The icon is shown only when set, and is decorative")
    func icon() throws {
        let plain = TSBadge("Draft")
        #expect(throws: (any Error).self) {
            try plain.inspect().find(TSIcon.self)
        }
        let withIcon = TSBadge("Active", tone: .success, icon: .checkCircle)
        let icon = try withIcon.inspect().find(TSIcon.self)
        #expect(try icon.actualView().body.inspect().accessibilityHidden())
        #expect(try icon.actualView().body.inspect().fixedFrame().width == 16)
    }

    @Test("The dot is shown only when requested")
    func dot() throws {
        #expect(throws: (any Error).self) {
            try TSBadge("Draft").inspect().find(ViewType.Shape.self) { try $0.fixedFrame().width == 6 }
        }
        let dot = try TSBadge("Live", tone: .danger, showsDot: true).inspect().find(ViewType.Shape.self) { try $0.fixedFrame().width == 6 }
        #expect(try dot.fixedFrame().height == 6)
    }
}
