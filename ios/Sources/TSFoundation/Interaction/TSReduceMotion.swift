import SwiftUI
import TSTokens
import UIKit

public enum TSReduceMotion {
    @MainActor
    public static var isEnabled: Bool {
        UIAccessibility.isReduceMotionEnabled
    }

    public static func duration(_ duration: TimeInterval, reduceMotion: Bool) -> TimeInterval {
        reduceMotion ? 0 : duration
    }

    public static func animation(
        duration: TimeInterval = TSMotion.durationBase,
        easing: TSCubicBezier = TSMotion.easingStandard,
        reduceMotion: Bool
    ) -> Animation? {
        reduceMotion ? nil : easing.animation(duration: duration)
    }

    public static func transition(_ preferred: AnyTransition, reduceMotion: Bool) -> AnyTransition {
        reduceMotion ? .opacity : preferred
    }

    @MainActor
    public static func animate(
        duration: TimeInterval = TSMotion.durationBase,
        easing: TSCubicBezier = TSMotion.easingStandard,
        reduceMotion: Bool = isEnabled,
        animations: @escaping @MainActor () -> Void,
        completion: (@MainActor () -> Void)? = nil
    ) {
        guard !reduceMotion else {
            animations()
            completion?()
            return
        }
        let animator = UIViewPropertyAnimator(duration: duration, timingParameters: easing.timingParameters)
        animator.addAnimations(animations)
        if let completion {
            animator.addCompletion { _ in completion() }
        }
        animator.startAnimation()
    }
}

public extension View {
    func tsAnimation<Value: Equatable>(
        _ value: Value,
        duration: TimeInterval = TSMotion.durationBase,
        easing: TSCubicBezier = TSMotion.easingStandard
    ) -> some View {
        modifier(TSReduceMotionAnimationModifier(value: value, duration: duration, easing: easing))
    }
}

struct TSReduceMotionAnimationModifier<Value: Equatable>: ViewModifier {
    let value: Value
    let duration: TimeInterval
    let easing: TSCubicBezier

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    func body(content: Content) -> some View {
        content.animation(TSReduceMotion.animation(duration: duration, easing: easing, reduceMotion: reduceMotion), value: value)
    }
}
