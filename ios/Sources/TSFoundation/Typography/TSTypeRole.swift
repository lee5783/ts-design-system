import TSTokens

public enum TSTypeRole: String, CaseIterable, Hashable, Sendable {
    case display
    case title1
    case title2
    case title3
    case bodyLg
    case body
    case bodySm
    case label
    case caption
    case overline
    case code

    public var font: TSFont {
        switch self {
        case .display: .display
        case .title1: .title1
        case .title2: .title2
        case .title3: .title3
        case .bodyLg: .bodyLg
        case .body: .body
        case .bodySm: .bodySm
        case .label: .label
        case .caption: .caption
        case .overline: .overline
        case .code: .code
        }
    }
}
