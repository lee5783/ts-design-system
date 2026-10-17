import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct CardPage: View {
    @State private var stateTapCount = 0
    @State private var lastAction = DemoStrings.string(.none)
    @State private var showsSubtitle = true
    @State private var isRaised = false
    @State private var isInteractive = true
    @State private var showsFooter = false
    @State private var isDisabled = false
    @State private var tapCount = 0

    var body: some View {
        DemoPage(page: .card) {
            DemoComponentSection(.variants) {
                VStack(spacing: TSCardStyleSpec.stackSpacing) {
                    TSCard(
                        title: DemoStrings.string(.sampleWeeklyReport),
                        subtitle: DemoStrings.string(.sampleUpdatedToday),
                        text: DemoStrings.string(.sampleReportBody)
                    ) {
                        TSBadge(DemoStrings.string(.sampleNew), tone: .accent)
                    }
                    TSCard(
                        title: DemoStrings.string(.raised),
                        text: DemoStrings.string(.sampleReportBody),
                        isRaised: true,
                        accessory: .iconButton(icon: .more, label: DemoStrings.string(.sampleMoreOptions)) {}
                    )
                    TSCard(
                        title: DemoStrings.string(.sampleInviteTeam),
                        text: DemoStrings.string(.sampleInviteBody),
                        footerActions: [
                            TSCardAction(DemoStrings.string(.sampleCancel)) {},
                            TSCardAction(DemoStrings.string(.sampleSendInvite), variant: .primary) {}
                        ]
                    )
                }
            }
            DemoComponentSection(.states) {
                VStack(spacing: TSCardStyleSpec.stackSpacing) {
                    TSCard(
                        title: DemoStrings.string(.stateDefault),
                        subtitle: DemoStrings.string(.interactive),
                        text: DemoStrings.string(.sampleReportBody),
                        footerActions: [
                            TSCardAction(DemoStrings.string(.sampleShare)) { lastAction = DemoStrings.string(.sampleShare) },
                            TSCardAction(DemoStrings.string(.sampleOpen), variant: .primary) { lastAction = DemoStrings.string(.sampleOpen) }
                        ],
                        action: { stateTapCount += 1 }
                    )
                    .accessibilityIdentifier("demo.card.state.default")
                    TSCard(
                        title: DemoStrings.string(.stateDisabled),
                        subtitle: DemoStrings.string(.interactive),
                        text: DemoStrings.string(.sampleReportBody),
                        action: { stateTapCount += 1 }
                    )
                    .disabled(true)
                    .accessibilityIdentifier("demo.card.state.disabled")
                }
                DemoTapCounter(count: stateTapCount, identifier: "demo.card.state.count")
                DemoCaption(text: lastAction)
                    .accessibilityIdentifier("demo.card.state.last-action")
            }
            DemoComponentSection(.playground) {
                Toggle(DemoStrings.string(.subtitle), isOn: $showsSubtitle)
                    .accessibilityIdentifier("demo.card.playground.subtitle")
                Toggle(DemoStrings.string(.raised), isOn: $isRaised)
                    .accessibilityIdentifier("demo.card.playground.raised")
                Toggle(DemoStrings.string(.interactive), isOn: $isInteractive)
                    .accessibilityIdentifier("demo.card.playground.interactive")
                Toggle(DemoStrings.string(.footer), isOn: $showsFooter)
                    .accessibilityIdentifier("demo.card.playground.footer")
                Toggle(DemoStrings.string(.stateDisabled), isOn: $isDisabled)
                    .accessibilityIdentifier("demo.card.playground.disabled")
                TSCard(
                    title: DemoStrings.string(.sampleWeeklyReport),
                    subtitle: showsSubtitle ? DemoStrings.string(.sampleUpdatedToday) : nil,
                    text: DemoStrings.string(.sampleReportBody),
                    isRaised: isRaised,
                    footerActions: showsFooter ? [TSCardAction(DemoStrings.string(.sampleOpen), variant: .primary) { tapCount += 1 }] : [],
                    action: isInteractive ? { tapCount += 1 } : nil
                )
                .disabled(isDisabled)
                .accessibilityIdentifier("demo.card.playground.card")
                DemoTapCounter(count: tapCount, identifier: "demo.card.playground.count")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }
}
