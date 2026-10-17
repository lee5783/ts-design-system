import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class CheckboxViewController: DemoPageViewController {
    private var changeCount = 0
    private lazy var counter = DemoPlayground.counter(identifier: "demo.checkbox.playground.count")
    private lazy var valueCaption: DemoLabel = {
        let caption = DemoPlayground.caption(DemoStrings.string(.stateUnchecked))
        caption.accessibilityIdentifier = "demo.checkbox.playground.value"
        return caption
    }()
    private lazy var playgroundCheckbox: TSCheckboxView = {
        let checkbox = TSCheckboxView(label: DemoStrings.string(.sampleAcceptTerms))
        checkbox.accessibilityIdentifier = "demo.checkbox.playground.checkbox"
        checkbox.addAction(UIAction { [weak self] _ in self?.checkboxDidChange() }, for: .valueChanged)
        return checkbox
    }()

    init() {
        super.init(page: .checkbox)
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
                let checkbox = TSCheckboxView(label: DemoStrings.string(.sampleEmailUpdates), isOn: isOn)
                checkbox.isEnabled = !isDisabled
                section.addFullWidth(DemoComponentRowView([
                    checkbox,
                    DemoPlayground.caption(CheckboxSample.caption(isOn: isOn, isDisabled: isDisabled))
                ]))
            }
        }
        [false, true].enumerated().forEach { index, isOn in
            section.addFullWidth(TSCheckboxView(label: CheckboxSample.label(index), isOn: isOn))
        }
        return section
    }

    private func labelHiddenSection() -> UIView {
        let row = UIStackView(arrangedSubviews: [false, true].enumerated().map { index, isOn in
            TSCheckboxView(label: CheckboxSample.label(index), isOn: isOn, isLabelHidden: true)
        })
        row.axis = .horizontal
        row.alignment = .center
        row.spacing = TSSpacing.space4
        return DemoComponentSectionView(.labelHidden, views: [row])
    }

    private func wrappingSection() -> UIView {
        let section = DemoComponentSectionView(.wrapping, views: [])
        section.addFullWidth(TSCheckboxView(label: DemoStrings.string(.sampleLongConsent), isOn: true))
        return section
    }

    private func playgroundSection() -> UIView {
        let checkbox = playgroundCheckbox
        let section = DemoComponentSectionView(.playground, views: [])
        [
            DemoPlayground.toggle(.stateDisabled, identifier: "demo.checkbox.playground.disabled") { checkbox.isEnabled = !$0 },
            DemoPlayground.toggle(.labelHidden, identifier: "demo.checkbox.playground.label-hidden") { checkbox.isLabelHidden = $0 },
            checkbox
        ].forEach(section.addFullWidth)
        section.addArrangedSubview(valueCaption)
        section.addArrangedSubview(counter)
        return section
    }

    private func checkboxDidChange() {
        changeCount += 1
        counter.string = DemoStrings.tapCount(changeCount)
        valueCaption.string = DemoStrings.string(playgroundCheckbox.isOn ? .stateChecked : .stateUnchecked)
    }
}

enum CheckboxSample {
    static func label(_ index: Int) -> String {
        DemoStrings.string(index == 0 ? .sampleRememberMe : .sampleAcceptTerms)
    }

    static func caption(isOn: Bool, isDisabled: Bool) -> String {
        let value = DemoStrings.string(isOn ? .stateChecked : .stateUnchecked)
        return isDisabled ? "\(value), \(DemoStrings.string(.stateDisabled))" : value
    }
}
