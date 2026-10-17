import Foundation

public struct DemoSection: Hashable, Sendable, Identifiable {
    public enum ID: String, CaseIterable, Hashable, Sendable {
        case foundations
        case actions
        case forms
        case navigation
        case content
        case feedback
    }

    public let id: ID
    public let pages: [DemoPageID]

    public init(id: ID, pages: [DemoPageID]) {
        self.id = id
        self.pages = pages
    }

    public var title: String {
        DemoStrings.sectionTitle(id)
    }
}
