import UIKit

@MainActor
enum RenderedInk {
    struct Pixel: Equatable {
        let red: Int
        let green: Int
        let blue: Int
        let alpha: Int
    }

    static func strongest(in view: UIView) -> Pixel? {
        let width = Int(view.bounds.width)
        let height = Int(view.bounds.height)
        guard width > 0, height > 0 else { return nil }
        var bytes = [UInt8](repeating: 0, count: width * height * 4)
        let drawn: Bool = bytes.withUnsafeMutableBytes { buffer in
            guard let context = CGContext(
                data: buffer.baseAddress, width: width, height: height, bitsPerComponent: 8, bytesPerRow: width * 4,
                space: CGColorSpaceCreateDeviceRGB(), bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
            ) else { return false }
            view.layer.render(in: context)
            return true
        }
        guard drawn else { return nil }
        let offset = stride(from: 0, to: bytes.count, by: 4).max { bytes[$0 + 3] < bytes[$1 + 3] } ?? 0
        let alpha = Int(bytes[offset + 3])
        guard alpha > 0 else { return Pixel(red: 0, green: 0, blue: 0, alpha: 0) }
        func straight(_ value: UInt8) -> Int { Int((Double(value) * 255 / Double(alpha)).rounded()) }
        return Pixel(red: straight(bytes[offset]), green: straight(bytes[offset + 1]), blue: straight(bytes[offset + 2]), alpha: alpha)
    }

    static func isClose(_ lhs: Pixel, _ rhs: Pixel, tolerance: Int = 3) -> Bool {
        abs(lhs.red - rhs.red) <= tolerance
            && abs(lhs.green - rhs.green) <= tolerance
            && abs(lhs.blue - rhs.blue) <= tolerance
            && abs(lhs.alpha - rhs.alpha) <= tolerance
    }
}
