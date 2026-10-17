import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct BadgePage: View {
    @State private var tone: TSBadgeTone = .neutral
    @State private var showsDot = false
    @State private var showsIcon = false

    var body: some View {
        DemoPage(page: .badge) {
            DemoComponentSection(.tones) {
                ForEach(TSBadgeTone.allCases, id: \.self) { tone in
                    DemoComponentRow {
                        TSBadge(BadgeSample.text(tone), tone: tone)
                        DemoCaption(text: tone.rawValue)
                    }
                }
            }
            DemoComponentSection(.withDot) {
                ForEach(TSBadgeTone.allCases, id: \.self) { tone in
                    TSBadge(BadgeSample.text(tone), tone: tone, showsDot: true)
                }
            }
            DemoComponentSection(.withIcons) {
                ForEach(TSBadgeTone.allCases, id: \.self) { tone in
                    TSBadge(BadgeSample.text(tone), tone: tone, icon: BadgeSample.icon(tone))
                }
            }
            DemoComponentSection(.truncation) {
                TSBadge(DemoStrings.string(.sampleNeedsReview), tone: .warning, showsDot: true)
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.tone), selection: $tone) {
                    ForEach(TSBadgeTone.allCases, id: \.self) { Text($0.rawValue).tag($0) }
                }
                .pickerStyle(.menu)
                .accessibilityIdentifier("demo.badge.playground.tone")
                Toggle(DemoStrings.string(.dot), isOn: $showsDot)
                    .accessibilityIdentifier("demo.badge.playground.dot")
                Toggle(DemoStrings.string(.icon), isOn: $showsIcon)
                    .accessibilityIdentifier("demo.badge.playground.icon")
                TSBadge(BadgeSample.text(tone), tone: tone, showsDot: showsDot, icon: showsIcon ? BadgeSample.icon(tone) : nil)
                    .accessibilityIdentifier("demo.badge.playground.badge")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }
}

enum BadgeSample {
    static func text(_ tone: TSBadgeTone) -> String {
        switch tone {
        case .neutral: DemoStrings.string(.sampleDraft)
        case .accent: DemoStrings.string(.sampleNew)
        case .success: DemoStrings.string(.sampleActive)
        case .warning: DemoStrings.string(.samplePending)
        case .danger: DemoStrings.string(.sampleFailed)
        case .info: DemoStrings.string(.sampleBeta)
        }
    }

    static func icon(_ tone: TSBadgeTone) -> TSIconName {
        switch tone {
        case .neutral: .edit
        case .accent: .star
        case .success: .checkCircle
        case .warning: .alert
        case .danger: .xCircle
        case .info: .info
        }
    }
}
