package co.sodalabs.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatSeekBar
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import co.sodalabs.view.slider.R

/**
 * A capsule track styled slider with flexible styled markers.
 *
 * @see [R.attr.thumbDrawable] The thumb drawable.
 * @see [R.attr.trackDrawable] The track (a.k.a progress, but only the background part) drawable.
 * @see [R.attr.markerDrawableMiddle] The marker (tick) drawable in the middle.
 * @see [R.attr.markerDrawableStart] The marker (tick) drawable at the start.
 * @see [R.attr.markerDrawableEnd] The marker (tick) drawable at the end.
 * @see [R.attr.markerNum] The amount of markers on the track. The markers are distributed evenly spaced.
 * @see [R.attr.touchDragSlop] A slop where the touch forms a drag if the move distance is over.
 */
class StyledMarkerSliderView : AppCompatSeekBar {

    private var markerNum = 5

    // Thumb
    private var thumbDrawable: Drawable? = null
        set(value) {
            value?.determineSelfCenterBound()
            field = value
        }
    private var thumbHalfWidth: Float = 0f
    /**
     * The starting x of the thumb (align with the center of the thumb)
     */
    private var thumbStartX: Float = 0f
    /**
     * The ending x of the thumb (align with the center of the thumb)
     */
    private var thumbEndX: Float = 0f
    private var thumbAnimator: ValueAnimator? = null

    // Track
    private var trackDrawable: Drawable?
        set(value) {
            // The setter will take care of the padding
            progressDrawable = value
        }
        get() {
            return progressDrawable
        }

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

    private var touchStartX: Float = 0f
    private var touchDragging: Boolean = false
    private var touchDragSlop: Float = context.resources.getDimension(co.sodalabs.view.R.dimen.default_touch_drag_slop)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // By default determinate
        isIndeterminate = false

        // Force original thumb null
        thumb = null

        initCommonProperties(attrs)
        initProperties(attrs)
    }

    private fun initCommonProperties(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StyledViewCommon, 0, 0)

        for (i in 0 until typedArray.indexCount) {
            when (typedArray.getIndex(i)) {
                R.styleable.StyledViewCommon_touchDragSlop -> touchDragSlop = typedArray.getDimension(R.styleable.StyledViewCommon_touchDragSlop, touchDragSlop)
            }
        }

        typedArray.recycle()
    }

    private fun initProperties(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StyledMarkerSliderView, 0, 0)

        thumbDrawable = ContextCompat.getDrawable(context, R.drawable.default_marker_slider_thumb)
        // Override the track drawable
        progressDrawable = ContextCompat.getDrawable(context, R.drawable.default_marker_slider_track)
        // Marker
        markerDrawableMiddle = ContextCompat.getDrawable(context, R.drawable.default_marker_slider_marker_middle)
        markerDrawableStart = ContextCompat.getDrawable(context, R.drawable.default_marker_slider_marker_start)
        markerDrawableEnd = ContextCompat.getDrawable(context, R.drawable.default_marker_slider_marker_end)

        for (i in 0 until typedArray.indexCount) {
            when (typedArray.getIndex(i)) {
                R.styleable.StyledMarkerSliderView_thumbDrawable -> thumbDrawable = typedArray.getCompatDrawable(context,
                    R.styleable.StyledMarkerSliderView_thumbDrawable)
                R.styleable.StyledMarkerSliderView_trackDrawable -> trackDrawable = typedArray.getCompatDrawable(context,
                    R.styleable.StyledMarkerSliderView_trackDrawable)
                R.styleable.StyledMarkerSliderView_markerNum -> markerNum = typedArray.getInt(R.styleable.StyledMarkerSliderView_markerNum, markerNum)
                R.styleable.StyledMarkerSliderView_markerDrawableMiddle -> markerDrawableMiddle = typedArray.getCompatDrawable(context,
                    R.styleable.StyledMarkerSliderView_markerDrawableMiddle)
                R.styleable.StyledMarkerSliderView_markerDrawableStart -> markerDrawableStart = typedArray.getCompatDrawable(context,
                    R.styleable.StyledMarkerSliderView_markerDrawableStart)
                R.styleable.StyledMarkerSliderView_markerDrawableEnd -> markerDrawableEnd = typedArray.getCompatDrawable(context,
                    R.styleable.StyledMarkerSliderView_markerDrawableEnd)
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
            thumbHalfWidth = (thumbDrawable?.intrinsicWidth?.toFloat() ?: 0f) / 2f
            thumbStartX = paddingLeft + thumbHalfWidth
            thumbEndX = width - paddingRight - thumbHalfWidth

            markerDistance = if (markerNum > 1) {
                (thumbEndX - thumbStartX) / (markerNum - 1)
            } else {
                0f
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawTrack(canvas)
        drawMarker(canvas)
        drawThumb(canvas)
    }

    private fun drawTrack(canvas: Canvas) {
        canvas.runSafely {
            translate(paddingLeft.toFloat(), paddingTop.toFloat())
            progressDrawable.draw(canvas)
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

    private fun drawThumb(canvas: Canvas) {
        val viewHeight = height.toFloat()
        val progress = this.progress.toFloat() / 100f
        val thumbX = progress * thumbEndX + (1f - progress) * thumbStartX

        canvas.runSafely {
            translate(thumbX, viewHeight / 2f)
            thumbDrawable?.draw(canvas)
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
                    touchDragging = Math.abs(event.x - touchStartX) > touchDragSlop
                }

                if (touchDragging) {
                    val x = if (event.x < thumbStartX) {
                        thumbStartX
                    } else {
                        if (event.x > thumbEndX) {
                            thumbEndX
                        } else {
                            event.x
                        }
                    }

                    progress = positionToIntProgress(x)
                }

                return true
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                touchDragging = false

                snapToClosestMarkerSmoothly(event.x)

                return true
            }

            else -> return false
        }
    }

    private fun positionToIntProgress(thumbX: Float): Int {
        return Math.round(100f * (thumbX - thumbStartX) / (thumbEndX - thumbStartX))
    }

    private fun snapToClosestMarkerSmoothly(touchX: Float) {
        var closestX = thumbStartX
        var closestDistance = Math.abs(touchX - closestX)
        for (i in 1 until markerNum) {
            val markerX = thumbStartX + i * markerDistance
            val distance = Math.abs(touchX - markerX)
            if (distance < closestDistance) {
                closestX = markerX
                closestDistance = distance
            }
        }

        val currentProgress = progress
        val nextProgress = positionToIntProgress(closestX)

        thumbAnimator?.cancel()
        thumbAnimator = ValueAnimator.ofInt(currentProgress, nextProgress)
        thumbAnimator?.addUpdateListener { animator ->
            progress = animator.animatedValue as Int
        }
        thumbAnimator?.interpolator = AccelerateDecelerateInterpolator()
        thumbAnimator?.duration = (450 * (Math.abs(currentProgress - nextProgress) / 100f)).toLong()
        thumbAnimator?.start()
    }
}