import SwiftUI

public struct TSAvatar: View {
    let name: String
    let image: Image?
    let size: TSAvatarSize

    public init(name: String, image: UIImage? = nil, size: TSAvatarSize = .md) {
        self.name = name
        self.image = TSAvatarImage.usable(image).map(Image.init(uiImage:))
        self.size = size
    }

    public init(name: String, phase: AsyncImagePhase, size: TSAvatarSize = .md) {
        self.name = name
        self.image = phase.image
        self.size = size
    }

    public var body: some View {
        let initials = TSInitials.initials(for: name)
        let content = TSAvatarContent.make(initials: initials, hasImage: image != nil)
        let resolved = TSAvatarStyleSpec.resolve(.init(size: size, content: content))
        ZStack {
            Circle()
                .fill(resolved.fill.color)
            if let image, resolved.showsImage {
                image
                    .resizable()
                    .scaledToFill()
            } else if resolved.showsInitials {
                Text(initials)
                    .font(.system(size: resolved.initialsFontSize, weight: TSFontWeight.fontWeight(resolved.initialsFontWeight)))
                    .lineLimit(1)
                    .minimumScaleFactor(resolved.initialsMinimumScaleFactor)
                    .padding(resolved.contentInset)
            } else {
                TSIcon(.user, size: resolved.placeholderIconSize)
            }
        }
        .foregroundColor(resolved.foreground.color)
        .frame(width: resolved.diameter, height: resolved.diameter)
        .clipShape(Circle())
    }
}
