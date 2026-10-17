import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class SwitchViewController: DemoPageViewController {
    private var changeCount = 0
    private lazy var counter = DemoPlayground.counter(identifier: "demo.switch.playground.count")
    private lazy var valueCaption: DemoLabel = {
        let caption = DemoPlayground.caption(DemoStrings.string(.stateOff))
        caption.accessibilityIdentifier = "demo.switch.playground.value"
        return caption
    }()
    private lazy var playgroundSwitch: TSSwitchView = {
        let control = TSSwitchView(label: DemoStrings.string(.sampleDarkMode))
        control.accessibilityIdentifier = "demo.switch.playground.switch"
        control.addAction(UIAction { [weak self] _ in self?.switchDidChange() }, for: .valueChanged)
        return control
    }()

    init() {
        super.init(page: .switch)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func makeContentViews() -> [UIView] {
        [statesSection(), labelHiddenSection(), wrappingSection(), playgroundSection()]
    }

    private func statesSection() -> UIView {
        let section = DemoComponentSectionView(.states, views: [])
        for isDisabled in [false, true] {
            for isOn in [false, true] {
                let control = TSSwitchView(label: DemoStrings.string(.sampleWifi), isOn: isOn)
                control.isEnabled = !isDisabled
                section.addFullWidth(DemoComponentRowView([
                    control,
                    DemoPlayground.caption(SwitchSample.caption(isOn: isOn, isDisabled: isDisabled))
                ]))
            }
        }
        [false, true].enumerated().forEach { index, isOn in
            section.addFullWidth(TSSwitchView(label: SwitchSample.label(index), isOn: isOn))
        }
        return section
    }

    private func labelHiddenSection() -> UIView {
        let row = UIStackView(arrangedSubviews: [false, true].enumerated().map { index, isOn in
            TSSwitchView(label: SwitchSample.label(index), isOn: isOn, isLabelHidden: true)
        })
        row.axis = .horizontal
        row.alignment = .center
        row.spacing = TSSpacing.space4
        return DemoComponentSectionView(.labelHidden, views: [row])
    }

    private func wrappingSection() -> UIView {
        let section = DemoComponentSectionView(.wrapping, views: [])
        section.addFullWidth(TSSwitchView(label: DemoStrings.string(.sampleLongSetting), isOn: true))
        return section
    }

    private func playgroundSection() -> UIView {
        let control = playgroundSwitch
        let section = DemoComponentSectionView(.playground, views: [])
        [
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.switch.playground.disabled") { control.isEnabled = !$0 },
            DemoPlayground.toggle(.labelHidden, identifier: "demo.switch.playground.label-hidden") { control.isLabelHidden = $0 },
            control
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(valueCaption)
        section.addArrangedSubview(counter)
        return section
    }

    private func switchDidChange() {
        changeCount += 1
        counter.string = DemoStrings.tapCount(changeCount)
        valueCaption.string = DemoStrings.string(playgroundSwitch.isOn ? .stateOn : .stateOff)
    }
}

enum SwitchSample {
    static func label(_ index: Int) -> String {
        DemoStrings.string(index == 0 ? .sampleNotifications : .sampleDarkMode)
    }

    static func caption(isOn: Bool, isDisabled: Bool) -> String {
        let value = DemoStrings.string(isOn ? .stateOn : .stateOff)
        return isDisabled ? "\(value), \(DemoStrings.string(.stateDisabled))" : value
    }
}
