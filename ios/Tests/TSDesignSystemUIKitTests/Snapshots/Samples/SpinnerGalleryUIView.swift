import TSDesignSystemUIKit
import UIKit

final class SpinnerGalleryUIView: GalleryStackView {
    override init(frame: CGRect) {
        super.init(frame: frame)
        for tone in TSSpinnerTone.allCases {
            let label = UILabel()
            label.attributedText = TSTypeRole.bodySm.font.attributedString(tone.rawValue, sizeCategory: .large, color: TSTextColor.muted.color.uiColor)
            addRow(TSSpinnerSize.allCases.map { TSSpinnerView(size: $0, tone: tone) } + [label])
        }
    }
}
