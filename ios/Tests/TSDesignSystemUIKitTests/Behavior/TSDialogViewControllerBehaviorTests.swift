import Testing
import UIKit
@testable import TSDesignSystemUIKit

@MainActor
@Suite("TSDialogViewController behaviour")
struct TSDialogViewControllerBehaviorTests {
    private final class Owner {}

    @MainActor
    private final class WeakBox {
        weak var value: TSDialogViewController?
    }

    private let dialogOwner = WeakBox()

    @MainActor
    private final class Host {
        let window = UIWindow(frame: CGRect(x: 0, y: 0, width: 390, height: 844))
        let root = UIViewController()

        init() {
            window.rootViewController = root
            window.isHidden = false
        }
    }

    private func makeDialog(
        variant: TSDialogVariant = .center,
        counter: TapCounter = TapCounter(),
        presenter: TSDialogPresenter = TSDialogPresenter()
    ) -> TSDialogViewController {
        let dialog = TSDialogViewController(
            title: "Delete this project?",
            message: "This cannot be undone.",
            variant: variant,
            actions: [TSDialogAction("Cancel"), TSDialogAction("Delete", variant: .danger)],
            onDismiss: { counter.tap() }
        )
        dialog.presenter = presenter
        return dialog
    }

    @Test("A center dialog presents over full screen with a cross-dissolve, over the overlay scrim")
    func centerPresentation() {
        let dialog = makeDialog()
        dialog.loadViewIfNeeded()
        #expect(dialog.modalPresentationStyle == .overFullScreen)
        #expect(dialog.modalTransitionStyle == .crossDissolve)
        #expect(dialog.view.backgroundColor == TSColor.overlay.uiColor.resolvedColor(with: dialog.traitCollection))
    }

    @Test("The dialog's view is modal and shows its title and message", arguments: TSDialogVariant.allCases)
    func modal(variant: TSDialogVariant) {
        let dialog = makeDialog(variant: variant)
        dialog.loadViewIfNeeded()
        #expect(dialog.view.accessibilityViewIsModal)
        #expect(dialog.panelView.titleLabel.attributedText?.string == "Delete this project?")
        #expect(dialog.panelView.messageLabel.attributedText?.string == "This cannot be undone.")
    }

    @Test("A sheet's detents follow the OS: fit + large on iOS 16+, medium + large on iOS 15")
    func sheetDetents() throws {
        let dialog = makeDialog(variant: .sheet)
        let sheet = try #require(dialog.sheetPresentationController)
        #expect(dialog.modalPresentationStyle == .pageSheet)
        #expect(sheet.preferredCornerRadius == 16)
        #expect(sheet.prefersGrabberVisible)
        if #available(iOS 16, *) {
            #expect(detentIdentifiers(sheet) == [TSSheetCompat.identifier(for: .fit), .large])
        }
        dialog.configurePresentation(detentPath: .fallback)
        #expect(detentIdentifiers(sheet) == [.medium, .large])
        #expect(sheet.selectedDetentIdentifier == .medium)
        dialog.configurePresentation(detentPath: .native)
        if #available(iOS 16, *) {
            #expect(detentIdentifiers(sheet) == [TSSheetCompat.identifier(for: .fit), .large])
        }
    }

    @Test("The fit detent is the panel's height: 32 top, title, body, stacked actions and 24 bottom")
    func fitHeight() {
        let dialog = makeDialog(variant: .sheet)
        dialog.loadViewIfNeeded()
        dialog.view.frame = CGRect(x: 0, y: 0, width: 390, height: 844)
        #expect(abs(dialog.fitHeight() - (32 + 28 + 8 + 24 + 24 + 44 + 8 + 44 + 24)) < 1)
    }

    @Test("Center actions keep primary last; sheet and accessibility sizes stack them with primary first")
    func actionOrder() {
        let center = makeDialog(variant: .center)
        #expect(center.panelView.actionButtons.map(\.title) == ["Cancel", "Delete"])
        #expect(center.panelView.actionsStack.axis == .horizontal)
        let sheet = makeDialog(variant: .sheet)
        #expect(sheet.panelView.actionButtons.map(\.title) == ["Delete", "Cancel"])
        #expect(sheet.panelView.actionsStack.axis == .vertical)
        #expect(sheet.panelView.actionButtons.filter { !$0.isBlock }.isEmpty)
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        let large = makeDialog(variant: .center)
        large.panelView.translatesAutoresizingMaskIntoConstraints = false
        host.content.addSubview(large.panelView)
        #expect(large.panelView.actionButtons.map(\.title) == ["Delete", "Cancel"])
        #expect(large.panelView.actionsStack.axis == .vertical)
    }

    @Test("A second dialog is refused while one is presented")
    func refusesSecond() {
        let presenter = TSDialogPresenter()
        let host = Host()
        let first = makeDialog(presenter: presenter)
        let second = makeDialog(presenter: presenter)
        #expect(first.present(from: host.root, animated: false))
        #expect(presenter.isActive(first))
        #expect(first.presentingViewController === host.root)
        #expect(!second.present(from: host.root, animated: false))
        #expect(second.presentingViewController == nil)
        #expect(presenter.refusedCount == 1)
    }

    @Test("Presenting from a source that is already presenting returns false, frees the presenter and a later dialog still presents")
    func refusedByUIKit() {
        let presenter = TSDialogPresenter()
        let host = Host()
        host.root.present(UIViewController(), animated: false)
        let dialog = makeDialog(presenter: presenter)
        #expect(!dialog.present(from: host.root, animated: false))
        #expect(dialog.presentingViewController == nil)
        #expect(!presenter.isPresenting)
        #expect(presenter.refusedCount == 0)
        let other = Host()
        let later = makeDialog(presenter: presenter)
        #expect(later.present(from: other.root, animated: false))
        #expect(presenter.isActive(later))
    }

    @Test("Presenting a dialog that is already on screen returns false and keeps its claim")
    func presentingTwiceReturnsFalse() {
        let presenter = TSDialogPresenter()
        let host = Host()
        let dialog = makeDialog(presenter: presenter)
        #expect(dialog.present(from: host.root, animated: false))
        #expect(!dialog.present(from: host.root, animated: false))
        #expect(dialog.presentingViewController === host.root)
        #expect(presenter.isActive(dialog))
        #expect(presenter.refusedCount == 0)
    }

    @Test("Presenting from a source outside a window returns false and leaves the presenter free")
    func refusedOffWindow() {
        let presenter = TSDialogPresenter()
        let dialog = makeDialog(presenter: presenter)
        #expect(!dialog.present(from: UIViewController(), animated: false))
        #expect(!presenter.isPresenting)
    }

    @Test("A presented dialog that is released frees the shared presenter")
    func releasedDialogFreesPresenter() {
        let presenter = TSDialogPresenter()
        autoreleasepool {
            let dialog = makeDialog(presenter: presenter)
            presenter.begin(dialog)
            #expect(presenter.isPresenting)
        }
        #expect(!presenter.isPresenting)
        #expect(presenter.begin(Owner()))
    }

    @Test("Appearing after present(from:) does not count a refusal")
    func appearingDoesNotCountRefusal() {
        let presenter = TSDialogPresenter()
        let host = Host()
        let dialog = makeDialog(presenter: presenter)
        #expect(dialog.present(from: host.root, animated: false))
        dialog.beginAppearanceTransition(true, animated: false)
        dialog.endAppearanceTransition()
        #expect(presenter.refusedCount == 0)
        #expect(presenter.isActive(dialog))
    }

    @Test("The hardware Escape key closes the dialog, calls onDismiss and frees the presenter")
    func escapeKey() throws {
        let presenter = TSDialogPresenter()
        let counter = TapCounter()
        let dialog = makeDialog(counter: counter, presenter: presenter)
        let command = try #require(dialog.keyCommands?.first)
        #expect(command.input == UIKeyCommand.inputEscape)
        #expect(command.modifierFlags.isEmpty)
        #expect(command.wantsPriorityOverSystemBehavior)
        #expect(dialog.canBecomeFirstResponder)
        presenter.begin(dialog)
        let action = try #require(command.action)
        _ = dialog.perform(action)
        #expect(counter.count == 1)
        #expect(!presenter.isPresenting)
    }

    @Test("A dismissing action's handler runs while the dialog still holds the presenter; a follow-up it presents is shown once the dialog has dismissed")
    func chainedDialog() {
        let presenter = TSDialogPresenter()
        let host = Host()
        var followUpPresented = false
        var heldInHandler = false
        let followUp = makeDialog(presenter: presenter)
        let dialog = TSDialogViewController(
            title: "Delete this project?",
            actions: [
                TSDialogAction("Delete", variant: .danger) {
                    heldInHandler = presenter.isActive(dialogOwner.value!)
                    followUpPresented = followUp.present(from: host.root, animated: false)
                }
            ]
        )
        dialogOwner.value = dialog
        dialog.presenter = presenter
        presenter.begin(dialog)
        dialog.panelView.actionButtons[0].sendActions(for: .primaryActionTriggered)
        #expect(heldInHandler)
        #expect(followUpPresented)
        #expect(presenter.isActive(followUp))
        #expect(followUp.presentingViewController === host.root)
        #expect(presenter.refusedCount == 0)
    }

    @Test("A follow-up presented from the handler of an on-screen dialog waits for that dialog's dismissal instead of stacking or being refused")
    func chainedDialogWaitsForTheDismissal() {
        let presenter = TSDialogPresenter()
        let host = Host()
        var followUpPresented = false
        let followUp = makeDialog(presenter: presenter)
        let dialog = TSDialogViewController(
            title: "Delete this project?",
            actions: [TSDialogAction("Delete", variant: .danger) { followUpPresented = followUp.present(from: host.root, animated: false) }]
        )
        dialog.presenter = presenter
        #expect(dialog.present(from: host.root, animated: false))
        dialog.panelView.actionButtons[0].sendActions(for: .primaryActionTriggered)
        #expect(followUpPresented)
        #expect(followUp.presentingViewController == nil)
        #expect(!presenter.isActive(followUp))
        #expect(presenter.refusedCount == 0)
    }

    @Test("A chained dialog takes the slot ahead of a queued request; a deferred presentation that UIKit refuses still calls its completion and frees the presenter")
    func chainedHandOverAndFailure() {
        let presenter = TSDialogPresenter()
        let host = Host()
        let queued = Owner()
        var granted = 0
        var completed = false
        let followUp = makeDialog(presenter: presenter)
        let dialog = TSDialogViewController(
            title: "Delete this project?",
            actions: [TSDialogAction("Delete", variant: .danger) { _ = followUp.present(from: host.root, animated: false) { completed = true } }]
        )
        dialog.presenter = presenter
        presenter.begin(dialog)
        presenter.request(queued) { granted += 1 }
        host.root.present(UIViewController(), animated: false)
        dialog.panelView.actionButtons[0].sendActions(for: .primaryActionTriggered)
        #expect(completed)
        #expect(followUp.presentingViewController == nil)
        #expect(presenter.isActive(queued))
        #expect(granted == 1)
    }

    @Test("A dialog presented from another window inside a handler is not deferred and is refused")
    func otherWindowIsNotChained() {
        let presenter = TSDialogPresenter()
        let host = Host()
        let other = Host()
        var presentedFromOtherWindow: Bool?
        let followUp = makeDialog(presenter: presenter)
        let dialog = TSDialogViewController(
            title: "Delete this project?",
            actions: [TSDialogAction("Delete", variant: .danger) { presentedFromOtherWindow = followUp.present(from: other.root, animated: false) }]
        )
        dialog.presenter = presenter
        #expect(dialog.present(from: host.root, animated: false))
        dialog.panelView.actionButtons[0].sendActions(for: .primaryActionTriggered)
        #expect(presentedFromOtherWindow == false)
        #expect(presenter.refusedCount == 1)
    }

    @Test("An action's after-dismissal step runs once the dialog has released the presenter, so it can present a system alert from the source")
    func afterDismissalPresentsAnAlert() {
        let presenter = TSDialogPresenter()
        let host = Host()
        var order: [String] = []
        let dialog = TSDialogViewController(
            title: "Share this project?",
            actions: [
                TSDialogAction("Share", variant: .primary, handler: { order.append(presenter.isPresenting ? "handler" : "handler-without-claim") }, afterDismissal: {
                    order.append(presenter.isPresenting ? "after-while-held" : "after")
                    host.root.present(UIAlertController(title: "Shared", message: nil, preferredStyle: .alert), animated: false)
                })
            ]
        )
        dialog.presenter = presenter
        presenter.begin(dialog)
        dialog.panelView.actionButtons[0].sendActions(for: .primaryActionTriggered)
        #expect(order == ["handler", "after"])
        #expect(host.root.presentedViewController is UIAlertController)
    }

    @Test("Outside an action handler a second dialog is still refused while one is on screen")
    func refusedOutsideAHandler() {
        let presenter = TSDialogPresenter()
        let host = Host()
        let dialog = TSDialogViewController(title: "One", actions: [TSDialogAction("Stay", dismisses: false)])
        dialog.presenter = presenter
        #expect(dialog.present(from: host.root, animated: false))
        dialog.panelView.actionButtons[0].sendActions(for: .primaryActionTriggered)
        #expect(!makeDialog(presenter: presenter).present(from: host.root, animated: false))
        #expect(presenter.refusedCount == 1)
    }

    @Test("A scrim tap closes the dialog and calls onDismiss; the scrim recognizer filters panel touches")
    func scrimDismisses() {
        let presenter = TSDialogPresenter()
        let counter = TapCounter()
        let dialog = makeDialog(counter: counter, presenter: presenter)
        dialog.loadViewIfNeeded()
        let scrim = dialog.view.gestureRecognizers?.first { $0 is UITapGestureRecognizer }
        #expect(scrim?.delegate === dialog)
        presenter.begin(dialog)
        dialog.scrimTapped()
        #expect(counter.count == 1)
        #expect(!presenter.isPresenting)
    }

    @Test("A sheet has no scrim recognizer of its own; the system dimming view dismisses it")
    func sheetHasNoScrimRecognizer() {
        let dialog = makeDialog(variant: .sheet)
        dialog.loadViewIfNeeded()
        #expect(dialog.view.gestureRecognizers?.contains { $0 is UITapGestureRecognizer } != true)
        #expect(dialog.view.backgroundColor == TSColor.surfaceElevated.uiColor.resolvedColor(with: dialog.traitCollection))
    }

    @Test("A dismissing action runs its handler and closes without onDismiss; a non-dismissing one stays open")
    func actions() {
        let presenter = TSDialogPresenter()
        let handled = TapCounter()
        let dismissed = TapCounter()
        let dialog = TSDialogViewController(
            title: "One dialog at a time",
            actions: [
                TSDialogAction("Stay", dismisses: false) { handled.tap() },
                TSDialogAction("Done", variant: .primary) { handled.tap() }
            ],
            onDismiss: { dismissed.tap() }
        )
        dialog.presenter = presenter
        presenter.begin(dialog)
        dialog.panelView.actionButtons[0].sendActions(for: .primaryActionTriggered)
        #expect(handled.count == 1)
        #expect(presenter.isActive(dialog))
        dialog.panelView.actionButtons[1].sendActions(for: .primaryActionTriggered)
        #expect(handled.count == 2)
        #expect(!presenter.isPresenting)
        #expect(dismissed.count == 0)
    }

    @Test("A drag-to-dismiss calls onDismiss and frees the presenter")
    func dragDismiss() throws {
        let presenter = TSDialogPresenter()
        let counter = TapCounter()
        let dialog = makeDialog(variant: .sheet, counter: counter, presenter: presenter)
        presenter.begin(dialog)
        dialog.presentationControllerDidDismiss(try #require(dialog.presentationController))
        #expect(counter.count == 1)
        #expect(!presenter.isPresenting)
    }

    @Test("Presenting inherits the source's text size and layout direction")
    func inheritsTraits() throws {
        let host = TraitHost(sizeCategory: .accessibilityExtraExtraExtraLarge)
        let dialog = makeDialog()
        #expect(dialog.present(from: host.child, animated: false))
        let traits = try #require(dialog.presentationController?.overrideTraitCollection)
        #expect(traits.preferredContentSizeCategory == .accessibilityExtraExtraExtraLarge)
    }

    @Test("A dialog does not pin the appearance, text size or direction it inherits from its window, so it follows a live change; a source's own override is kept", arguments: TSDialogVariant.allCases)
    func followsWindowAppearance(variant: TSDialogVariant) {
        let host = Host()
        host.window.overrideUserInterfaceStyle = .light
        let dialog = makeDialog(variant: variant)
        #expect(dialog.present(from: host.root, animated: false))
        #expect(dialog.overrideUserInterfaceStyle == .unspecified)
        #expect(dialog.presentationController?.overrideTraitCollection == nil)
        dialog.dismiss(animated: false)

        let pinned = Host()
        pinned.window.overrideUserInterfaceStyle = .light
        pinned.root.overrideUserInterfaceStyle = .dark
        let pinnedDialog = makeDialog(variant: variant)
        #expect(pinnedDialog.present(from: pinned.root, animated: false))
        #expect(pinnedDialog.overrideUserInterfaceStyle == .dark)
    }

    @Test("A presenter that forces the appearance its window already has keeps the dialog in it after the window changes", arguments: TSDialogVariant.allCases)
    func keepsAnOverrideThatEqualsTheWindow(variant: TSDialogVariant) {
        let host = Host()
        host.window.overrideUserInterfaceStyle = .dark
        host.root.overrideUserInterfaceStyle = .dark
        let dialog = makeDialog(variant: variant)
        #expect(dialog.present(from: host.root, animated: false))
        #expect(dialog.overrideUserInterfaceStyle == .dark)
        host.window.overrideUserInterfaceStyle = .light
        #expect(dialog.traitCollection.userInterfaceStyle == .dark)
    }

    @Test("A container's text size override that equals the window's is still kept")
    func keepsATextSizeOverrideThatEqualsTheWindow() throws {
        let category = Host().window.traitCollection.preferredContentSizeCategory
        try #require(category != .unspecified)
        let host = TraitHost(sizeCategory: category)
        let dialog = makeDialog()
        #expect(dialog.present(from: host.child, animated: false))
        let traits = try #require(dialog.presentationController?.overrideTraitCollection)
        #expect(traits.preferredContentSizeCategory == category)
        #expect(traits.layoutDirection == .unspecified)
    }

    @Test("An appearance forced on a parent container is kept, even when it equals the window's")
    func keepsAnOverrideFromTheParentChain() {
        let host = Host()
        host.window.overrideUserInterfaceStyle = .dark
        host.root.overrideUserInterfaceStyle = .dark
        let child = UIViewController()
        host.root.addChild(child)
        host.root.view.addSubview(child.view)
        child.didMove(toParent: host.root)
        host.window.layoutIfNeeded()
        #expect(child.traitCollection.userInterfaceStyle == .dark)
        let dialog = makeDialog()
        #expect(dialog.present(from: child, animated: false))
        #expect(dialog.overrideUserInterfaceStyle == .dark)
    }

    @Test("With animations off the sheet's slide becomes a fade")
    func reduceMotion() {
        let host = Host()
        let dialog = makeDialog(variant: .sheet)
        dialog.isReduceMotionEnabled = { true }
        #expect(dialog.present(from: host.root, animated: false))
        #expect(dialog.modalTransitionStyle == .crossDissolve)
    }

    @Test("A presenter shared with another owner refuses this dialog")
    func busyPresenter() {
        let presenter = TSDialogPresenter()
        let owner = Owner()
        presenter.begin(owner)
        let host = Host()
        #expect(!makeDialog(presenter: presenter).present(from: host.root, animated: false))
    }
}

@MainActor
private func detentIdentifiers(_ sheet: UISheetPresentationController) -> [UISheetPresentationController.Detent.Identifier] {
    if #available(iOS 16, *) {
        return sheet.detents.map { $0.identifier }
    }
    return sheet.detents.isEmpty ? [] : [.medium, .large]
}
