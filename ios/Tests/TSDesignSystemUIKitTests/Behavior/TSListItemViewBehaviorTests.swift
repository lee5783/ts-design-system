import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSListItemView behaviour")
struct TSListItemViewBehaviorTests {
    private func layout<View: UIView>(_ view: View, width: CGFloat = 390) -> View {
        let size = view.systemLayoutSizeFitting(
            CGSize(width: width, height: UIView.layoutFittingCompressedSize.height),
            withHorizontalFittingPriority: .required,
            verticalFittingPriority: .fittingSizeLevel
        )
        view.frame = CGRect(origin: .zero, size: size)
        view.layoutIfNeeded()
        return view
    }

    private func makeRow(_ configuration: TSListItemConfiguration) -> TSListItemView {
        layout(TSListItemView(configuration: configuration))
    }

    @Test("A tap on an interactive row sends .primaryActionTriggered once")
    func actionFires() {
        let counter = TapCounter()
        let row = makeRow(TSListItemConfiguration(title: "Inbox", showsChevron: true, isInteractive: true))
        row.addAction(UIAction { _ in counter.tap() }, for: .primaryActionTriggered)
        row.sendActions(for: .touchUpInside)
        #expect(counter.count == 1)
        #expect(row.accessibilityActivate())
        #expect(counter.count == 2)
    }

    @Test("A row without an action, or a disabled row, never fires")
    func blocked() {
        let counter = TapCounter()
        let plain = makeRow(TSListItemConfiguration(title: "Favorites"))
        let disabled = makeRow(TSListItemConfiguration(title: "Inbox", isInteractive: true, isEnabled: false))
        let controlDisabled = makeRow(TSListItemConfiguration(title: "Inbox", isInteractive: true))
        controlDisabled.isEnabled = false
        for row in [plain, disabled, controlDisabled] {
            row.addAction(UIAction { _ in counter.tap() }, for: .primaryActionTriggered)
            row.sendActions(for: .touchUpInside)
            #expect(!row.accessibilityActivate())
        }
        #expect(counter.count == 0)
        #expect(disabled.accessibilityTraits.contains(.notEnabled))
        #expect(controlDisabled.accessibilityTraits.contains(.notEnabled))
        #expect(abs(disabled.itemContentView.alpha - 0.45) < 0.001)
    }

    @Test("Highlighting draws surface-pressed only when the row has an action")
    func pressedOnlyWithAction() {
        let pressable = makeRow(TSListItemConfiguration(title: "Inbox", isInteractive: true))
        pressable.isHighlighted = true
        let traits = pressable.traitCollection
        #expect(pressable.itemContentView.backgroundColor == TSColor.surfacePressed.uiColor.resolvedColor(with: traits))
        pressable.isHighlighted = false
        #expect(pressable.itemContentView.backgroundColor == .clear)
        let plain = makeRow(TSListItemConfiguration(title: "Favorites"))
        plain.isHighlighted = true
        #expect(plain.itemContentView.backgroundColor == .clear)
        let toggle = makeRow(TSListItemConfiguration(title: "Wi-Fi", trailing: .toggle(isOn: true)))
        toggle.isHighlighted = true
        #expect(toggle.itemContentView.backgroundColor == .clear)
    }

    @Test("The row is one element labelled by title and subtitle; a chevron action row has the button trait and the navigates hint")
    func accessibility() {
        let row = makeRow(TSListItemConfiguration(
            title: "Inbox",
            subtitle: "12 unread",
            leading: .avatar(name: "Ada Lovelace"),
            trailing: .badge("New"),
            showsChevron: true,
            isInteractive: true
        ))
        #expect(row.isAccessibilityElement)
        #expect(!row.itemContentView.isAccessibilityElement)
        #expect(row.accessibilityLabel == "Inbox, 12 unread")
        #expect(row.accessibilityValue == "New")
        #expect(row.accessibilityHint == "Opens another screen")
        #expect(row.accessibilityTraits.contains(.button))
        let plain = makeRow(TSListItemConfiguration(title: "Calendar", trailing: .text("Today")))
        #expect(plain.accessibilityValue == "Today")
        #expect(plain.accessibilityHint == nil)
        #expect(!plain.accessibilityTraits.contains(.button))
    }

    @Test(
        "A trailing-switch row exposes switch semantics: On/Off value, toggle trait on 17+ or button on 15–16",
        arguments: [TSCompatPath.native, .fallback], [false, true]
    )
    func switchSemantics(path: TSCompatPath, isOn: Bool) {
        let row = makeRow(TSListItemConfiguration(title: "Wi-Fi", trailing: .toggle(isOn: isOn)))
        row.itemContentView.togglePath = path
        #expect(row.accessibilityLabel == "Wi-Fi")
        #expect(row.accessibilityValue == (isOn ? "On" : "Off"))
        #expect(!row.accessibilityTraits.contains(.selected))
        if path == .native, #available(iOS 17, *) {
            #expect(row.accessibilityTraits.contains(.toggleButton))
        } else {
            #expect(row.accessibilityTraits.contains(.button))
        }
        #expect(!row.itemContentView.switchSlot.switchView.isHidden)
    }

    @Test("Activating a switch row toggles the switch, calls onToggle and sends .valueChanged; disabled blocks")
    func switchActivation() {
        var values: [Bool] = []
        let counter = TapCounter()
        let row = makeRow(TSListItemConfiguration(title: "Wi-Fi", trailing: .toggle(isOn: false), onToggle: { values.append($0) }))
        row.itemContentView.switchSlot.switchView.reduceMotion = { true }
        row.itemContentView.switchSlot.switchView.playHaptic = { _ in }
        row.addAction(UIAction { _ in counter.tap() }, for: .valueChanged)
        #expect(row.accessibilityActivate())
        #expect(values == [true])
        #expect(counter.count == 1)
        #expect(row.configuration.trailing == .toggle(isOn: true))
        #expect(row.accessibilityValue == "On")
        row.isHighlighted = true
        #expect(row.itemContentView.switchSlot.switchView.isOn)
        row.isEnabled = false
        #expect(!row.accessibilityActivate())
        #expect(values == [true])
    }

    @Test("Only touches on the switch reach it; the rest of the row is the control, and the switch hit area spans 44 pt")
    func hitTesting() {
        let row = makeRow(TSListItemConfiguration(title: "Wi-Fi", trailing: .toggle(isOn: false)))
        let switchView = row.itemContentView.switchSlot.switchView
        let center = switchView.convert(CGPoint(x: switchView.bounds.midX, y: switchView.bounds.midY), to: row)
        #expect(row.hitTest(center, with: nil) === switchView)
        #expect(row.hitTest(CGPoint(x: 30, y: row.bounds.midY), with: nil) === row)
        #expect(switchView.bounds.height >= 44)
    }

    @Test("Rows are 48 pt with a title and 68 pt with a subtitle; a switch row is 50 pt and an icon row 60 pt")
    func heights() {
        let samples: [(TSListItemConfiguration, CGFloat)] = [
            (TSListItemConfiguration(title: "Inbox"), 48),
            (TSListItemConfiguration(title: "Inbox", subtitle: "12 unread messages"), 68),
            (TSListItemConfiguration(title: "Inbox", subtitle: "12 unread", trailing: .badge("New"), showsChevron: true, isInteractive: true), 68),
            (TSListItemConfiguration(title: "Wi-Fi", trailing: .toggle(isOn: true)), 50),
            (TSListItemConfiguration(title: "Inbox", leading: .icon(.mail)), 60)
        ]
        for (configuration, expected) in samples {
            let row = makeRow(configuration)
            #expect(abs(row.bounds.height - expected) < 0.5, "\(configuration.title) \(configuration.subtitle ?? "")")
        }
    }

    @Test("At AX5 the row reflows vertically and the title wraps")
    func accessibilityReflow() {
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        let row = TSListItemView(configuration: TSListItemConfiguration(
            title: "Download large files over mobile data",
            leading: .icon(.download),
            trailing: .text("Today"),
            showsChevron: true,
            isInteractive: true
        ))
        row.translatesAutoresizingMaskIntoConstraints = false
        host.content.addSubview(row)
        NSLayoutConstraint.activate([
            row.topAnchor.constraint(equalTo: host.content.topAnchor),
            row.leadingAnchor.constraint(equalTo: host.content.leadingAnchor),
            row.trailingAnchor.constraint(equalTo: host.content.trailingAnchor)
        ])
        host.content.layoutIfNeeded()
        let content = row.itemContentView
        #expect(content.bodyStack.axis == .vertical)
        #expect(content.titleLabel.numberOfLines == 0)
        #expect(content.tileView.frame.maxY <= content.textStack.frame.minY)
        #expect(content.textStack.frame.maxY <= content.trailingLabel.frame.minY)
        #expect(content.titleLabel.bounds.height > TSFont.body.lineHeight(for: .accessibilityExtraExtraExtraLarge) * 1.5)
        #expect(!content.chevronView.isHidden)
        let compact = makeRow(TSListItemConfiguration(title: "Inbox", leading: .icon(.mail)))
        #expect(compact.itemContentView.bodyStack.axis == .horizontal)
        #expect(compact.itemContentView.titleLabel.numberOfLines == 1)
    }

    @Test("The chevron is chevron-right and sits on the leading edge in RTL")
    func chevronMirrors() {
        let row = makeRow(TSListItemConfiguration(title: "Settings", showsChevron: true, isInteractive: true))
        let chevron = row.itemContentView.chevronView
        #expect(chevron.name == .chevronRight)
        let rtl = TSListItemView(configuration: TSListItemConfiguration(title: "Settings", showsChevron: true, isInteractive: true))
        rtl.semanticContentAttribute = .forceRightToLeft
        rtl.itemContentView.semanticContentAttribute = .forceRightToLeft
        rtl.itemContentView.rowStack.semanticContentAttribute = .forceRightToLeft
        _ = layout(rtl)
        let rtlChevron = rtl.itemContentView.chevronView.convert(rtl.itemContentView.chevronView.bounds, to: rtl)
        #expect(rtlChevron.minX < rtl.bounds.midX)
    }

    @Test("A list view separates rows with full-width 1 pt dividers and draws the card chrome")
    func listView() {
        let rows = (0..<3).map { TSListItemView(configuration: TSListItemConfiguration(title: "Row \($0)")) }
        let list = layout(TSListView(items: rows))
        let dividers = list.stackView.arrangedSubviews.compactMap { $0 as? TSListDividerView }
        #expect(dividers.count == 2)
        #expect(dividers.allSatisfy { $0.bounds.height == 1 && $0.bounds.width == list.bounds.width })
        #expect(list.layer.cornerRadius == TSRadius.radiusLg)
        #expect(list.layer.borderWidth == 1)
        #expect(abs(list.bounds.height - (48 * 3 + 2)) < 0.5)
        list.items = [rows[0]]
        #expect(list.stackView.arrangedSubviews.count == 1)
    }
}
