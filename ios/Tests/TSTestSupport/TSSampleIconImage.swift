import UIKit

public enum TSSampleIconImage {
    public static func controller(side: CGFloat = 24) -> UIImage {
        let unit = side / 24
        let format = UIGraphicsImageRendererFormat.preferred()
        format.opaque = false
        return UIGraphicsImageRenderer(size: CGSize(width: side, height: side), format: format).image { _ in
            let body = UIBezierPath(roundedRect: CGRect(x: 2 * unit, y: 7 * unit, width: 20 * unit, height: 10 * unit), cornerRadius: 5 * unit)
            body.lineWidth = 1.75 * unit
            UIColor.black.setStroke()
            body.stroke()
            let pad = UIBezierPath()
            pad.move(to: CGPoint(x: 7 * unit, y: 10 * unit))
            pad.addLine(to: CGPoint(x: 7 * unit, y: 14 * unit))
            pad.move(to: CGPoint(x: 5 * unit, y: 12 * unit))
            pad.addLine(to: CGPoint(x: 9 * unit, y: 12 * unit))
            pad.lineWidth = 1.75 * unit
            pad.lineCapStyle = .round
            pad.stroke()
            UIColor.red.setFill()
            UIBezierPath(ovalIn: CGRect(x: 15 * unit, y: 10 * unit, width: 2 * unit, height: 2 * unit)).fill()
            UIColor.blue.setFill()
            UIBezierPath(ovalIn: CGRect(x: 17 * unit, y: 12 * unit, width: 2 * unit, height: 2 * unit)).fill()
        }
    }
}
