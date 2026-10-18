import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct SpinnerPage: View {
    var body: some View {
        DemoPage(page: .spinner) {
            DemoComponentSection(.sizes) {
                DemoComponentRow {
                    ForEach(TSSpinnerSize.allCases, id: \.self) { size in
                        TSSpinner(size: size)
                            .accessibilityIdentifier("demo.spinner.size.\(size.rawValue)")
                    }
                }
            }
            DemoComponentSection(.tones) {
                ForEach(TSSpinnerTone.allCases, id: \.self) { tone in
                    DemoComponentRow {
                        TSSpinner(tone: tone)
                            .accessibilityIdentifier("demo.spinner.tone.\(tone.rawValue)")
                        DemoCaption(text: tone.rawValue)
                    }
                }
            }
            DemoComponentSection(.inContext) {
                HStack(spacing: TSSpacing.space2) {
                    TSSpinner(size: .sm, tone: .secondary)
                    TSText(DemoStrings.string(.sampleLoadingMessages), role: .bodySm, color: .secondary)
                }
                .accessibilityElement(children: .combine)
                .accessibilityIdentifier("demo.spinner.in-context")
                TSSpinner(size: .lg, tone: .accent)
                    .accessibilityIdentifier("demo.spinner.labelled")
            }
        }
    }
}
