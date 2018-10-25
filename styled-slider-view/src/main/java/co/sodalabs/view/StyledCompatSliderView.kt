package co.sodalabs.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet

/**
 * This is a wrapper of [AppCompatSeekBar] which uses "android:seekBarStyle"
 * style attribute by default and this enable us the hot-theme function.
 */
class StyledCompatSliderView : AppCompatSeekBar {

    companion object {
        private const val MINIMUM_PROGRESS = 5
    }

    private val paint = Paint()
    private val thumbBitmap by lazy { context.getBitmapFromVectorDrawable(R.drawable.ic_slider_thumb) }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, android.R.attr.seekBarStyle)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
        initProperties(attrs)
    }

    private fun initView() {
        progressDrawable = ContextCompat.getDrawable(context, R.drawable.slider_progress)
        thumb = null
        paint.color = Color.parseColor("#ccffffff")
    }

    private fun initProperties(attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.StyledSlider, 0, 0)
        for (i in 0 until attributes.indexCount) {
            when (attributes.getIndex(i)) {
                R.styleable.StyledSlider_android_progressDrawable -> progressDrawable = attributes.getDrawable(
                    R.styleable.StyledSlider_android_progressDrawable)
            }
        }
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (progress >= MINIMUM_PROGRESS) {
            val available = width - paddingRight - thumbBitmap.width
            val thumbX = (available * progress / max).toFloat() - thumbBitmap.width
            c.drawBitmap(thumbBitmap, thumbX, 0f, paint)
        }
    }
}
