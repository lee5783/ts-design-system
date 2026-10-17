import AppKit

let arguments = CommandLine.arguments
guard arguments.count == 4, let pixels = Int(arguments[3]) else {
    FileHandle.standardError.write(Data("usage: render-app-icon <source.svg> <output.png> <pixels>\n".utf8))
    exit(2)
}

guard let image = NSImage(contentsOf: URL(fileURLWithPath: arguments[1])) else {
    FileHandle.standardError.write(Data("render-app-icon: cannot read \(arguments[1])\n".utf8))
    exit(1)
}

var proposedRect = NSRect(x: 0, y: 0, width: pixels, height: pixels)
let hints: [NSImageRep.HintKey: Any] = [.ctm: NSAffineTransform()]
guard
    let source = image.cgImage(forProposedRect: &proposedRect, context: nil, hints: hints),
    let context = CGContext(
        data: nil,
        width: pixels,
        height: pixels,
        bitsPerComponent: 8,
        bytesPerRow: 0,
        space: CGColorSpace(name: CGColorSpace.sRGB)!,
        bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue
    )
else {
    FileHandle.standardError.write(Data("render-app-icon: cannot render \(arguments[1])\n".utf8))
    exit(1)
}

context.interpolationQuality = .high
context.draw(source, in: CGRect(x: 0, y: 0, width: pixels, height: pixels))

guard let output = context.makeImage(), let png = NSBitmapImageRep(cgImage: output).representation(using: .png, properties: [:]) else {
    exit(1)
}
try png.write(to: URL(fileURLWithPath: arguments[2]))
