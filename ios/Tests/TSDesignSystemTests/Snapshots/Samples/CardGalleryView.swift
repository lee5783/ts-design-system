import SwiftUI
@testable import TSDesignSystem

enum CardGalleryKind: String, CaseIterable {
    case variants
    case states
}

enum CardSample {
    static let title = "Weekly report"
    static let subtitle = "Updated today"
    static let body = "Revenue is up 12% on last week. Two invoices are still pending review."
}

struct CardGalleryView: View {
    let kind: CardGalleryKind

    var body: some View {
        VStack(alignment: .leading, spacing: TSCardStyleSpec.stackSpacing) {
            switch kind {
            case .variants:
                variants
            case .states:
                states
            }
        }
        .padding(TSSpacing.space4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private var variants: some View {
        TSCard(title: CardSample.title, subtitle: CardSample.subtitle, text: CardSample.body) {
            TSBadge("New", tone: .accent)
        }
        TSCard(title: "Raised", text: CardSample.body, isRaised: true) {
            TSIconButton(.more, label: "More options") {}
        }
        TSCard(
            title: "Invite your team",
            text: "Members can view and edit every project.",
            footerActions: [TSCardAction("Cancel") {}, TSCardAction("Send invite", variant: .primary) {}]
        )
        TSCard {
            Text("A card with only a body.")
        }
    }

    @ViewBuilder
    private var states: some View {
        TSCard(title: "Default", subtitle: "Interactive", text: CardSample.body, action: {})
        TSCard(title: "Pressed", subtitle: "Interactive", text: CardSample.body, action: {}).forcingState(.pressed)
        TSCard(title: "Focused", subtitle: "Interactive", text: CardSample.body, action: {}).forcingState(.focused)
        TSCard(title: "Disabled", subtitle: "Interactive", text: CardSample.body, footerActions: [TSCardAction("Open") {}], action: {})
            .disabled(true)
        TSCard(title: "Raised pressed", text: CardSample.body, isRaised: true, action: {}).forcingState(.pressed)
    }
}
