import UIKit

public enum TSListGroupPosition: String, CaseIterable, Hashable, Sendable {
    case single
    case first
    case middle
    case last

    public init(index: Int, count: Int) {
        switch (index, count) {
        case (_, ...1): self = .single
        case (0, _): self = .first
        case (count - 1, _): self = .last
        default: self = .middle
        }
    }

    public var showsTopDivider: Bool {
        self == .middle || self == .last
    }

    public var roundedCorners: CACornerMask {
        switch self {
        case .single: [.layerMinXMinYCorner, .layerMaxXMinYCorner, .layerMinXMaxYCorner, .layerMaxXMaxYCorner]
        case .first: [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        case .middle: []
        case .last: [.layerMinXMaxYCorner, .layerMaxXMaxYCorner]
        }
    }

    var hasTopEdge: Bool {
        self == .single || self == .first
    }

    var hasBottomEdge: Bool {
        self == .single || self == .last
    }
}
