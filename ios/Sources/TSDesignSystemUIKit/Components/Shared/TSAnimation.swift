import UIKit

enum TSAnimation {
    @MainActor
    static func animate(
        duration: TimeInterval = TSMotion.durationBase,
        easing: TSCubicBezier = TSMotion.easingStandard,
        isInstant: Bool = false,
        animations: @escaping @MainActor () -> Void,
        completion: (@MainActor () -> Void)? = nil
    ) {
        guard !isInstant else {
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
