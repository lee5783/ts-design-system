import Testing
import TSDemoSupport

@Suite("DemoCatalog")
struct DemoCatalogTests {
    static let foundations: [DemoPageID] = [.colors, .typography, .spacing, .radius, .size, .opacity, .elevation, .icons]
    static let components: [DemoPageID] = [
        .button, .iconButton, .badge, .avatar, .textField, .checkbox, .switch,
        .tabs, .listItem, .card, .alert, .dialog, .toast, .bottomNav, .spinner, .text
    ]

    @Test("Every page id appears in the catalog exactly once")
    func everyPageOnce() {
        let pages = DemoCatalog.allPages
        #expect(pages.count == Set(pages).count)
        #expect(Set(pages) == Set(DemoPageID.allCases))
    }

    @Test("Each foundation is listed once", arguments: foundations)
    func foundationListedOnce(page: DemoPageID) {
        #expect(DemoCatalog.allPages.filter { $0 == page }.count == 1)
        #expect(page.kind == .foundation)
        #expect(DemoCatalog.section(containing: page)?.id == .foundations)
    }

    @Test("Each component is listed once", arguments: components)
    func componentListedOnce(page: DemoPageID) {
        #expect(DemoCatalog.allPages.filter { $0 == page }.count == 1)
        #expect(page.kind == .component)
        #expect(DemoCatalog.section(containing: page)?.id != .foundations)
    }

    @Test("Foundations come first, in design-system order")
    func foundationsOrder() {
        #expect(DemoCatalog.sections.first?.id == .foundations)
        #expect(DemoCatalog.foundationPages == Self.foundations)
        #expect(Set(DemoCatalog.componentPages) == Set(Self.components))
    }

    @Test("Every section is used once and is not empty")
    func sections() {
        let ids = DemoCatalog.sections.map(\.id)
        #expect(ids == DemoSection.ID.allCases)
        #expect(DemoCatalog.sections.allSatisfy { !$0.pages.isEmpty })
    }

    @Test("Accessibility identifiers are unique and stable")
    func accessibilityIdentifiers() {
        let identifiers = DemoPageID.allCases.map(\.accessibilityIdentifier)
        #expect(Set(identifiers).count == identifiers.count)
        #expect(DemoPageID.textField.accessibilityIdentifier == "demo.page.text-field")
    }
}
