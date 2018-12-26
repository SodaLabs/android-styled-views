package co.sodalabs.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import co.sodalabs.view.sw.R

@Suppress("unused", "MemberVisibilityCanBePrivate")
class StyledSwitchView : ToggleableView {

    private var thumbRadius: Float = 0f

    private val paint by lazy { Paint() }

    private val thumbBounds = RectF()
    private val textBounds = Rect()

    private val outlinePath = Path()
    private val outlineRectBound = RectF()

    private var labelTextSize: Float = resources.getDimension(R.dimen.styled_switch_default_text_size)
        set(value) {
            field = value

            paint.textSize = value

            postInvalidate()
        }

    /**
     * Typeface for Switch on/off labels.
     */
    var typeface: Typeface? = null
        set(typeface) {
            field = typeface
            paint.typeface = typeface
            postInvalidate()
        }

    /**
     * Background color for `on` state.
     */
    var backgroundColorOn: Int = Color.parseColor("#31657D")
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Background color for `off` state.
     */
    var backgroundColorOff: Int = Color.parseColor("#00415c")
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Thumb color for `on` state.
     */
    var thumbColorOn: Int = Color.WHITE
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Thumb color for `off` state.
     */
    var thumbColorOff: Int = Color.WHITE
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Label for `on` state.
     */
    var labelOn: String = "On"
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Label for `off` state.
     */
    var labelOff: String = "Off"
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Label color for `on` state.
     */
    var labelColorOn: Int = Color.parseColor("#D6E0E4")
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Label color for `off` state.
     */
    var labelColorOff: Int = Color.parseColor("#D6E0E4")
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Color for Switch disabled state
     */
    var colorDisabled: Int = Color.parseColor("#D3D3D3")
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Color-on for Switch border
     */
    var borderColorOn: Int = Color.parseColor("#33677C")
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Color-on for Switch border
     */
    var borderColorOff: Int = Color.parseColor("#33677C")
        set(value) {
            field = value
            postInvalidate()
        }

    /**
     * Width for Switch border. By default is 1dp.
     */
    var borderWidth: Float = 1f * resources.displayMetrics.density
        set(value) {
            field = value
            paint.strokeWidth = value
            postInvalidate()
        }

    private var thumbOnCenterX: Float = 0.toFloat()
    private var thumbOffCenterX: Float = 0.toFloat()
    private var thumbAnimator: ValueAnimator? = null

    private var touchStartX: Float = 0f
    private var touchStartTime: Long = 0
    private var touchDragging: Boolean = false
    private var touchDragSlop: Float = context.resources.getDimension(R.dimen.default_touch_drag_slop)

    private val alphaOn: Float
        get() {
            return (thumbBounds.centerX() - thumbOffCenterX) / (thumbOnCenterX - thumbOffCenterX)
        }

    /**
     * Boolean state of this Switch.
     */
    private var isActualOn: Boolean = false
    override val isOn: Boolean
        get() = isActualOn

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
        initProperties(attrs)
    }

    override fun setOnOff(value: Boolean, userTriggered: Boolean) {
        ensureUIThread()

        if (value != isActualOn) {
            isActualOn = value
            // FIXME: There is a inconsistent internal state and the presentation
            // FIXME: The programmatic change always overrides the animation.
            // if (thumbAnimator?.isStarted == false) {
            setupThumbBounds(thumbBounds, thumbOnCenterX, thumbOffCenterX, thumbRadius, value)
            // }

            onToggledListener?.invoke(this, value, userTriggered)

            postInvalidate()
        }
    }

    private fun initView() {
        paint.isAntiAlias = true
        paint.strokeJoin = Paint.Join.MITER
    }

    private fun initProperties(attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.StyledSwitchView, 0, 0)
        for (i in 0 until attributes.indexCount) {
            when (attributes.getIndex(i)) {
                R.styleable.StyledSwitchView_swOn -> setOnOff(attributes.getBoolean(R.styleable.StyledSwitchView_swOn, false), false)
                R.styleable.StyledSwitchView_swColorDisabled -> colorDisabled = attributes.getColor(R.styleable.StyledSwitchView_swColorDisabled,
                    Color.parseColor("#D3D3D3"))
                R.styleable.StyledSwitchView_swTextOn -> labelOn = attributes.getString(R.styleable.StyledSwitchView_swTextOn)
                R.styleable.StyledSwitchView_swTextOff -> labelOff = attributes.getString(R.styleable.StyledSwitchView_swTextOff)
                R.styleable.StyledSwitchView_swTextColorOn -> labelColorOn = attributes.getColor(R.styleable.StyledSwitchView_swTextColorOn, labelColorOn)
                R.styleable.StyledSwitchView_swTextColorOff -> labelColorOff = attributes.getColor(R.styleable.StyledSwitchView_swTextColorOff, labelColorOff)
                R.styleable.StyledSwitchView_swTextSize -> labelTextSize = attributes.getDimension(R.styleable.StyledSwitchView_swTextSize, labelTextSize)
                R.styleable.StyledSwitchView_swBorderColorOn -> borderColorOn = attributes.getColor(R.styleable.StyledSwitchView_swBorderColorOn, borderColorOn)
                R.styleable.StyledSwitchView_swBorderColorOff -> borderColorOff = attributes.getColor(R.styleable.StyledSwitchView_swBorderColorOff,
                    borderColorOff)
                R.styleable.StyledSwitchView_swBorderWidth -> borderWidth = attributes.getDimension(R.styleable.StyledSwitchView_swBorderWidth,
                    borderWidth)
                R.styleable.StyledSwitchView_swBackgroundColorOn -> backgroundColorOn = attributes.getColor(R.styleable.StyledSwitchView_swBackgroundColorOn,
                    backgroundColorOn)
                R.styleable.StyledSwitchView_swBackgroundColorOff -> backgroundColorOff = attributes.getColor(R.styleable.StyledSwitchView_swBackgroundColorOff,
                    backgroundColorOff)
                R.styleable.StyledSwitchView_swThumbColorOn -> thumbColorOn = attributes.getColor(R.styleable.StyledSwitchView_swThumbColorOn, thumbColorOn)
                R.styleable.StyledSwitchView_swThumbColorOff -> thumbColorOff = attributes.getColor(R.styleable.StyledSwitchView_swThumbColorOff, thumbColorOff)
                R.styleable.StyledSwitchView_swTouchDragSlop -> touchDragSlop = attributes.getDimension(R.styleable.StyledSwitchView_swTouchDragSlop,
                    touchDragSlop)
            }
        }
        attributes.recycle()
    }

    @SuppressLint("SwitchIntDef")
    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        val standardWidth = resources.getDimensionPixelSize(R.dimen.styled_switch_default_width)
        val standardHeight = resources.getDimensionPixelSize(R.dimen.styled_switch_default_height)

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec).toFloat()

        val doubleBorder = 2f * borderWidth
        val desiredWidth = when (widthMode) {
            View.MeasureSpec.EXACTLY -> widthSize
            else -> Math.min(standardWidth + doubleBorder, widthSize)
        }
        val desiredHeight = when (heightMode) {
            View.MeasureSpec.EXACTLY -> heightSize
            else -> Math.min(standardHeight + doubleBorder, heightSize)
        }
        val scale = Math.min(
            desiredWidth / standardWidth,
            desiredHeight / standardHeight)

        setMeasuredDimension(
            (scale * standardWidth).toInt(),
            (scale * standardHeight).toInt())
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed) {
            val doubleBorder = 2f * borderWidth
            val borderlessWidth = right - left - doubleBorder
            val borderlessHeight = bottom - top - doubleBorder

            outlineRectBound.set(borderWidth, borderWidth, width - borderWidth, height - borderWidth)
            val boundRadius = Math.min(outlineRectBound.width(), outlineRectBound.height()) / 2f

            val borderlessDiameter = Math.min(borderlessWidth, borderlessHeight)
            thumbRadius = borderlessDiameter / 2.88f

            // x-on and x-off
            thumbOnCenterX = outlineRectBound.right - boundRadius
            thumbOffCenterX = outlineRectBound.left + boundRadius

            // The thumbnail boundary
            setupThumbBounds(thumbBounds, thumbOnCenterX, thumbOffCenterX, thumbRadius, isOn)

            // The outline boundary
            outlinePath.run {
                reset()

                // + 2--------------3 +
                //  /                \
                // 1                  4
                //  \                /
                // + 6--------------5 +

                // (4/3)*tan(pi/8)
                val controlHandleLength = 0.552284749831f * boundRadius

                // point 1
                moveTo(outlineRectBound.left, outlineRectBound.centerY())
                // point 2
                cubicTo(
                    outlineRectBound.left, outlineRectBound.centerY() - controlHandleLength, // beginning control point
                    thumbOffCenterX - controlHandleLength, outlineRectBound.top, // end control point
                    thumbOffCenterX, outlineRectBound.top)
                // point 3
                lineTo(thumbOnCenterX, outlineRectBound.top)
                // point 4
                cubicTo(
                    thumbOnCenterX + controlHandleLength, outlineRectBound.top, // beginning control point
                    outlineRectBound.right, outlineRectBound.centerY() - controlHandleLength, // end control point
                    outlineRectBound.right, outlineRectBound.centerY())
                // point 5
                cubicTo(
                    outlineRectBound.right, outlineRectBound.centerY() + controlHandleLength, // beginning control point
                    thumbOnCenterX + controlHandleLength, outlineRectBound.bottom, // end control point
                    thumbOnCenterX, outlineRectBound.bottom)
                // point 6
                lineTo(thumbOffCenterX, outlineRectBound.bottom)
                // back to point 1
                cubicTo(
                    thumbOffCenterX - controlHandleLength, outlineRectBound.bottom, // beginning control point
                    outlineRectBound.left, outlineRectBound.centerY() + controlHandleLength, // end control point
                    outlineRectBound.left, outlineRectBound.centerY())
                close()
            }
        }
    }

    private fun setupThumbBounds(
        thumbBounds: RectF,
        thumbOnCenterX: Float,
        thumbOffCenterX: Float,
        thumbRadius: Float,
        isOn: Boolean
    ) {
        val thumbCenterX = if (isOn) thumbOnCenterX else thumbOffCenterX
        val thumbCenterY = outlineRectBound.centerY()
        thumbBounds.set(thumbCenterX - thumbRadius, thumbCenterY - thumbRadius, thumbCenterX + thumbRadius, thumbCenterY + thumbRadius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSwitchBackground(canvas)
        drawSwitchLabels(canvas)
        drawSwitchThumb(canvas)
    }

    private fun drawSwitchBackground(canvas: Canvas) {
        if (paint.strokeWidth != borderWidth) {
            paint.strokeWidth = borderWidth
        }

        canvas.run {
            // Background
            paint.style = Paint.Style.FILL
            paint.color = if (isEnabled) {
                Color.argb(
                    255,
                    ((1f - alphaOn) * Color.red(backgroundColorOff) + alphaOn * Color.red(backgroundColorOn)).toInt(),
                    ((1f - alphaOn) * Color.green(backgroundColorOff) + alphaOn * Color.green(backgroundColorOn)).toInt(),
                    ((1f - alphaOn) * Color.blue(backgroundColorOff) + alphaOn * Color.blue(backgroundColorOn)).toInt()
                )
            } else {
                colorDisabled
            }
            drawPath(outlinePath, paint)

            // Border
            paint.color = if (isEnabled) borderColorOn else colorDisabled
            paint.style = Paint.Style.STROKE
            drawPath(outlinePath, paint)
        }
    }

    private fun drawSwitchLabels(canvas: Canvas) {
        if (paint.textSize != labelTextSize) {
            paint.textSize = labelTextSize
        }

        canvas.run {
            paint.style = Paint.Style.FILL

            val halfSize = paint.measureText("N") / 2f

            // Label-on
            paint.color = Color.argb(alphaOn.xOneByte(), Color.red(thumbColorOn), Color.green(thumbColorOn), Color.blue(thumbColorOn))
            canvas.drawText(
                labelOn,
                thumbOffCenterX,
                outlineRectBound.centerY() + halfSize,
                paint)

            // Label-off
            paint.color = Color.argb((1f - alphaOn).xOneByte(), Color.red(thumbColorOff), Color.green(thumbColorOff), Color.blue(thumbColorOff))
            canvas.drawText(
                labelOff,
                thumbOnCenterX - paint.measureText(labelOff),
                outlineRectBound.centerY() + halfSize,
                paint)
        }
    }

    private fun drawSwitchThumb(canvas: Canvas) {

        // IMPORTANT: Do not try to enclose in if-else blocks. We need to draw both for smooth animation.

        paint.style = Paint.Style.FILL

        // Draw thumb for `on` state
        paint.color = Color.argb(
            255,
            ((1f - alphaOn) * Color.red(thumbColorOff) + alphaOn * Color.red(thumbColorOn)).toInt(),
            ((1f - alphaOn) * Color.green(thumbColorOff) + alphaOn * Color.green(thumbColorOn)).toInt(),
            ((1f - alphaOn) * Color.blue(thumbColorOff) + alphaOn * Color.blue(thumbColorOn)).toInt()
        )
        canvas.drawCircle(thumbBounds.centerX(), thumbBounds.centerY(), thumbRadius, paint)
    }

    override fun performClick(): Boolean {
        super.performClick()

        val newValue = !isOn
        animateThumbnail(newValue)
        setOnOff(newValue, userTriggered = true)

        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        // TODO: Support additive animation

        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartTime = System.currentTimeMillis()
                touchStartX = x
                touchDragging = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // Detect dragging
                if (touchDragging.not() && Math.abs(x - touchStartX) >= touchDragSlop) {
                    touchDragging = true
                }

                // Move the thumb next to the user touch iff the touch forms a dragging
                if (touchDragging) {
                    // Constraint x
                    val constrainedX = if (x < thumbOffCenterX) {
                        thumbOffCenterX
                    } else {
                        if (x > thumbOnCenterX) {
                            thumbOnCenterX
                        } else {
                            x
                        }
                    }
                    thumbBounds.set(constrainedX - thumbRadius, thumbBounds.top, constrainedX + thumbRadius, thumbBounds.bottom)
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_OUTSIDE,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                val endTime = System.currentTimeMillis()
                val span = endTime - touchStartTime
                if (span < 200) {
                    performClick()
                } else {
                    val newValue = x >= outlineRectBound.centerX()
                    animateThumbnail(newValue)
                    setOnOff(newValue, userTriggered = true)
                }
                invalidate()
                return true
            }

            else -> return super.onTouchEvent(event)
        }
    }

    private fun animateThumbnail(isOn: Boolean) {
        val currentX = thumbBounds.centerX()
        val nextX = if (isOn) thumbOnCenterX else thumbOffCenterX
        thumbAnimator = ValueAnimator.ofFloat(currentX, nextX)
        thumbAnimator?.run {
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                thumbBounds.set(value - thumbRadius, thumbBounds.top, value + thumbRadius, thumbBounds.bottom)
                invalidate()
            }
            interpolator = AccelerateDecelerateInterpolator()
            // TODO: Dynamic duration
            duration = 250
            start()
        }
    }

    private fun ensureUIThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw IllegalThreadStateException("Must run on the UI thread")
        }
    }
}