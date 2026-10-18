import UIKit

public final class TSTabsView: UIControl {
    public var items: [TSTabItem] {
        didSet { rebuildTabs() }
    }

    public var selectedID: String {
        didSet { applySelection() }
    }

    public var variant: TSTabsVariant {
        didSet { applyStyle() }
    }

    public var isBlock: Bool {
        didSet { applyStyle() }
    }

    override public var isEnabled: Bool {
        didSet { applyStyle() }
    }

    let trackView = UIView()
    let hairlineView = UIView()
    let scrollView = UIScrollView()
    let contentView = UIView()
    let stackView = UIStackView()
    let thumbShadowView = UIView()
    let thumbView = UIView()
    let indicatorView = UIView()
    private(set) var tabViews: [TSTabView] = []

    var playHaptic: @MainActor (TSHapticEvent) -> Void = { TSHaptics.shared.play($0) }
    var reduceMotion: @MainActor () -> Bool = { false }

    private var trackTrailing: NSLayoutConstraint?
    private var segmentedWidth: NSLayoutConstraint?
    private var hairlineHeight: NSLayoutConstraint?
    private var laidOutWidth: CGFloat = 0

    public init(
        items: [TSTabItem],
        selectedID: String? = nil,
        variant: TSTabsVariant = .underline,
        isBlock: Bool = false
    ) {
        self.items = items
        self.selectedID = selectedID ?? items.first?.id ?? ""
        self.variant = variant
        self.isBlock = isBlock
        super.init(frame: .zero)
        configureHierarchy()
        rebuildTabs()
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) is not supported")
    }

    var container: TSTabsStyleSpec.Container {
        TSTabsStyleSpec.container(variant, isEnabled: isEnabled)
    }

    var selectedTab: TSTabView? {
        tabViews.first { $0.item.id == selectedID }
    }

    override public func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        applyStyle()
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        scrollView.layoutIfNeeded()
        contentView.layoutIfNeeded()
        stackView.layoutIfNeeded()
        layoutSelection()
        if laidOutWidth != bounds.width {
            laidOutWidth = bounds.width
            scrollToSelection(animated: false)
        }
    }

    func userDidSelect(_ tab: TSTabView) {
        guard isEnabled, tab.item.id != selectedID else { return }
        selectedID = tab.item.id
        TSAnimation.animate(isInstant: reduceMotion()) {
            self.layoutIfNeeded()
        }
        scrollToSelection(animated: !reduceMotion())
        if variant == .segmented {
            playHaptic(.selection)
        }
        sendActions(for: .valueChanged)
    }

    private func configureHierarchy() {
        isAccessibilityElement = false
        addInteraction(UILargeContentViewerInteraction())

        [trackView, hairlineView, scrollView, stackView].forEach { $0.translatesAutoresizingMaskIntoConstraints = false }
        trackView.isUserInteractionEnabled = false
        hairlineView.isUserInteractionEnabled = false
        addSubview(trackView)
        addSubview(hairlineView)
        addSubview(scrollView)

        scrollView.showsHorizontalScrollIndicator = false
        scrollView.showsVerticalScrollIndicator = false
        scrollView.alwaysBounceVertical = false
        [thumbShadowView, thumbView].forEach {
            $0.isUserInteractionEnabled = false
            $0.layer.cornerCurve = .continuous
            scrollView.addSubview($0)
        }
        stackView.axis = .horizontal
        stackView.alignment = .fill
        stackView.isLayoutMarginsRelativeArrangement = true
        stackView.insetsLayoutMarginsFromSafeArea = false
        contentView.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(stackView)
        scrollView.addSubview(contentView)
        indicatorView.isUserInteractionEnabled = false
        scrollView.addSubview(indicatorView)

        let trailing = trackView.trailingAnchor.constraint(equalTo: trailingAnchor)
        trackTrailing = trailing
        segmentedWidth = stackView.widthAnchor.constraint(equalTo: scrollView.frameLayoutGuide.widthAnchor)
        let hairline = hairlineView.heightAnchor.constraint(equalToConstant: 0)
        hairlineHeight = hairline
        let content = scrollView.contentLayoutGuide
        NSLayoutConstraint.activate([
            trackView.topAnchor.constraint(equalTo: topAnchor),
            trackView.bottomAnchor.constraint(equalTo: bottomAnchor),
            trackView.leadingAnchor.constraint(equalTo: leadingAnchor),
            trackView.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor),
            trailing,
            hairlineView.leadingAnchor.constraint(equalTo: leadingAnchor),
            hairlineView.trailingAnchor.constraint(equalTo: trailingAnchor),
            hairlineView.bottomAnchor.constraint(equalTo: bottomAnchor),
            hairline,
            scrollView.topAnchor.constraint(equalTo: trackView.topAnchor),
            scrollView.bottomAnchor.constraint(equalTo: trackView.bottomAnchor),
            scrollView.leadingAnchor.constraint(equalTo: trackView.leadingAnchor),
            scrollView.trailingAnchor.constraint(equalTo: trackView.trailingAnchor),
            contentView.topAnchor.constraint(equalTo: content.topAnchor),
            contentView.bottomAnchor.constraint(equalTo: content.bottomAnchor),
            contentView.leadingAnchor.constraint(equalTo: content.leadingAnchor),
            contentView.trailingAnchor.constraint(equalTo: content.trailingAnchor),
            contentView.heightAnchor.constraint(equalTo: scrollView.frameLayoutGuide.heightAnchor),
            contentView.widthAnchor.constraint(greaterThanOrEqualTo: scrollView.frameLayoutGuide.widthAnchor),
            stackView.topAnchor.constraint(equalTo: contentView.topAnchor),
            stackView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor),
            stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            stackView.trailingAnchor.constraint(lessThanOrEqualTo: contentView.trailingAnchor)
        ])
        let hugsContent = contentView.widthAnchor.constraint(equalTo: stackView.widthAnchor)
        hugsContent.priority = .defaultLow
        let hugsFrame = scrollView.frameLayoutGuide.widthAnchor.constraint(equalTo: contentView.widthAnchor)
        hugsFrame.priority = UILayoutPriority(UILayoutPriority.defaultLow.rawValue - 1)
        NSLayoutConstraint.activate([hugsContent, hugsFrame])
    }

    private func rebuildTabs() {
        tabViews.forEach { $0.removeFromSuperview() }
        tabViews = items.map { item in
            let tab = TSTabView(item: item, variant: variant)
            tab.onSelect = { [weak self] in self?.userDidSelect($0) }
            stackView.addArrangedSubview(tab)
            return tab
        }
        if !items.contains(where: { $0.id == selectedID }), let first = items.first {
            selectedID = first.id
        }
        applyStyle()
    }

    private func applySelection() {
        tabViews.forEach { $0.isSelected = $0.item.id == selectedID }
        setNeedsLayout()
    }

    private func applyStyle() {
        let container = container
        let traits = traitCollection
        let isSegmented = variant == .segmented
        let inset = container.padding + container.borderWidth

        trackView.backgroundColor = container.fill?.uiColor.resolvedColor(with: traits) ?? .clear
        trackView.tsApplyBorder(isSegmented ? container.border : nil, width: container.borderWidth, cornerRadius: container.cornerRadius)
        trackView.alpha = container.opacity
        hairlineView.isHidden = isSegmented
        hairlineView.backgroundColor = container.border.uiColor.resolvedColor(with: traits)
        hairlineView.alpha = container.opacity
        hairlineHeight?.constant = container.borderWidth

        scrollView.isScrollEnabled = container.isScrollable
        scrollView.clipsToBounds = container.isScrollable
        trackTrailing?.isActive = !isSegmented || isBlock
        segmentedWidth?.isActive = isSegmented
        stackView.spacing = container.spacing
        stackView.distribution = isSegmented && isBlock ? .fillEqually : .fill
        stackView.directionalLayoutMargins = NSDirectionalEdgeInsets(top: inset, leading: inset, bottom: inset, trailing: inset)

        tabViews.forEach {
            $0.variant = variant
            $0.isEnabled = isEnabled
            $0.isSelected = $0.item.id == selectedID
        }
        setNeedsLayout()
    }

    private func layoutSelection() {
        guard let tab = selectedTab else {
            [thumbView, thumbShadowView, indicatorView].forEach { $0.isHidden = true }
            return
        }
        let selected = TSTabsStyleSpec.resolve(.init(variant: variant, isSelected: true, state: tab.controlState))
        let traits = traitCollection
        let frame = tab.convert(tab.bounds, to: scrollView)

        let showsThumb = selected.fill != nil
        [thumbView, thumbShadowView].forEach {
            $0.isHidden = !showsThumb
            $0.frame = frame
            $0.layer.cornerRadius = selected.cornerRadius
            $0.alpha = selected.opacity
        }
        thumbView.backgroundColor = selected.fill?.uiColor.resolvedColor(with: traits)
        thumbShadowView.backgroundColor = .clear
        if let shadow = selected.shadow {
            TSShadow.apply(
                shadow,
                style: traits.userInterfaceStyle,
                to: [thumbView.layer, thumbShadowView.layer],
                cornerRadius: selected.cornerRadius
            )
        }

        indicatorView.isHidden = selected.indicatorColor == nil
        indicatorView.backgroundColor = selected.indicatorColor?.uiColor.resolvedColor(with: traits)
        indicatorView.alpha = selected.opacity
        indicatorView.frame = CGRect(
            x: frame.minX,
            y: frame.maxY - selected.indicatorHeight,
            width: frame.width,
            height: selected.indicatorHeight
        )
    }

    private func scrollToSelection(animated: Bool) {
        guard container.isScrollable, let tab = selectedTab else { return }
        let frame = tab.convert(tab.bounds, to: scrollView).insetBy(dx: -container.spacing, dy: 0)
        scrollView.scrollRectToVisible(frame, animated: animated)
    }
}
