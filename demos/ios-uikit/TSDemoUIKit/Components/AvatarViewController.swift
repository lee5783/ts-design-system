import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class AvatarViewController: DemoPageViewController {
    private lazy var playgroundAvatar: TSAvatarView = {
        let avatar = TSAvatarView(name: SamplePeople.all[0].name)
        avatar.accessibilityIdentifier = "demo.avatar.playground.avatar"
        return avatar
    }()

    init() {
        super.init(page: .avatar)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [sizesSection(), contentsSection(), initialsSection(), playgroundSection()]
    }

    private func sizesSection() -> UIView {
        let section = DemoComponentSectionView(.sizes, views: [])
        let name = SamplePeople.all[0].name
        TSAvatarSize.allCases.forEach { size in
            section.addFullWidth(DemoComponentRowView([
                TSAvatarView(name: name, size: size),
                TSAvatarView(name: name, image: DemoSamplePhoto.image, size: size),
                DemoPlayground.caption(size.rawValue)
            ]))
        }
        return section
    }

    private func contentsSection() -> UIView {
        let name = SamplePeople.all[1].name
        let rows: [UIView] = TSAvatarSize.allCases.map { size in
            row([
                TSAvatarView(name: name, size: size),
                TSAvatarView(name: name, image: DemoSamplePhoto.image, size: size),
                TSAvatarView(name: "", size: size)
            ])
        }
        let caption = DemoPlayground.caption(
            [DemoStrings.string(.initials), DemoStrings.string(.photo), DemoStrings.string(.noName)].joined(separator: " · ")
        )
        return DemoComponentSectionView(.contents, views: rows + [caption])
    }

    private func initialsSection() -> UIView {
        let section = DemoComponentSectionView(.initials, views: [])
        SamplePeople.all.forEach { person in
            section.addFullWidth(DemoComponentRowView([TSAvatarView(name: person.name), DemoPlayground.caption(person.name)]))
        }
        return section
    }

    private func playgroundSection() -> UIView {
        let avatar = playgroundAvatar
        let section = DemoComponentSectionView(.playground, views: [])
        let sizes = DemoPlayground.segmented(TSAvatarSize.allCases.map(\.rawValue)) { index in
            avatar.size = TSAvatarSize.allCases[index]
        }
        sizes.selectedSegmentIndex = TSAvatarSize.allCases.firstIndex(of: avatar.size) ?? 0
        section.addFullWidth(sizes)
        section.addFullWidth(DemoPlayground.segmented(SamplePeople.all.map(\.id)) { index in
            avatar.name = SamplePeople.all[index].name
        })
        section.addFullWidth(DemoPlayground.toggle(.photo, identifier: "demo.avatar.playground.photo") { avatar.image = $0 ? DemoSamplePhoto.image : nil })
        section.addArrangedSubview(avatar)
        return section
    }

    private func row(_ views: [UIView]) -> UIStackView {
        let stack = UIStackView(arrangedSubviews: views)
        stack.axis = .horizontal
        stack.alignment = .center
        stack.spacing = TSSpacing.space3
        return stack
    }
}
