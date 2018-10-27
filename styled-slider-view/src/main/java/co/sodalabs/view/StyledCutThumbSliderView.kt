package co.sodalabs.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import co.sodalabs.view.slider.R

/**
 * This is a wrapper of [AppCompatSeekBar] which uses "android:seekBarStyle"
 * style attribute by default and this enable us the hot-theme function.
 *
 * @see [R.attr.thumbDrawable] The thumb drawable.
 * @see [R.attr.trackForegroundDrawable] The foreground track drawable.
 * @see [R.attr.trackBackgroundDrawable] The background track (a.k.a progress, but only the background part) drawable.
 * @see [R.attr.touchDragSlop] A slop where the touch forms a drag if the move distance is over.
 */
class StyledCutThumbSliderView : StyledBaseSliderView {

    private var thumbBitmap: Bitmap? = null
    private var trackBitmap: Bitmap? = null

    private var mergedBitmap: Bitmap? = null
    private var mergedCanvas: Canvas? = null

    private val bitmapPaint = Paint()
    private val cutMaskPaint = Paint()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        bitmapPaint.isAntiAlias = true
        cutMaskPaint.isAntiAlias = true
        cutMaskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            onCutBitmapChanged()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // The super call will handle the track boundary
        super.onSizeChanged(w, h, oldw, oldh)

        // The track bounds is valid after super call
        onCutBitmapChanged()
    }

    private fun onCutBitmapChanged() {
        val w = trackBackgroundDrawable?.bounds?.width() ?: 0
        val h = trackBackgroundDrawable?.bounds?.height() ?: 0
        if (w == 0 || h == 0) return

        trackBackgroundDrawable?.run {
            trackBitmap?.recycle()
            trackBitmap = this.toBitmap()

            mergedBitmap?.recycle()
            mergedBitmap = this.toBitmap()
            mergedBitmap?.let {
                mergedCanvas = Canvas(it)
            }
        }
    }

    override fun onThumbDrawableChanged() {
        // Default thumb properties setting
        super.onThumbDrawableChanged()

        val drawableWidth = thumbDrawable?.intrinsicWidth ?: 0
        val drawableHeight = thumbDrawable?.intrinsicHeight ?: 0
        thumbDrawable?.setBounds(0, 0, drawableWidth, drawableHeight)
        thumbDrawable?.run {
            thumbBitmap?.recycle()
            thumbBitmap = this.toBitmap()
        }
    }

    override fun drawThumb(canvas: Canvas) {
        if (isInEditMode) {
            // The AndroidStudio preview cannot render PorterDuff mode correctly.
            // Therefore, it's a workaround to imitate the final visual.
            val thumbWidth = thumbBitmap?.width ?: 0
            val thumbHeight = thumbBitmap?.height ?: 0
            val progressFloat = this.progress.toFloat() / 100f
            val thumbX = -thumbWidth + progressFloat * thumbEndX + (1f - progressFloat) * thumbStartX
            val thumbY = (height - thumbHeight) / 2f

            canvas.runSafely {
                translate(thumbX, thumbY)
                thumbDrawable?.draw(canvas)
            }
        } else {
            val thumbWidth = thumbBitmap?.width ?: 0
            val thumbHeight = thumbBitmap?.height ?: 0
            val progressFloat = this.progress.toFloat() / 100f
            //        val thumbX = progressFloat * thumbEndX + (1f - progressFloat) * thumbStartX
            val thumbX = -thumbWidth + progressFloat * (mergedBitmap?.width?.toFloat() ?: 0f)
            val thumbY = (height - thumbHeight) / 2f

            mergedBitmap?.eraseColor(Color.TRANSPARENT)

            mergedCanvas?.runSafely {
                translate(thumbX, 0f)
                drawBitmap(thumbBitmap, 0f, 0f, bitmapPaint)
            }

            mergedCanvas?.runSafely {
                drawBitmap(trackBitmap, 0f, 0f, cutMaskPaint)
            }

            canvas.runSafely {
                translate(0f, thumbY)
                drawBitmap(mergedBitmap, 0f, 0f, bitmapPaint)
            }
        }
    }
}
