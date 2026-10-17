import SwiftUI
import UIKit

package enum TSTrackingCompat {
    package static func attributedString(_ string: String, tracking: CGFloat) -> AttributedString {
        var attributed = AttributedString(string)
        if tracking != 0 {
            attributed.kern = tracking
        }
        return attributed
    }
}

package extension Text {
    func tsTracking(_ tracking: CGFloat) -> Text {
        self.tracking(tracking)
    }

    func tsFontWeight(_ weight: Font.Weight) -> Text {
        fontWeight(weight)
    }
}
