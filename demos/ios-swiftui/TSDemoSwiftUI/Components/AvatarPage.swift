import SwiftUI
import TSDemoSupport
import TSDesignSystem

struct AvatarPage: View {
    @State private var size: TSAvatarSize = .md
    @State private var personID = SamplePeople.all[0].id
    @State private var showsPhoto = false

    var body: some View {
        DemoPage(page: .avatar) {
            DemoComponentSection(.sizes) {
                ForEach(TSAvatarSize.allCases, id: \.self) { size in
                    DemoComponentRow {
                        TSAvatar(name: SamplePeople.all[0].name, size: size)
                        TSAvatar(name: SamplePeople.all[0].name, image: DemoSamplePhoto.image, size: size)
                        DemoCaption(text: size.rawValue)
                    }
                }
            }
            DemoComponentSection(.contents) {
                ForEach(TSAvatarSize.allCases, id: \.self) { size in
                    HStack(spacing: TSSpacing.space3) {
                        TSAvatar(name: SamplePeople.all[1].name, size: size)
                        TSAvatar(name: SamplePeople.all[1].name, image: DemoSamplePhoto.image, size: size)
                        TSAvatar(name: "", size: size)
                    }
                }
                DemoCaption(text: [DemoStrings.string(.initials), DemoStrings.string(.photo), DemoStrings.string(.noName)].joined(separator: " · "))
            }
            DemoComponentSection(.initials) {
                ForEach(SamplePeople.all) { person in
                    DemoComponentRow {
                        TSAvatar(name: person.name)
                        DemoCaption(text: person.name)
                    }
                }
            }
            DemoComponentSection(.playground) {
                Picker(DemoStrings.string(.size), selection: $size) {
                    ForEach(TSAvatarSize.allCases, id: \.self) { Text($0.rawValue).tag($0) }
                }
                .pickerStyle(.segmented)
                Picker(DemoStrings.string(.person), selection: $personID) {
                    ForEach(SamplePeople.all) { Text($0.name).tag($0.id) }
                }
                .pickerStyle(.menu)
                Toggle(DemoStrings.string(.photo), isOn: $showsPhoto)
                    .accessibilityIdentifier("demo.avatar.playground.photo")
                TSAvatar(name: person.name, image: showsPhoto ? DemoSamplePhoto.image : nil, size: size)
                    .accessibilityIdentifier("demo.avatar.playground.avatar")
            }
            .toggleStyle(SwitchToggleStyle(tint: TSColor.accentPrimary.color))
            .foregroundColor(TSColor.textPrimary.color)
        }
    }

    private var person: SamplePerson {
        SamplePeople.all.first { $0.id == personID } ?? SamplePeople.all[0]
    }
}
