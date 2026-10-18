import SwiftUI
import TSTokens

public enum TSPressFeedback: String, CaseIterable, Hashable, Sendable {
    case fill
    case opacity

    public static let duration: TimeInterval = TSMotion.durationFast

    public func opacity(isPressed: Bool) -> Double {
        switch self {
        case .fill: 1
        case .opacity: isPressed ? TSOpacity.opacityPressed : 1
        }
    }

    public func color(_ base: TSColor, pressed: TSColor, isPressed: Bool) -> TSColor {
        switch self {
        case .fill: isPressed ? pressed : base
        case .opacity: base
        }
    }
}

public extension View {
    func tsPressFeedback(_ feedback: TSPressFeedback, isPressed: Bool) -> some View {
        modifier(TSPressFeedbackModifier(feedback: feedback, isPressed: isPressed))
    }
}

struct TSPressFeedbackModifier: ViewModifier {
    let feedback: TSPressFeedback
    let isPressed: Bool

    func body(content: Content) -> some View {
        content
            .opacity(feedback.opacity(isPressed: isPressed))
            .animation(TSMotion.easingStandard.animation(duration: TSPressFeedback.duration), value: isPressed)
    }
}
