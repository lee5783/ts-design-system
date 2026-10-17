import SwiftUI
import Testing
import UIKit
import ViewInspector
@testable import TSDesignSystem

@MainActor
@Suite("TSAvatar behaviour")
struct TSAvatarBehaviorTests {
    static let photo = UIGraphicsImageRenderer(size: CGSize(width: 8, height: 8)).image { context in
        UIColor.systemTeal.setFill()
        context.fill(CGRect(x: 0, y: 0, width: 8, height: 8))
    }

    @Test("The name is the accessibility label of one image element", arguments: TSAvatarSize.allCases)
    func accessibility(size: TSAvatarSize) throws {
        let sut = TSAvatar(name: "Ada Lovelace", size: size)
        let body = try sut.body.inspect()
        #expect(try body.accessibilityLabel().string() == "Ada Lovelace")
        #expect(try body.accessibilityTraits().contains(.isImage))
        #expect(try !body.accessibilityTraits().contains(.isButton))
    }

    @Test("Without an image it shows the clamped initials at a fixed size", arguments: TSAvatarSize.allCases)
    func initials(size: TSAvatarSize) throws {
        let sut = TSAvatar(name: "Grace Brewster Hopper", size: size)
        let resolved = TSAvatarStyleSpec.resolve(.init(size: size, content: .initials))
        let text = try sut.inspect().find(text: "GH")
        #expect(try text.lineLimit() == 1)
        #expect(try text.minimumScaleFactor() == resolved.initialsMinimumScaleFactor)
        #expect(try text.attributes().font().size() == resolved.initialsFontSize)
        #expect(try sut.body.inspect().fixedFrame().width == resolved.diameter)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(TSIcon.self)
        }
    }

    @Test("A usable image replaces the initials")
    func image() throws {
        let sut = TSAvatar(name: "Ada Lovelace", image: Self.photo)
        #expect(throws: (any Error).self) {
            try sut.inspect().find(text: "AL")
        }
        #expect(try sut.inspect().find(ViewType.Image.self).actualImage().uiImage() === Self.photo)
    }

    @Test("An image that failed to decode falls back to the initials")
    func failedImage() throws {
        let broken = UIImage(data: Data("not a photo".utf8)) ?? UIImage()
        let sut = TSAvatar(name: "Ada Lovelace", image: broken)
        #expect(try sut.inspect().find(text: "AL").string() == "AL")
    }

    @Test("A failed or pending AsyncImage phase falls back to the initials")
    func phases() throws {
        struct LoadFailed: Error {}
        let failed = TSAvatar(name: "Alan Turing", phase: .failure(LoadFailed()))
        let pending = TSAvatar(name: "Alan Turing", phase: .empty)
        let loaded = TSAvatar(name: "Alan Turing", phase: .success(Image(uiImage: Self.photo)))
        #expect(try failed.inspect().find(text: "AT").string() == "AT")
        #expect(try pending.inspect().find(text: "AT").string() == "AT")
        #expect(throws: (any Error).self) {
            try loaded.inspect().find(text: "AT")
        }
    }

    @Test("A name without letters shows the placeholder icon")
    func placeholder() throws {
        let sut = TSAvatar(name: "   ", size: .lg)
        let icon = try sut.inspect().find(TSIcon.self)
        #expect(try icon.actualView().body.inspect().fixedFrame().width == 24)
    }

    @Test("A blank name hides the avatar from accessibility, a named one stays exposed")
    func blankNameIsHidden() throws {
        #expect(try TSAvatar(name: "   ").body.inspect().accessibilityHidden())
        #expect(try !TSAvatar(name: "Ada Lovelace").body.inspect().accessibilityHidden())
    }
}
