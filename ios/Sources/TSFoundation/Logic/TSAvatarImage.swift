#if canImport(UIKit)
import UIKit
#elseif canImport(AppKit)
import AppKit
#endif

public enum TSAvatarImage {
    #if canImport(UIKit)
    public static func usable(_ image: UIImage?) -> UIImage? {
        guard let image, image.size.width > 0, image.size.height > 0, image.cgImage != nil || image.ciImage != nil else {
            return nil
        }
        return image
    }
    #elseif canImport(AppKit)
    public static func usable(_ image: NSImage?) -> NSImage? {
        guard let image, image.size.width > 0, image.size.height > 0, image.cgImage(forProposedRect: nil, context: nil, hints: nil) != nil else {
            return nil
        }
        return image
    }
    #endif
}
