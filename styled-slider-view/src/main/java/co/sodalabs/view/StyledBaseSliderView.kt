package co.sodalabs.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ProgressBar
import android.widget.SeekBar
import co.sodalabs.view.slider.R

/**
 * The base slider view
 *
 * @see [R.attr.slThumbDrawable] The thumb drawable.
 * @see [R.attr.slTrackBackgroundDrawable] The track (a.k.a progress, but only the background part) drawable.
 * @see [R.attr.slMarkerDrawableMiddle] The marker (tick) drawable in the middle.
 * @see [R.attr.slMarkerDrawableStart] The marker (tick) drawable at the start.
 * @see [R.attr.slMarkerDrawableEnd] The marker (tick) drawable at the end.
 * @see [R.attr.slMarkerNum] The amount of markers on the track. The markers are distributed evenly spaced.
 * @see [R.attr.slTouchDragSlop] A slop where the touch forms a drag if the move distance is over.
 */
abstract class StyledBaseSliderView : AppCompatSeekBar {

    // Thumb
    protected var thumbDrawable: Drawable? = null
        set(value) {
            field = value
            onThumbDrawableChanged()
        }
    /**
     * The starting x of the thumb (align with the center of the thumb)
     */
    protected var thumbStartX: Float = 0f
    /**
     * The ending x of the thumb (align with the center of the thumb)
     */
    protected var thumbEndX: Float = 0f

    // Track
    protected var trackForegroundDrawable: Drawable? = null
        set(value) {
            ensureTrackDrawableBoundary(value)
            field = value
        }

    protected var trackBackgroundDrawable: Drawable?
        set(value) {
            ensureTrackDrawableBoundary(value)
            // The setter will take care of the boundary (by forcing layout)
            progressDrawable = value
        }
        get() {
            return progressDrawable
        }

    protected var touchStartX: Float = 0f
    protected var touchDragging: Boolean = false
    protected var touchDragSlop: Float = context.resources.getDimension(R.dimen.default_touch_drag_slop)

    protected val tmpBound = RectF()
    protected var isTrackingTouch = false

    protected val progressInternalMethod by lazy {
        val clazz = ProgressBar::class.java

        val setProgressInternal = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            clazz.getDeclaredMethod(
                "setProgress",
                Int::class.java,
                Boolean::class.java)
        } else {
            clazz.getDeclaredMethod(
                "setProgressInternal",
                Int::class.java,
                Boolean::class.java,
                Boolean::class.java)
        }

        setProgressInternal.isAccessible = true
        return@lazy setProgressInternal
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // By default determinate
        isIndeterminate = false

        // Force original thumb null
        thumb = null

        initProperties(attrs)
    }

    private fun initProperties(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StyledSliderView, 0, 0)

        thumbDrawable = ContextCompat.getDrawable(context, R.drawable.default_slider_thumb)
        // Override the track drawable
        trackForegroundDrawable = ContextCompat.getDrawable(context, R.drawable.default_slider_foreground_track)
        trackBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.default_slider_background_track)

        for (i in 0 until typedArray.indexCount) {
            when (typedArray.getIndex(i)) {
                R.styleable.StyledSliderView_slThumbDrawable -> {
                    thumbDrawable = typedArray.getCompatDrawable(context,
                        R.styleable.StyledSliderView_slThumbDrawable)
                }
                R.styleable.StyledSliderView_slTrackForegroundDrawable -> {
                    trackForegroundDrawable = typedArray.getCompatDrawable(context,
                        R.styleable.StyledSliderView_slTrackForegroundDrawable)
                }
                R.styleable.StyledSliderView_slTrackBackgroundDrawable -> {
                    trackBackgroundDrawable = typedArray.getCompatDrawable(context,
                        R.styleable.StyledSliderView_slTrackBackgroundDrawable)
                }
                R.styleable.StyledSliderView_slTouchDragSlop -> {
                    touchDragSlop = typedArray.getDimension(R.styleable.StyledSliderView_slTouchDragSlop, touchDragSlop)
                }
            }
        }

        typedArray.recycle()
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            onThumbDrawableChanged()

            // Manually update foreground track boundary
            ensureTrackDrawableBoundary(trackForegroundDrawable)
        }
    }

    /**
     * The moment for setting [thumbStartX] and [thumbEndX]. It could get triggered
     * by setting thumb drawable or layout change.
     */
    protected open fun onThumbDrawableChanged() {
        if (width == 0 || height == 0) return

        thumbStartX = paddingLeft.toFloat()
        thumbEndX = (width - paddingRight).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        drawTrack(canvas)
        drawThumb(canvas)
    }

    private fun ensureTrackDrawableBoundary(drawable: Drawable?) {
        val validViewWidth = width - paddingLeft - paddingRight
        val validViewHeight = height - paddingTop - paddingBottom
        if (validViewWidth <= 0 && validViewHeight <= 0) return

        drawable?.let {
            if (it.bounds.width() != validViewWidth ||
                it.bounds.height() != validViewHeight) {
                it.setBounds(0, 0, validViewWidth, validViewHeight)
            }
        }
    }

    /**
     * Called in [onDraw] and is to draw the foreground/background track.
     */
    protected open fun drawTrack(canvas: Canvas) {
        val paddingLeft = paddingLeft.toFloat()
        val paddingRight = paddingTop.toFloat()

        // Background track
        canvas.runSafely {
            translate(paddingLeft, paddingRight)
            trackBackgroundDrawable?.draw(canvas)
        }

        ensureTrackDrawableBoundary(trackForegroundDrawable)

        // Foreground track
        canvas.runSafely {
            translate(paddingLeft, paddingRight)

            val progressFloat = progress.toFloat() / 100f
            val drawableBound = trackForegroundDrawable?.bounds
            val clipWidth = (drawableBound?.width()?.toFloat() ?: 0f) * progressFloat
            val clipHeight = drawableBound?.height()?.toFloat() ?: 0f
            tmpBound.set(0f, 0f, clipWidth, clipHeight)
            clipRect(tmpBound)

            trackForegroundDrawable?.draw(canvas)
        }
    }

    /**
     * Called in [onDraw] and is to draw the thumb.
     */
    protected open fun drawThumb(canvas: Canvas) {
        // DO NOTHING
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                onStartTrackingTouch()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!touchDragging) {
                    touchDragging = isTouchDrag(event.x)
                }

                if (touchDragging) {
                    val x = constraintTouchX(
                        touchX = event.x,
                        from = thumbStartX,
                        to = thumbEndX)

                    val prog = positionToIntProgress(x)
                    callProgressInternal(prog, true)
                }

                if (!isTrackingTouch) {
                    onStartTrackingTouch()
                }

                return true
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                if (!isTrackingTouch) {
                    onStartTrackingTouch()
                }

                touchDragging = false
                onStopTrackingTouch()

                val x = constraintTouchX(
                    touchX = event.x,
                    from = thumbStartX,
                    to = thumbEndX)

                val prog = positionToIntProgress(x)
                callProgressInternal(prog, true)

                return true
            }

            else -> return false
        }
    }

    protected fun onStartTrackingTouch() {
        if (isTrackingTouch) return

        isTrackingTouch = true
        touchTrackingDelegateListener.onStartTrackingTouch(this)
    }

    protected fun onStopTrackingTouch() {
        if (!isTrackingTouch) return

        isTrackingTouch = false
        touchTrackingDelegateListener.onStopTrackingTouch(this)
    }

    protected fun callProgressInternal(progress: Int, fromUser: Boolean, animate: Boolean = false) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            progressInternalMethod.invoke(this, progress, fromUser)
        } else {
            progressInternalMethod.invoke(this, progress, fromUser, animate)
        }
    }

    protected fun isTouchDrag(touchX: Float): Boolean {
        return Math.abs(touchX - touchStartX) > touchDragSlop
    }

    protected fun constraintTouchX(touchX: Float, from: Float, to: Float): Float {
        return if (touchX < from) {
            from
        } else {
            if (touchX > to) {
                to
            } else {
                touchX
            }
        }
    }

    protected fun positionToIntProgress(thumbX: Float): Int {
        return Math.round(100f * (thumbX - thumbStartX) / (thumbEndX - thumbStartX))
    }

    /**
     * A wrapper listener dispatching [IStyledSliderListener] callbacks.
     */
    private val touchTrackingDelegateListener = WrapperListener()

    /**
     * Redirect the given [SeekBar.OnSeekBarChangeListener] to [touchTrackingDelegateListener].
     */
    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
        touchTrackingDelegateListener.actualListener = l
        super.setOnSeekBarChangeListener(if (l != null) touchTrackingDelegateListener else null)
    }

    /**
     * The [IStyledSliderListener] wrapper class.
     */
    private class WrapperListener(
        var actualListener: SeekBar.OnSeekBarChangeListener? = null
    ) : OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            actualListener?.onProgressChanged(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            actualListener?.onStartTrackingTouch(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            actualListener?.onStopTrackingTouch(seekBar)
        }
    }
}