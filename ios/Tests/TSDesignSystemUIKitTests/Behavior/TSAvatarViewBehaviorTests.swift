import Testing
import TSDesignSystemUIKit
import UIKit

@MainActor
@Suite("TSAvatarView behaviour")
struct TSAvatarViewBehaviorTests {
    static let photo = UIGraphicsImageRenderer(size: CGSize(width: 8, height: 8)).image { context in
        UIColor.systemTeal.setFill()
        context.fill(CGRect(x: 0, y: 0, width: 8, height: 8))
    }

    private func visible<View: UIView>(_ kind: View.Type, in avatar: TSAvatarView) -> View? {
        avatar.subviews.compactMap { $0 as? View }.first { !$0.isHidden && ObjectIdentifier(type(of: $0)) == ObjectIdentifier(kind) }
    }

    @Test("Each size is a fixed circle, even at AX5", arguments: TSAvatarSize.allCases)
    func size(size: TSAvatarSize) {
        let avatar = TSAvatarView(name: "Ada Lovelace", size: size)
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        avatar.translatesAutoresizingMaskIntoConstraints = false
        host.content.addSubview(avatar)
        host.window.layoutIfNeeded()
        let diameter = TSAvatarStyleSpec.resolve(.init(size: size)).diameter
        #expect(avatar.bounds.size == CGSize(width: diameter, height: diameter))
        #expect(avatar.layer.cornerRadius == diameter / 2)
        #expect(avatar.clipsToBounds)
        let label = visible(UILabel.self, in: avatar)
        #expect(label?.font.pointSize == TSAvatarStyleSpec.resolve(.init(size: size)).initialsFontSize)
        #expect(label?.adjustsFontSizeToFitWidth == true)
    }

    @Test("Without an image it shows the initials")
    func initials() {
        let avatar = TSAvatarView(name: "Grace Brewster Hopper")
        #expect(visible(UILabel.self, in: avatar)?.text == "GH")
        #expect(visible(UIImageView.self, in: avatar) == nil)
    }

    @Test("A usable image replaces the initials, and clearing it brings them back")
    func image() {
        let avatar = TSAvatarView(name: "Ada Lovelace", image: Self.photo)
        #expect(visible(UIImageView.self, in: avatar)?.image === Self.photo)
        #expect(visible(UILabel.self, in: avatar) == nil)
        avatar.image = nil
        #expect(visible(UILabel.self, in: avatar)?.text == "AL")
        #expect(visible(UIImageView.self, in: avatar) == nil)
    }

    @Test("An image that failed to decode falls back to the initials")
    func failedImage() {
        let avatar = TSAvatarView(name: "Alan Turing", image: UIImage())
        #expect(visible(UILabel.self, in: avatar)?.text == "AT")
        #expect(visible(UIImageView.self, in: avatar) == nil)
    }

    @Test("A name without letters shows the placeholder icon")
    func placeholder() {
        let avatar = TSAvatarView(name: " ", size: .lg)
        let icon = visible(TSIconView.self, in: avatar)
        #expect(icon?.name == .user)
        #expect(icon?.size == .lg)
        #expect(visible(UILabel.self, in: avatar) == nil)
    }
}
