import TSDesignSystemUIKit
import UIKit

final class AvatarGalleryUIView: GalleryStackView {
    init() {
        super.init(frame: .zero)
        stack.spacing = TSSpacing.space4
        for size in TSAvatarSize.allCases {
            addRow([
                TSAvatarView(name: "Ada Lovelace", size: size),
                TSAvatarView(name: "Wolfgang Mozart", size: size),
                TSAvatarView(name: "山田太郎", size: size),
                TSAvatarView(name: "Grace Hopper", image: SampleAvatarPhoto.image, size: size),
                TSAvatarView(name: "", size: size)
            ])
        }
    }
}

enum SampleAvatarPhoto {
    static let image = UIGraphicsImageRenderer(size: CGSize(width: 120, height: 80)).image { context in
        UIColor(red: 0.16, green: 0.45, blue: 0.62, alpha: 1).setFill()
        context.fill(CGRect(x: 0, y: 0, width: 120, height: 80))
        UIColor(red: 0.96, green: 0.76, blue: 0.55, alpha: 1).setFill()
        context.cgContext.fillEllipse(in: CGRect(x: 44, y: 12, width: 32, height: 32))
        context.cgContext.fillEllipse(in: CGRect(x: 30, y: 48, width: 60, height: 52))
    }
}
