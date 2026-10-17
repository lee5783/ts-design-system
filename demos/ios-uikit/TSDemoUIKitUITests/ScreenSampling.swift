import XCTest

extension XCUIApplication {
    @MainActor
    func inkContrast(in rect: CGRect) -> Double {
        let image = screenshot().image
        guard let cgImage = image.cgImage else { return .nan }
        let scale = CGFloat(cgImage.width) / image.size.width
        let pixels = CGRect(x: rect.minX * scale, y: rect.minY * scale, width: rect.width * scale, height: rect.height * scale).integral
        guard let crop = cgImage.cropping(to: pixels) else { return .nan }
        let width = crop.width
        let height = crop.height
        var rgba = [UInt8](repeating: 0, count: width * height * 4)
        let drawn: Bool = rgba.withUnsafeMutableBytes { buffer in
            guard let context = CGContext(
                data: buffer.baseAddress, width: width, height: height, bitsPerComponent: 8, bytesPerRow: width * 4,
                space: CGColorSpaceCreateDeviceRGB(), bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
            ) else { return false }
            context.draw(crop, in: CGRect(x: 0, y: 0, width: width, height: height))
            return true
        }
        guard drawn else { return .nan }
        let luminances = stride(from: 0, to: rgba.count, by: 4).map { (index: Int) -> Double in
            let red = 0.2126 * Double(rgba[index])
            let green = 0.7152 * Double(rgba[index + 1])
            let blue = 0.0722 * Double(rgba[index + 2])
            return (red + green + blue) / 255
        }
        return (luminances.max() ?? 0) - (luminances.min() ?? 0)
    }
}

extension XCUIElement {
    @MainActor
    func waitUntilSettled() {
        var previous = CGRect.null
        for _ in 0..<20 {
            let current = frame
            if current == previous {
                return
            }
            previous = current
            RunLoop.current.run(until: Date().addingTimeInterval(0.15))
        }
    }
}
