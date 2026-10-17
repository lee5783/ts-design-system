import TSDesignSystemUIKit
import UIKit

enum CardGalleryUIKind: String, CaseIterable {
    case variants
    case states
}

final class CardGalleryUIView: GalleryStackView {
    private static let title = "Weekly report"
    private static let subtitle = "Updated today"
    private static let body = "Revenue is up 12% on last week. Two invoices are still pending review."

    init(kind: CardGalleryUIKind) {
        super.init(frame: .zero)
        stack.alignment = .fill
        stack.spacing = TSCardStyleSpec.stackSpacing
        let cards = switch kind {
        case .variants: Self.variants()
        case .states: Self.states()
        }
        cards.forEach(stack.addArrangedSubview)
    }

    private static func variants() -> [UIView] {
        let accessory = TSCardView(title: title, subtitle: subtitle, bodyText: body)
        accessory.accessoryView = TSBadgeView(text: "New", tone: .accent)
        let raised = TSCardView(title: "Raised", bodyText: body, isRaised: true)
        raised.accessoryView = TSIconButtonView(icon: .more, label: "More options")
        let footer = TSCardView(
            title: "Invite your team",
            bodyText: "Members can view and edit every project.",
            footerActions: [TSCardAction("Cancel") {}, TSCardAction("Send invite", variant: .primary) {}]
        )
        let bodyOnly = TSCardView(bodyText: "A card with only a body.")
        return [accessory, raised, footer, bodyOnly]
    }

    private static func states() -> [UIView] {
        let pressed = TSCardView(title: "Pressed", subtitle: "Interactive", bodyText: body, isInteractive: true)
        pressed.isHighlighted = true
        let disabled = TSCardView(title: "Disabled", subtitle: "Interactive", bodyText: body, isInteractive: true, footerActions: [TSCardAction("Open") {}])
        disabled.isEnabled = false
        let raisedPressed = TSCardView(title: "Raised pressed", bodyText: body, isRaised: true, isInteractive: true)
        raisedPressed.isHighlighted = true
        return [
            TSCardView(title: "Default", subtitle: "Interactive", bodyText: body, isInteractive: true),
            pressed,
            disabled,
            raisedPressed
        ]
    }
}
