import SwiftUI

struct TSSpinner: View {
    let size: CGFloat
    let lineWidth: CGFloat
    let color: Color

    @State private var isRotating = false

    var body: some View {
        Circle()
            .trim(from: 0, to: 0.75)
            .stroke(color, style: StrokeStyle(lineWidth: lineWidth, lineCap: .butt))
            .padding(lineWidth / 2)
            .frame(width: size, height: size)
            .rotationEffect(.degrees(isRotating ? 405 : 45))
            .animation(.linear(duration: TSComponentTokens.spinnerRotationDuration).repeatForever(autoreverses: false), value: isRotating)
            .onAppear { isRotating = true }
            .accessibilityHidden(true)
    }
}
