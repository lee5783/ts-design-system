import SwiftUI
import UIKit

@MainActor
final class ToggleBox {
    var value: Bool

    init(_ value: Bool = false) {
        self.value = value
    }

    var binding: Binding<Bool> {
        Binding(get: { self.value }, set: { self.value = $0 })
    }
}

@MainActor
enum RenderedPixels {
    static func image(of view: some View, size: CGSize) -> UIImage? {
        let host = UIHostingController(rootView: view)
        let window = UIWindow(frame: CGRect(origin: .zero, size: size))
        window.rootViewController = host
        window.isHidden = false
        host.view.frame = window.bounds
        host.view.layoutIfNeeded()
        let format = UIGraphicsImageRendererFormat()
        format.scale = 2
        return UIGraphicsImageRenderer(size: size, format: format).image {
            host.view.layer.render(in: $0.cgContext)
        }
    }

    static func isLight(_ image: UIImage?, at point: CGPoint) -> Bool {
        guard let image, let cgImage = image.cgImage else { return false }
        let pixelPoint = CGPoint(x: point.x * image.scale, y: point.y * image.scale)
        var pixel = [UInt8](repeating: 0, count: 4)
        let context = CGContext(
            data: &pixel,
            width: 1,
            height: 1,
            bitsPerComponent: 8,
            bytesPerRow: 4,
            space: CGColorSpaceCreateDeviceRGB(),
            bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
        )
        context?.translateBy(x: -pixelPoint.x, y: pixelPoint.y - CGFloat(cgImage.height) + 1)
        context?.draw(cgImage, in: CGRect(x: 0, y: 0, width: cgImage.width, height: cgImage.height))
        return pixel[0] > 230 && pixel[1] > 230 && pixel[2] > 230
    }
}
