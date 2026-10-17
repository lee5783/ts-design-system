import Combine
import TSDemoSupport
import TSDesignSystemUIKit
import UIKit

final class DemoSettingsViewController: UITableViewController {
    private enum Section: Int, CaseIterable {
        case theme
        case textSize
        case toggles
        case reset
    }

    private let settings: DemoSettingsModel
    private var subscription: AnyCancellable?

    private lazy var themeControl: UISegmentedControl = {
        let control = UISegmentedControl(items: DemoTheme.allCases.map(\.title))
        control.accessibilityIdentifier = "demo.settings.theme"
        control.addAction(UIAction { [weak self, unowned control] _ in
            self?.settings.theme = DemoTheme.allCases[control.selectedSegmentIndex]
        }, for: .valueChanged)
        return control
    }()

    private lazy var textSizeButton: UIButton = {
        var configuration = UIButton.Configuration.plain()
        configuration.image = UIImage(systemName: "chevron.up.chevron.down")
        configuration.imagePlacement = .trailing
        configuration.imagePadding = TSSpacing.space1
        configuration.preferredSymbolConfigurationForImage = UIImage.SymbolConfiguration(scale: .small)
        configuration.contentInsets = .zero
        configuration.titleLineBreakMode = .byWordWrapping
        let button = UIButton(configuration: configuration)
        button.contentHorizontalAlignment = .leading
        button.showsMenuAsPrimaryAction = true
        button.accessibilityIdentifier = "demo.settings.text-size"
        button.accessibilityLabel = DemoStrings.string(.settingsTextSize)
        return button
    }()

    private lazy var previewLabel: DemoLabel = {
        let label = DemoLabel(DemoStrings.string(.settingsTextSizePreview), font: .body, color: .textSecondary)
        label.accessibilityIdentifier = "demo.settings.text-size.preview"
        return label
    }()

    private lazy var rightToLeftSwitch = makeSwitch(identifier: "demo.settings.right-to-left") { [weak self] isOn in
        self?.settings.isRightToLeft = isOn
    }

    private lazy var reduceMotionSwitch = makeSwitch(identifier: "demo.settings.reduce-motion") { [weak self] isOn in
        self?.settings.reduceMotion = isOn
    }

    private lazy var themeCell = makeControlCell(themeControl)
    private lazy var textSizeCell = makeControlCell(textSizeButton)
    private lazy var previewCell = makeControlCell(previewLabel)
    private lazy var rightToLeftCell = makeLabeledCell(DemoStrings.string(.settingsRightToLeft), accessory: rightToLeftSwitch)
    private lazy var reduceMotionCell = makeLabeledCell(DemoStrings.string(.settingsReduceMotion), accessory: reduceMotionSwitch)
    private lazy var resetCell: UITableViewCell = {
        let cell = UITableViewCell()
        cell.accessibilityIdentifier = "demo.settings.reset"
        cell.accessibilityTraits = .button
        return cell
    }()

    init(settings: DemoSettingsModel) {
        self.settings = settings
        super.init(style: .insetGrouped)
    }

    required init?(coder: NSCoder) {
        nil
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        title = DemoStrings.string(.settingsTitle)
        navigationItem.largeTitleDisplayMode = .never
        let done = UIBarButtonItem(systemItem: .done, primaryAction: UIAction { [weak self] _ in
            guard let self else { return }
            dismiss(animated: !settings.reduceMotion)
        })
        done.accessibilityIdentifier = "demo.settings.done"
        navigationItem.rightBarButtonItem = done
        subscription = settings.objectWillChange
            .receive(on: DispatchQueue.main)
            .sink { [weak self] _ in self?.render() }
        render()
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        Section.allCases.count
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        switch Section(rawValue: section) {
        case .theme, .reset:
            1
        case .textSize, .toggles:
            2
        case nil:
            0
        }
    }

    override func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        switch Section(rawValue: section) {
        case .theme:
            DemoStrings.string(.settingsTheme)
        case .textSize:
            DemoStrings.string(.settingsTextSize)
        case .toggles, .reset, nil:
            nil
        }
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        switch (Section(rawValue: indexPath.section), indexPath.row) {
        case (.theme, _):
            themeCell
        case (.textSize, 0):
            textSizeCell
        case (.textSize, _):
            previewCell
        case (.toggles, 0):
            rightToLeftCell
        case (.toggles, _):
            reduceMotionCell
        case (.reset, _), (nil, _):
            resetCell
        }
    }

    override func tableView(_ tableView: UITableView, shouldHighlightRowAt indexPath: IndexPath) -> Bool {
        Section(rawValue: indexPath.section) == .reset && !settings.isDefault
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        guard Section(rawValue: indexPath.section) == .reset else { return }
        settings.reset()
    }

    private func render() {
        themeControl.selectedSegmentIndex = DemoTheme.allCases.firstIndex(of: settings.theme) ?? 0
        textSizeButton.configuration?.title = settings.textSize.title
        textSizeButton.accessibilityValue = settings.textSize.title
        textSizeButton.menu = UIMenu(children: DemoTextSize.allCases.map { textSize in
            UIAction(title: textSize.title, state: textSize == settings.textSize ? .on : .off) { [weak self] _ in
                self?.settings.textSize = textSize
            }
        })
        rightToLeftSwitch.setOn(settings.isRightToLeft, animated: false)
        reduceMotionSwitch.setOn(settings.reduceMotion, animated: false)
        var resetContent = UIListContentConfiguration.cell()
        resetContent.text = DemoStrings.string(.settingsReset)
        resetContent.textProperties.color = settings.isDefault ? TSColor.textDisabled.uiColor : TSColor.accentText.uiColor
        resetCell.contentConfiguration = resetContent
        resetCell.accessibilityTraits = settings.isDefault ? [.button, .notEnabled] : .button
        if tableView.window != nil {
            tableView.performBatchUpdates(nil)
        }
    }

    private func makeSwitch(identifier: String, onChange: @escaping (Bool) -> Void) -> UISwitch {
        let control = UISwitch()
        control.accessibilityIdentifier = identifier
        control.addAction(UIAction { [unowned control] _ in onChange(control.isOn) }, for: .valueChanged)
        return control
    }

    private func makeLabeledCell(_ title: String, accessory: UIView) -> UITableViewCell {
        let cell = UITableViewCell()
        var content = UIListContentConfiguration.cell()
        content.text = title
        content.textProperties.font = TSFont.body.uiFont()
        content.textProperties.color = TSColor.textPrimary.uiColor
        content.textProperties.adjustsFontForContentSizeCategory = true
        cell.contentConfiguration = content
        accessory.sizeToFit()
        cell.accessoryView = accessory
        cell.selectionStyle = .none
        return cell
    }

    private func makeControlCell(_ control: UIView) -> UITableViewCell {
        let cell = UITableViewCell()
        cell.selectionStyle = .none
        control.translatesAutoresizingMaskIntoConstraints = false
        cell.contentView.addSubview(control)
        let margins = cell.contentView.layoutMarginsGuide
        NSLayoutConstraint.activate([
            control.topAnchor.constraint(equalTo: margins.topAnchor),
            control.bottomAnchor.constraint(equalTo: margins.bottomAnchor),
            control.leadingAnchor.constraint(equalTo: margins.leadingAnchor),
            control.trailingAnchor.constraint(equalTo: margins.trailingAnchor)
        ])
        return cell
    }
}
