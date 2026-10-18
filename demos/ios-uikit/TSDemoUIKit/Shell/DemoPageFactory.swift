import TSDemoSupport
import UIKit

@MainActor
enum DemoPageFactory {
    static func makeViewController(for page: DemoPageID) -> UIViewController {
        switch page {
        case .colors:
            ColorsViewController()
        case .typography:
            TypographyViewController()
        case .spacing:
            SpacingViewController()
        case .radius:
            RadiusViewController()
        case .size:
            SizeViewController()
        case .opacity:
            OpacityViewController()
        case .elevation:
            ElevationViewController()
        case .icons:
            IconsViewController()
        case .button:
            ButtonViewController()
        case .iconButton:
            IconButtonViewController()
        case .badge:
            BadgeViewController()
        case .avatar:
            AvatarViewController()
        case .textField:
            TextFieldViewController()
        case .checkbox:
            CheckboxViewController()
        case .switch:
            SwitchViewController()
        case .tabs:
            TabsViewController()
        case .listItem:
            ListItemViewController()
        case .card:
            CardViewController()
        case .alert:
            AlertViewController()
        case .dialog:
            DialogViewController()
        case .toast:
            ToastViewController()
        case .bottomNav:
            BottomNavViewController()
        case .spinner:
            SpinnerViewController()
        case .text:
            TextViewController()
        }
    }
}
