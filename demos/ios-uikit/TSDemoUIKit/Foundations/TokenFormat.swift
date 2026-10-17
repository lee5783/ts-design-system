import TSDesignSystemUIKit
import UIKit

enum TokenFormat {
    static func points(_ value: CGFloat) -> String {
        value.rounded() == value ? "\(Int(value)) pt" : "\(value) pt"
    }

    static func hex(_ rgba: TSRGBA) -> String {
        let rgb = String(format: "#%02X%02X%02X", rgba.red, rgba.green, rgba.blue)
        return rgba.alpha == 0xFF ? rgb : rgb + String(format: " %d%%", Int((rgba.opacity * 100).rounded()))
    }
}
