import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSCardView behaviour")
struct TSCardViewBehaviorTests {
    private func layout<View: UIView>(_ view: View, width: CGFloat = 358) -> View {
        let size = view.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .required,
            verticalFittingPriority: .fittingSizeLevel
        )
        view.frame = CGRect(origin: .zero, size: size)
        view.layoutIfNeeded()
        return view
    }

    @Test("An interactive card exposes its content and owns its hits")
    func interactiveExposesContent() {
        let card = layout(TSCardView(title: "Weekly report", subtitle: "Updated today", bodyText: "Revenue is up 12%.", isInteractive: true))
        #expect(!card.isAccessibilityElement)
        #expect(card.hitTest(CGPoint(x: 40, y: 30), with: nil) === card)
        #expect(card.hitTest(CGPoint(x: 40, y: card.bounds.height - 20), with: nil) === card)
    }

    @Test("A typed accessory renders an icon button that stays hittable")
    func typedAccessory() throws {
        var fired = 0
        let card = layout(TSCardView(
            title: "Weekly report",
            bodyText: "Revenue is up 12%.",
            isInteractive: true,
            accessory: .iconButton(icon: .more, label: "More options") { fired += 1 }
        ))
        let button = try #require(card.accessoryView as? TSIconButtonView)
        let center = button.convert(CGPoint(x: button.bounds.midX, y: button.bounds.midY), to: card)
        #expect(card.hitTest(center, with: nil).map { $0.isDescendant(of: button) } == true)
        button.sendActions(for: .primaryActionTriggered)
        #expect(fired == 1)
    }

    @Test("A tap sends .primaryActionTriggered on an interactive card")
    func actionFires() {
        let counter = TapCounter()
        let card = layout(TSCardView(title: "Weekly report", bodyText: "Body", isInteractive: true))
        card.addAction(UIAction { _ in counter.tap() }, for: .primaryActionTriggered)
        card.sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
    }

    @Test("Footer buttons stay hittable inside an interactive card and fire their own handlers")
    func footerButtons() throws {
        var fired: [String] = []
        let card = layout(TSCardView(
            title: "Invite",
            bodyText: "Invite your team.",
            isInteractive: true,
            footerActions: [TSCardAction("Cancel") { fired.append("Cancel") }, TSCardAction("Send", variant: .primary) { fired.append("Send") }]
        ))
        #expect(card.footerButtons.map(\.title) == ["Cancel", "Send"])
        let send = try #require(card.footerButtons.last)
        let center = send.convert(CGPoint(x: send.bounds.midX, y: send.bounds.midY), to: card)
        #expect(card.hitTest(center, with: nil)?.isDescendant(of: send) == true)
        send.sendActions(for: .primaryActionTriggered)
        #expect(fired == ["Send"])
        #expect(send.frame.maxX <= card.footerView.bounds.width - 16 + 0.5)
    }

    @Test("A static card is a container: not an element and sends no action")
    func staticCard() {
        let counter = TapCounter()
        let card = layout(TSCardView(title: "Weekly report", subtitle: "Updated today", bodyText: "Revenue is up 12%."))
        card.addAction(UIAction { _ in counter.tap() }, for: .primaryActionTriggered)
        card.sendActions(for: .touchUpInside)
        #expect(counter.count == 0)
        #expect(!card.isAccessibilityElement)
    }

    @Test("Highlighting an interactive card draws surface-pressed; a static card keeps surface-card")
    func pressedTreatment() {
        let card = layout(TSCardView(title: "Weekly report", bodyText: "Body", isInteractive: true))
        let traits = card.traitCollection
        card.isHighlighted = true
        #expect(card.surfaceView.backgroundColor == TSColor.surfacePressed.uiColor.resolvedColor(with: traits))
        card.isHighlighted = false
        #expect(card.surfaceView.backgroundColor == TSColor.surfaceCard.uiColor.resolvedColor(with: traits))
        let plain = layout(TSCardView(title: "Static", bodyText: "Body"))
        plain.isHighlighted = true
        #expect(plain.surfaceView.backgroundColor == TSColor.surfaceCard.uiColor.resolvedColor(with: traits))
    }

    @Test("A disabled interactive card never fires and disables its footer buttons")
    func disabledBlocks() {
        let counter = TapCounter()
        let card = layout(TSCardView(title: "Weekly report", bodyText: "Body", isInteractive: true, footerActions: [TSCardAction("Share") {}]))
        card.isEnabled = false
        card.addAction(UIAction { _ in counter.tap() }, for: .primaryActionTriggered)
        card.sendActions(for: .touchUpInside)
        #expect(counter.count == 0)
        #expect(card.footerButtons.allSatisfy { !$0.isEnabled })
        #expect(abs(card.surfaceView.alpha - 0.45) < 0.001)
    }

    @Test("A disabled card owns the dimming: footer and accessory buttons render at 0.45, not 0.45 twice")
    func containerOwnsDimming() throws {
        let card = layout(TSCardView(
            title: "Weekly report",
            bodyText: "Body",
            footerActions: [TSCardAction("Share") {}],
            accessory: .iconButton(icon: .more, label: "More options") {}
        ))
        card.isEnabled = false
        card.layoutIfNeeded()
        let accessory = try #require(card.accessoryView as? TSIconButtonView)
        let controls: [UIButton] = card.footerButtons + [accessory]
        for control in controls {
            control.updateConfiguration()
            #expect(!control.isEnabled)
            #expect(abs(effectiveAlpha(of: control, in: card) - TSOpacity.opacityDisabled) < 0.001)
        }
        card.isEnabled = true
        for control in controls {
            control.updateConfiguration()
            #expect(abs(effectiveAlpha(of: control, in: card) - 1) < 0.001)
        }
    }

    private func effectiveAlpha(of view: UIView, in container: UIView) -> CGFloat {
        sequence(first: view, next: \.superview)
            .prefix { $0 !== container }
            .reduce(container.alpha) { $0 * $1.alpha }
    }

    @Test("Raised adds the shadow-sm layer; the surface clips while the card does not")
    func raised() {
        let flat = layout(TSCardView(title: "Flat", bodyText: "Body"))
        #expect(flat.layer.shadowOpacity == 0)
        let raised = layout(TSCardView(title: "Raised", bodyText: "Body", isRaised: true))
        #expect(raised.layer.shadowOpacity > 0)
        #expect(!raised.clipsToBounds)
        #expect(raised.surfaceView.clipsToBounds)
        #expect(raised.surfaceView.layer.borderWidth == 1)
        #expect(raised.surfaceView.layer.cornerRadius == 12)
    }

    @Test("Header, body and footer stack with the DS paddings")
    func heights() {
        let titleOnly = layout(TSCardView(title: "Weekly report"))
        #expect(abs(titleOnly.bounds.height - (16 + 24 + 16)) < 1)
        let withBody = layout(TSCardView(title: "Weekly report", bodyText: "Revenue is up."))
        #expect(abs(withBody.bounds.height - (16 + 24 + 16 + 20 + 16)) < 1)
        let withFooter = layout(TSCardView(title: "Weekly report", bodyText: "Revenue is up.", footerActions: [TSCardAction("Open") {}]))
        #expect(abs(withFooter.bounds.height - (withBody.bounds.height + 1 + 12 + 44 + 12)) < 1)
    }

    @Test("An accessory view sits at the trailing edge of the header and stays hittable")
    func accessory() throws {
        let counter = TapCounter()
        let accessory = TSIconButtonView(icon: .more, label: "More options", primaryAction: UIAction { _ in counter.tap() })
        let card = TSCardView(title: "Weekly report", bodyText: "Body", isInteractive: true)
        card.accessoryView = accessory
        _ = layout(card)
        #expect(accessory.superview === card.headerStack)
        #expect(accessory.frame.maxX <= card.headerStack.bounds.width - 16 + 0.5)
        let center = accessory.convert(CGPoint(x: accessory.bounds.midX, y: accessory.bounds.midY), to: card)
        #expect(card.hitTest(center, with: nil)?.isDescendant(of: accessory) == true)
    }

    @Test("An accessory replaced while the card is disabled dims itself again")
    func replacedAccessoryOwnsItsDimmingAgain() {
        let card = TSCardView(title: "Weekly report", bodyText: "Body")
        let button = TSButtonView(title: "Share")
        card.accessoryView = button
        card.isEnabled = false
        #expect(button.isDimmedByContainer)
        let iconButton = TSIconButtonView(icon: .more, label: "More options")
        card.accessoryView = iconButton
        #expect(!button.isDimmedByContainer)
        #expect(iconButton.isDimmedByContainer)
        card.accessoryView = nil
        #expect(!iconButton.isDimmedByContainer)
    }

    @Test("Setting accessoryView after a typed accessory clears it; setting accessory again restores it")
    func accessoryLastWriterWins() throws {
        let card = layout(TSCardView(
            title: "Weekly report",
            bodyText: "Body",
            isInteractive: true,
            accessory: .iconButton(icon: .more, label: "Card options") {}
        ))
        let typed = try #require(card.accessoryView as? TSIconButtonView)

        let share = TSButtonView(title: "Share")
        card.accessoryView = share
        #expect(card.accessory == nil)
        #expect(card.accessoryView === share)
        #expect(typed.superview == nil)

        card.accessory = .iconButton(icon: .more, label: "Card options") {}
        #expect(card.accessory != nil)
        let restored = try #require(card.accessoryView as? TSIconButtonView)
        #expect(restored.superview === card.headerStack)
        #expect(share.superview == nil)

        card.accessory = nil
        #expect(card.accessoryView == nil)
    }

    @Test("Replacing a typed accessory with a view while disabled hands the dimming back to the old button")
    func typedAccessoryReplacedWhileDisabled() throws {
        let card = TSCardView(title: "Weekly report", bodyText: "Body", accessory: .iconButton(icon: .more, label: "Card options") {})
        card.isEnabled = false
        let typed = try #require(card.accessoryView as? TSIconButtonView)
        #expect(typed.isDimmedByContainer)
        let share = TSButtonView(title: "Share")
        card.accessoryView = share
        #expect(!typed.isDimmedByContainer)
        #expect(share.isDimmedByContainer)
        card.accessory = .iconButton(icon: .more, label: "Card options") {}
        #expect(!share.isDimmedByContainer)
        #expect((card.accessoryView as? TSIconButtonView)?.isDimmedByContainer == true)
    }

    @Test("At AX5 footer actions stack vertically")
    func accessibilityReflow() {
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        let card = TSCardView(title: "Weekly report", bodyText: "Body", footerActions: [TSCardAction("Cancel") {}, TSCardAction("Open") {}])
        host.content.addSubview(card)
        card.traitCollectionDidChange(nil)
        #expect(card.footerStack.axis == .vertical)
        let regular = TSCardView(title: "Weekly report", bodyText: "Body", footerActions: [TSCardAction("Cancel") {}, TSCardAction("Open") {}])
        #expect(regular.footerStack.axis == .horizontal)
    }
}
