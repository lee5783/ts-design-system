import UIKit

public enum TSAvatarImage {
    public static func usable(_ image: UIImage?) -> UIImage? {
        guard let image, image.size.width > 0, image.size.height > 0, image.cgImage != nil || image.ciImage != nil else {
            return nil
        }
        return image
    }
}
