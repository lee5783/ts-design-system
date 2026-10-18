package com.ts.ds.views.components.avatar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.withClip
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.textview.MaterialTextView
import com.ts.ds.core.icon.TSIcon
import com.ts.ds.core.logic.TSAvatarImage
import com.ts.ds.core.logic.TSInitials
import com.ts.ds.core.style.TSAvatarContent
import com.ts.ds.core.style.TSAvatarSize
import com.ts.ds.core.style.TSAvatarStyleSpec
import com.ts.ds.core.style.resolve
import com.ts.ds.views.R
import com.ts.ds.views.components.shared.dpToPx
import com.ts.ds.views.theme.tsColor
import kotlin.math.roundToInt

public class TSAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val imageView = AppCompatImageView(context)
    private val initialsView = MaterialTextView(context)
    private val placeholderView = AppCompatImageView(context)
    private val chrome = GradientDrawable()
    private val clipPath = Path()
    private var ready = false

    public var name: String = ""
        set(value) {
            field = value
            applyConfiguration()
        }

    public var image: Drawable? = null
        set(value) {
            field = value
            applyConfiguration()
        }

    public var size: TSAvatarSize = TSAvatarSize.Md
        set(value) {
            field = value
            applyConfiguration()
            requestLayout()
        }

    init {
        background = chrome
        clipToOutline = true
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        placeholderView.scaleType = ImageView.ScaleType.FIT_CENTER
        placeholderView.setImageResource(TSIcon.User.drawableRes)
        initialsView.gravity = Gravity.CENTER
        initialsView.maxLines = 1
        initialsView.isSingleLine = true
        initialsView.includeFontPadding = false
        initialsView.setTextAppearance(R.style.TextAppearance_TS_Component_AvatarInitials)
        listOf(imageView, placeholderView).forEach { it.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }
        addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(initialsView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(placeholderView, LayoutParams(0, 0, Gravity.CENTER))
        context.obtainStyledAttributes(attrs, R.styleable.TSAvatarView, defStyleAttr, 0).apply {
            name = getString(R.styleable.TSAvatarView_tsName) ?: ""
            image = getDrawable(R.styleable.TSAvatarView_android_src)
            size = TSAvatarSize.entries[getInt(R.styleable.TSAvatarView_tsAvatarSize, TSAvatarSize.Md.ordinal)]
            recycle()
        }
        ready = true
        applyConfiguration()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val diameter = MeasureSpec.makeMeasureSpec(dpToPx(resolved.diameterDp), MeasureSpec.EXACTLY)
        super.onMeasure(diameter, diameter)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clipPath.reset()
        clipPath.addOval(0f, 0f, w.toFloat(), h.toFloat(), Path.Direction.CW)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withClip(clipPath) { super.dispatchDraw(canvas) }
    }

    internal val initials: String
        get() = TSInitials.from(name)

    internal val content: TSAvatarContent
        get() = TSAvatarContent.make(initials, hasImage = TSAvatarImage.usable(image) != null)

    internal val resolved: TSAvatarStyleSpec.Resolved
        get() = TSAvatarStyleSpec.resolve(TSAvatarStyleSpec.Configuration(size, content))

    internal val initialsLabel: TextView
        get() = initialsView

    private fun applyConfiguration() {
        if (!ready) return
        val resolved = resolved
        val foreground = tsColor(resolved.foreground)
        chrome.setColor(tsColor(resolved.fill))
        chrome.cornerRadius = dpToPx(resolved.cornerRadiusDp).toFloat()
        imageView.setImageDrawable(if (resolved.showsImage) TSAvatarImage.usable(image) else null)
        imageView.visibility = if (resolved.showsImage) VISIBLE else GONE
        initialsView.text = if (resolved.showsInitials) initials else ""
        initialsView.visibility = if (resolved.showsInitials) VISIBLE else GONE
        initialsView.setTextColor(foreground)
        val inset = dpToPx(resolved.contentInsetDp)
        initialsView.setPadding(inset, inset, inset, inset)
        val density = resources.displayMetrics.density
        val maxPx = (resolved.initialsFontSizeDp * density).roundToInt()
        val minPx = (resolved.initialsFontSizeDp * resolved.initialsMinimumScaleFactor * density).roundToInt()
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(initialsView, minPx, maxPx, 1, TypedValue.COMPLEX_UNIT_PX)
        val iconSize = dpToPx(resolved.placeholderIconSizeDp)
        placeholderView.layoutParams = (placeholderView.layoutParams as LayoutParams).apply {
            width = iconSize
            height = iconSize
        }
        placeholderView.visibility = if (resolved.showsPlaceholder) VISIBLE else GONE
        ImageViewCompat.setImageTintList(placeholderView, ColorStateList.valueOf(foreground))
    }
}
