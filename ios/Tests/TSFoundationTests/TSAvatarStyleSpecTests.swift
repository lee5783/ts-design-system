import Testing
import TSFoundation
import TSTokens
import UIKit

@Suite("TSAvatarStyleSpec")
struct TSAvatarStyleSpecTests {
    static let expectedGeometry: [TSAvatarSize: (diameter: CGFloat, fontSize: CGFloat, icon: TSIconSize)] = [
        .xs: (24, 10, .sm),
        .sm: (32, 12, .sm),
        .md: (40, 14, .md),
        .lg: (56, 20, .lg),
    ]

    @Test("The matrix covers 4 sizes × 3 contents once each")
    func matrixIsComplete() {
        let configurations = TSAvatarStyleSpec.allConfigurations
        #expect(configurations.count == 12)
        #expect(Set(configurations).count == 12)
    }

    @Test("Every configuration resolves geometry, colours and the visible content", arguments: TSAvatarStyleSpec.allConfigurations)
    func resolves(configuration: TSAvatarStyleSpec.Configuration) throws {
        let resolved = TSAvatarStyleSpec.resolve(configuration)
        let expected = try #require(Self.expectedGeometry[configuration.size])
        #expect(resolved.diameter == expected.diameter)
        #expect(resolved.cornerRadius == expected.diameter / 2)
        #expect(resolved.initialsFontSize == expected.fontSize)
        #expect(resolved.initialsFontWeight == 600)
        #expect(resolved.placeholderIconSize == expected.icon)
        #expect(resolved.fill == .accentSubtle)
        #expect(resolved.foreground == .accentText)
        #expect(resolved.showsInitials == (configuration.content == .initials))
        #expect(resolved.showsImage == (configuration.content == .image))
        #expect(resolved.showsPlaceholder == (configuration.content == .placeholder))
        #expect([resolved.showsInitials, resolved.showsImage, resolved.showsPlaceholder].filter { $0 }.count == 1)
    }

    @Test(
        "Content falls back from image to initials to the placeholder",
        arguments: [
            ("AL", true, TSAvatarContent.image),
            ("AL", false, .initials),
            ("", true, .image),
            ("", false, .placeholder),
        ]
    )
    func content(initials: String, hasImage: Bool, expected: TSAvatarContent) {
        #expect(TSAvatarContent.make(initials: initials, hasImage: hasImage) == expected)
    }

    @Test("Only an image with pixels is usable")
    func usableImage() {
        let photo = UIGraphicsImageRenderer(size: CGSize(width: 4, height: 4)).image { context in
            UIColor.red.setFill()
            context.fill(CGRect(x: 0, y: 0, width: 4, height: 4))
        }
        #expect(TSAvatarImage.usable(photo) === photo)
        #expect(TSAvatarImage.usable(nil) == nil)
        #expect(TSAvatarImage.usable(UIImage()) == nil)
        #expect(TSAvatarImage.usable(UIImage(data: Data("not an image".utf8))) == nil)
    }
}
