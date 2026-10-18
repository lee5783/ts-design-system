import SwiftUI

enum TSAnimation {
    static func standard(
        duration: TimeInterval = TSMotion.durationBase,
        easing: TSCubicBezier = TSMotion.easingStandard
    ) -> Animation {
        easing.animation(duration: duration)
    }

    static var pressFeedback: Animation {
        standard(duration: TSPressFeedback.duration)
    }
}
