import UIKit

enum DemoSamplePhoto {
    static let image = UIGraphicsImageRenderer(size: CGSize(width: 120, height: 120)).image { context in
        UIColor(red: 0.16, green: 0.45, blue: 0.62, alpha: 1).setFill()
        context.fill(CGRect(x: 0, y: 0, width: 120, height: 120))
        UIColor(red: 0.96, green: 0.76, blue: 0.55, alpha: 1).setFill()
        context.cgContext.fillEllipse(in: CGRect(x: 40, y: 22, width: 40, height: 40))
        context.cgContext.fillEllipse(in: CGRect(x: 22, y: 70, width: 76, height: 70))
    }
}
