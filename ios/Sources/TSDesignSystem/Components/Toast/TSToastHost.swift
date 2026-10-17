import SwiftUI

public extension View {
    func tsToastHost(_ queue: TSToastQueue = .shared, bottomInset: CGFloat = 0) -> some View {
        modifier(TSToastHostModifier(queue: queue, bottomInset: bottomInset))
    }
}

struct TSToastHostModifier: ViewModifier {
    let queue: TSToastQueue
    let bottomInset: CGFloat

    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @Environment(\.layoutDirection) private var layoutDirection
    @Environment(\.locale) private var locale

    func body(content: Content) -> some View {
        content.background(
            TSToastWindowBridge(
                queue: queue,
                bottomInset: bottomInset,
                environment: TSPresentationEnvironment(
                    colorScheme: colorScheme,
                    dynamicTypeSize: dynamicTypeSize,
                    layoutDirection: layoutDirection,
                    locale: locale
                )
            )
            .frame(width: 0, height: 0)
            .accessibilityHidden(true)
        )
    }
}

struct TSToastHostView: View {
    @ObservedObject var queue: TSToastQueue
    let bottomInset: CGFloat
    let onToastFrameChange: (CGRect) -> Void

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let margin = TSToastStyleSpec.resolve(.init()).outerMargin
        ZStack(alignment: .bottom) {
            Color.clear
            if let item = queue.current {
                TSToast(item: item, onAction: { queue.performAction() })
                    .background(
                        GeometryReader { proxy in
                            Color.clear.preference(key: TSToastFrameKey.self, value: proxy.frame(in: .global))
                        }
                    )
                    .id(item.id)
                    .transition(TSReduceMotion.transition(.move(edge: .bottom).combined(with: .opacity), reduceMotion: reduceMotion))
            }
        }
        .padding(.horizontal, margin)
        .padding(.bottom, margin + bottomInset)
        .animation(TSMotion.easingStandard.animation(duration: TSMotion.durationBase), value: queue.current?.id)
        .onPreferenceChange(TSToastFrameKey.self, perform: onToastFrameChange)
    }
}

struct TSToastFrameKey: PreferenceKey {
    static let defaultValue: CGRect = .null

    static func reduce(value: inout CGRect, nextValue: () -> CGRect) {
        let next = nextValue()
        if !next.isNull {
            value = next
        }
    }
}
