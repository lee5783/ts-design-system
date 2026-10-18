import SwiftUI

public struct TSSpinner: View {
    let size: CGFloat
    let lineWidth: CGFloat
    let color: Color

    @State private var isRotating = false

    public init(size: TSSpinnerSize = .md, tone: TSSpinnerTone = .primary) {
        let resolved = TSSpinnerStyleSpec.resolve(.init(size: size, tone: tone))
        self.init(size: resolved.size, lineWidth: resolved.strokeWidth, color: resolved.color.color)
    }

    init(size: CGFloat, lineWidth: CGFloat, color: Color) {
        self.size = size
        self.lineWidth = lineWidth
        self.color = color
    }

    public var body: some View {
        Circle()
            .trim(from: 0, to: 0.75)
            .stroke(color, style: StrokeStyle(lineWidth: lineWidth, lineCap: .butt))
            .padding(lineWidth / 2)
            .frame(width: size, height: size)
            .rotationEffect(.degrees(isRotating ? 405 : 45))
            .animation(.linear(duration: TSComponentTokens.spinnerRotationDuration).repeatForever(autoreverses: false), value: isRotating)
            .onAppear { isRotating = true }
    }
}
