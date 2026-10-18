import SwiftUI
import TSDemoSupport

struct DemoPageView: View {
    let page: DemoPageID

    var body: some View {
        switch page {
        case .colors:
            ColorsPage()
        case .typography:
            TypographyPage()
        case .spacing:
            SpacingPage()
        case .radius:
            RadiusPage()
        case .size:
            SizePage()
        case .opacity:
            OpacityPage()
        case .elevation:
            ElevationPage()
        case .icons:
            IconsPage()
        case .button:
            ButtonPage()
        case .iconButton:
            IconButtonPage()
        case .badge:
            BadgePage()
        case .avatar:
            AvatarPage()
        case .textField:
            TextFieldPage()
        case .checkbox:
            CheckboxPage()
        case .switch:
            SwitchPage()
        case .tabs:
            TabsPage()
        case .listItem:
            ListItemPage()
        case .card:
            CardPage()
        case .alert:
            AlertPage()
        case .dialog:
            DialogPage()
        case .toast:
            ToastPage()
        case .bottomNav:
            BottomNavPage()
        case .spinner:
            SpinnerPage()
        case .text:
            TextPage()
        }
    }
}
