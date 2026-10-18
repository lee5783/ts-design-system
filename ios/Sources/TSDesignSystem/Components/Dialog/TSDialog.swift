import SwiftUI
import UIKit

public extension View {
    func tsDialog<Content: View>(
        isPresented: Binding<Bool>,
        title: String,
        variant: TSDialogVariant = .center,
        actions: [TSDialogAction] = [],
        onDismiss: (() -> Void)? = nil,
        onRefused: (() -> Void)? = nil,
        @ViewBuilder content: () -> Content
    ) -> some View {
        modifier(
            TSDialogModifier(
                isPresented: isPresented,
                title: title,
                variant: variant,
                actions: actions,
                onDismiss: onDismiss,
                onRefused: onRefused,
                hasBody: Content.self != EmptyView.self,
                content: content()
            )
        )
    }

    func tsDialog(
        isPresented: Binding<Bool>,
        title: String,
        message: String? = nil,
        variant: TSDialogVariant = .center,
        actions: [TSDialogAction] = [],
        onDismiss: (() -> Void)? = nil,
        onRefused: (() -> Void)? = nil
    ) -> some View {
        modifier(
            TSDialogModifier(
                isPresented: isPresented,
                title: title,
                variant: variant,
                actions: actions,
                onDismiss: onDismiss,
                onRefused: onRefused,
                hasBody: message != nil,
                content: Text(message ?? "")
            )
        )
    }
}

enum TSDialogRoute: String, Hashable, CaseIterable {
    case overFullScreen
    case sheetBridge
    case nativeSheet

    static func resolve(variant: TSDialogVariant, sheetPath: TSCompatPath, reduceMotion: Bool = false) -> TSDialogRoute {
        switch variant {
        case .center:
            .overFullScreen
        case .sheet:
            sheetPath == .native && !reduceMotion ? .nativeSheet : .sheetBridge
        }
    }
}

@MainActor
final class TSDialogSession: ObservableObject {
    @Published private(set) var isGranted = false
    private var hasAppeared = false
    private var afterDismissal: (@MainActor () -> Void)?

    func request(_ isPresented: Bool, presenter: TSDialogPresenter) -> Bool {
        guard isPresented else {
            let grantedButNeverShown = isGranted && !hasAppeared
            isGranted = false
            if presenter.isPending(self) || grantedButNeverShown {
                presenter.end(self)
            }
            return true
        }
        guard !isGranted else { return true }
        switch presenter.request(self, onGranted: { [weak self] in self?.isGranted = true }) {
        case .granted:
            isGranted = true
            return true
        case .queued:
            return true
        case .refused:
            return false
        }
    }

    func withdraw() {
        isGranted = false
    }

    func perform(_ action: TSDialogAction, dismiss: () -> Void) {
        guard isGranted else { return }
        action.handler()
        guard action.dismisses else { return }
        afterDismissal = action.afterDismissal
        withdraw()
        dismiss()
    }

    func didAppear() {
        hasAppeared = true
    }

    func didDismiss(presenter: TSDialogPresenter) {
        guard !isGranted else { return }
        hasAppeared = false
        presenter.end(self)
        let afterDismissal = afterDismissal
        self.afterDismissal = nil
        afterDismissal?()
    }

    func invalidate(presenter: TSDialogPresenter) {
        isGranted = false
        hasAppeared = false
        afterDismissal = nil
        presenter.end(self)
    }
}

struct TSDialogModifier<DialogContent: View>: ViewModifier {
    @Binding var isPresented: Bool
    let title: String
    let variant: TSDialogVariant
    let actions: [TSDialogAction]
    let onDismiss: (() -> Void)?
    var onRefused: (() -> Void)?
    let hasBody: Bool
    let content: DialogContent
    var presenter: TSDialogPresenter = .shared
    var sheetPath: TSCompatPath = TSCompat.sheetPresentation
    var detentPath: TSCompatPath = TSCompat.customSheetDetents

    @StateObject private var session = TSDialogSession()
    @State private var sheetHeight: CGFloat?
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @Environment(\.layoutDirection) private var layoutDirection
    @Environment(\.locale) private var locale

    var route: TSDialogRoute {
        TSDialogRoute.resolve(variant: variant, sheetPath: sheetPath)
    }

    private var resolved: TSDialogStyleSpec.Resolved {
        TSDialogStyleSpec.resolve(.init(variant: variant))
    }

    private var environment: TSPresentationEnvironment {
        TSPresentationEnvironment(colorScheme: colorScheme, dynamicTypeSize: dynamicTypeSize, layoutDirection: layoutDirection, locale: locale)
    }

    private var grantedBinding: Binding<Bool> {
        Binding(
            get: { session.isGranted },
            set: { newValue in
                if !newValue, session.isGranted {
                    close()
                }
            }
        )
    }

    func body(content host: Content) -> some View {
        presenting(host)
            .tsOnChange(of: isPresented, initial: true) { _, newValue in
                requestPresentation(newValue)
            }
            .onDisappear {
                session.invalidate(presenter: presenter)
            }
    }

    @ViewBuilder
    private func presenting(_ host: Content) -> some View {
        switch route {
        case .overFullScreen:
            host.background(
                TSOverFullScreenBridge(
                    isPresented: grantedBinding,
                    environment: environment,
                    onDidDismiss: didDismiss,
                    onRefused: refuseLater,
                    content: AnyView(TSDialogCenterContainer(resolved: resolved, onScrimTap: close, panel: panel()).onAppear(perform: session.didAppear))
                )
            )
        case .sheetBridge:
            host.background(
                TSSheetBridge(
                    isPresented: grantedBinding,
                    environment: environment,
                    cornerRadius: resolved.cornerRadius,
                    background: resolved.fill.uiColor,
                    detentPath: detentPath,
                    onDidDismiss: didDismiss,
                    onRefused: refuseLater,
                    fitHeight: measure,
                    content: AnyView(sheetRoot)
                )
            )
        case .nativeSheet:
            host.sheet(isPresented: grantedBinding, onDismiss: didDismiss) {
                sheetRoot
                    .tsPresentationEnvironment(environment)
                    .tsSheetPresentation(
                        fitHeight: sheetHeight,
                        cornerRadius: resolved.cornerRadius,
                        background: resolved.fill.color,
                        path: sheetPath
                    )
            }
        }
    }

    private var sheetRoot: some View {
        TSDialogSheetContainer(onHeightChange: { sheetHeight = $0 }, panel: panel())
            .onAppear(perform: session.didAppear)
    }

    func panel() -> TSDialogPanel<DialogContent> {
        TSDialogPanel(
            title: title,
            variant: variant,
            actions: actions,
            hasBody: hasBody,
            onAction: perform,
            onClose: close,
            content: content
        )
    }

    private func measure(width: CGFloat) -> CGFloat {
        let host = UIHostingController(rootView: panel().tsPresentationEnvironment(environment))
        return host.sizeThatFits(in: CGSize(width: width, height: .greatestFiniteMagnitude)).height
    }

    func requestPresentation(_ newValue: Bool) {
        if !session.request(newValue, presenter: presenter) {
            isPresented = false
            reportRefusal()
        }
    }

    func refuse() {
        session.invalidate(presenter: presenter)
        isPresented = false
        reportRefusal()
    }

    private func refuseLater() {
        DispatchQueue.main.async(execute: refuse)
    }

    private func reportRefusal() {
        onRefused?()
    }

    func close() {
        session.withdraw()
        isPresented = false
        onDismiss?()
    }

    func perform(_ action: TSDialogAction) {
        session.perform(action) { isPresented = false }
    }

    private func didDismiss() {
        session.didDismiss(presenter: presenter)
    }
}
