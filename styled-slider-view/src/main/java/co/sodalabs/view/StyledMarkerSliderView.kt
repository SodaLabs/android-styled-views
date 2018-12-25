package co.sodalabs.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import co.sodalabs.view.slider.R

/**
 * A capsule track styled slider with flexible styled markers.
 *
 * @see [R.attr.slThumbDrawable] The thumb drawable.
 * @see [R.attr.slTrackBackgroundDrawable] The track (a.k.a progress, but only the background part) drawable.
 * @see [R.attr.slMarkerDrawableMiddle] The marker (tick) drawable in the middle.
 * @see [R.attr.slMarkerDrawableStart] The marker (tick) drawable at the start.
 * @see [R.attr.slMarkerDrawableEnd] The marker (tick) drawable at the end.
 * @see [R.attr.slMarkerNum] The amount of markers on the track. The markers are distributed evenly spaced.
 * @see [R.attr.slTouchDragSlop] A slop where the touch forms a drag if the move distance is over.
 */
open class StyledMarkerSliderView : StyledBaseSliderView {

    private var markerNum = 5
        set(value) {
            field = value
            onUpdateMarkerProperties()
        }

    // Thumb
    private var thumbAnimator: ValueAnimator? = null

    // Marker
    private var markerDrawableMiddle: Drawable? = null
        set(value) {
            value?.determineSelfCenterBound()
            field = value
        }
    private var markerDrawableStart: Drawable? = null
        set(value) {
            value?.determineSelfCenterBound()
            field = value
        }
    private var markerDrawableEnd: Drawable? = null
        set(value) {
            value?.determineSelfCenterBound()
            field = value
        }
    private var markerDistance: Float = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // By default determinate
        isIndeterminate = false

        // Force original thumb null
        thumb = null
        // Force foreground track null
        trackForegroundDrawable = null

        initProperties(attrs)
    }

    private fun initProperties(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StyledSliderView, 0, 0)

        thumbDrawable = ContextCompat.getDrawable(context, R.drawable.default_slider_thumb)
        // Override the track drawable
        trackBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.default_slider_background_track)
        // Marker
        markerDrawableMiddle = ContextCompat.getDrawable(context, R.drawable.default_marker_slider_marker_middle)
        markerDrawableStart = ContextCompat.getDrawable(context, R.drawable.default_marker_slider_marker_start)
        markerDrawableEnd = ContextCompat.getDrawable(context, R.drawable.default_marker_slider_marker_end)

        for (i in 0 until typedArray.indexCount) {
            when (typedArray.getIndex(i)) {
                R.styleable.StyledSliderView_slMarkerNum -> markerNum = typedArray.getInt(R.styleable.StyledSliderView_slMarkerNum, markerNum)
                R.styleable.StyledSliderView_slMarkerDrawableMiddle -> markerDrawableMiddle = typedArray.getCompatDrawable(context,
                    R.styleable.StyledSliderView_slMarkerDrawableMiddle)
                R.styleable.StyledSliderView_slMarkerDrawableStart -> markerDrawableStart = typedArray.getCompatDrawable(context,
                    R.styleable.StyledSliderView_slMarkerDrawableStart)
                R.styleable.StyledSliderView_slMarkerDrawableEnd -> markerDrawableEnd = typedArray.getCompatDrawable(context,
                    R.styleable.StyledSliderView_slMarkerDrawableEnd)
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
            onUpdateMarkerProperties()
        }
    }

    override fun onThumbDrawableChanged() {
        if (width == 0 || height == 0) return

        // IMPORTANT: Make the origin the center of the bound
        thumbDrawable?.determineSelfCenterBound()

        // IMPORTANT: The center bound simplify the later rendering. Because of
        // that, we shrink the drawing range
        val thumbHalfWidth = (thumbDrawable?.intrinsicWidth?.toFloat() ?: 0f) / 2f
        thumbStartX = paddingLeft + thumbHalfWidth
        thumbEndX = width - paddingRight - thumbHalfWidth
    }

    protected open fun onUpdateMarkerProperties() {
        if (width == 0 || height == 0) return

        markerDistance = if (markerNum > 1) {
            (thumbEndX - thumbStartX) / (markerNum - 1)
        } else {
            0f
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawTrack(canvas)
        drawMarker(canvas)
        drawThumb(canvas)
    }

    override fun drawThumb(canvas: Canvas) {
        val viewHeight = height.toFloat()
        val progressFloat = this.progress.toFloat() / 100f
        val thumbX = progressFloat * thumbEndX + (1f - progressFloat) * thumbStartX

        canvas.runSafely {
            translate(thumbX, viewHeight / 2f)
            thumbDrawable?.draw(canvas)
        }
    }

    private fun drawMarker(canvas: Canvas) {
        val viewHeight = height.toFloat()

        canvas.runSafely {
            translate(thumbStartX, viewHeight / 2f)
            for (i in 0 until markerNum) {
                when (i) {
                    0 -> markerDrawableStart?.draw(this)
                    markerNum - 1 -> markerDrawableEnd?.draw(this)
                    else -> markerDrawableMiddle?.draw(this)
                }

                translate(markerDistance, 0f)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
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

                return true
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                touchDragging = false

                val (closestX, closestIndex) = findClosestMarkerXAndIndex(event.x)
                snapToClosestMarkerSmoothly(closestX)

                // Dispatch callback
                progressDelegateListener.onMarkerLevelUpdated(
                    seekBar = this,
                    markerLevel = closestIndex)

                return true
            }

            else -> return false
        }
    }

    private fun findClosestMarkerXAndIndex(touchX: Float): Pair<Float, Int> {
        var closestX = thumbStartX
        var closestIndex = 0
        var closestDistance = Math.abs(touchX - closestX)
        for (i in 1 until markerNum) {
            val markerX = thumbStartX + i * markerDistance
            val distance = Math.abs(touchX - markerX)
            if (distance < closestDistance) {
                closestX = markerX
                closestIndex = i
                closestDistance = distance
            }
        }

        return Pair(closestX, closestIndex)
    }

    private fun snapToClosestMarkerSmoothly(closestX: Float) {
        val currentProgress = progress
        val nextProgress = positionToIntProgress(closestX)

        thumbAnimator?.cancel()
        thumbAnimator = ValueAnimator.ofInt(currentProgress, nextProgress)
        thumbAnimator?.addUpdateListener { animator ->
            val prog = animator.animatedValue as Int
            callProgressInternal(prog, true)
        }
        thumbAnimator?.interpolator = AccelerateDecelerateInterpolator()
        thumbAnimator?.duration = (450 * (Math.abs(currentProgress - nextProgress) / 100f)).toLong()
        thumbAnimator?.start()
    }

    /**
     * A wrapper listener dispatching [IStyledSliderListener] callbacks.
     */
    private val progressDelegateListener = WrapperListener()

    /**
     * Redirect the given [SeekBar.OnSeekBarChangeListener] to [progressDelegateListener].
     */
    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
        progressDelegateListener.actualListener = l
        super.setOnSeekBarChangeListener(if (l != null) progressDelegateListener else null)
    }

    /**
     * The [IStyledSliderListener] wrapper class.
     */
    private class WrapperListener(
        var actualListener: SeekBar.OnSeekBarChangeListener? = null
    ) : IStyledSliderListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            actualListener?.onProgressChanged(seekBar, progress, fromUser)
        }

        override fun onMarkerLevelUpdated(seekBar: SeekBar, markerLevel: Int) {
            val actual = actualListener
            if (actual is IStyledSliderListener) {
                actual.onMarkerLevelUpdated(seekBar, 0)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            actualListener?.onStartTrackingTouch(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            actualListener?.onStopTrackingTouch(seekBar)
        }
    }
}