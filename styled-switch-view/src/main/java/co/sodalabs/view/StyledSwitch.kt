package co.sodalabs.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

@Suppress("unused")
class StyledSwitch : ToggleableView {
    private var padding: Int = 0

    private var textSize: Int = 0
        set(value) {
            field = (value.toFloat() * resources.displayMetrics.scaledDensity).toInt()
            invalidate()
        }

    private var outerRadii: Int = 0
    private var thumbRadii: Int = 0

    private val paint by lazy { Paint() }

    private var startTime: Long = 0

    private var thumbBounds = RectF()

    private var leftBgArc = RectF()
    private var rightBgArc = RectF()

    private var leftFgArc = RectF()
    private var rightFgArc = RectF()

    /**
     * Typeface for Switch on/off labels.
     */
    var typeface: Typeface? = null
        set(typeface) {
            field = typeface
            paint.typeface = typeface
            invalidate()
        }

    /**
     * Color for `on` state.
     */
    private var colorOn: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Color for `off` state.
     */
    private var colorOff: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Background color for `on` state.
     */
    private var backgroundColorOn: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Background color for `off` state.
     */
    private var backgroundColorOff: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Thumb color for `on` state.
     */
    private var thumbColorOn: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Thumb color for `off` state.
     */
    private var thumbColorOff: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Label for `on` state.
     */
    private var labelOn: String = "On"
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Label for `off` state.
     */
    private var labelOff: String = "Off"
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Color for Switch disabled state
     */
    private var colorDisabled: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Color for Switch border
     */
    private var colorBorder: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private var thumbOnCenterX: Float = 0.toFloat()
    private var thumbOffCenterX: Float = 0.toFloat()

    /**
     * Boolean state of this Switch.
     */
    override var isOn: Boolean = false
        set(value) {
            field = value
            if (isOn) {
                thumbBounds.set((mWidth - padding - thumbRadii).toFloat(), padding.toFloat(), (mWidth - padding).toFloat(), (mHeight - padding).toFloat())
            } else {
                thumbBounds.set(padding.toFloat(), padding.toFloat(), (padding + thumbRadii).toFloat(), (mHeight - padding).toFloat())
            }
            invalidate()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
        initProperties(attrs)
    }

    private fun initView() {
        isOn = false
        labelOn = "On"
        labelOff = "Off"
        mEnabled = true
        textSize = 14

        paint.isAntiAlias = true

        colorOn = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            resources.getColor(R.color.colorOn, context.theme)
        } else {
            ContextCompat.getColor(context, R.color.colorOn)
        }
        colorBorder = colorOn
        thumbColorOn = colorOn
        colorOff = ContextCompat.getColor(context, android.R.color.white)
        colorDisabled = Color.parseColor("#D3D3D3")
    }

    private fun initProperties(attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.StyledSwitch, 0, 0)
        for (i in 0 until attributes.indexCount) {
            when (attributes.getIndex(i)) {
                R.styleable.StyledSwitch_on -> isOn = attributes.getBoolean(R.styleable.StyledSwitch_on, false)
                R.styleable.StyledSwitch_colorDisabled -> colorDisabled = attributes.getColor(R.styleable.StyledSwitch_colorOff, Color.parseColor("#D3D3D3"))
                R.styleable.StyledSwitch_textOff -> labelOff = attributes.getString(R.styleable.StyledSwitch_textOff)
                R.styleable.StyledSwitch_textOn -> labelOn = attributes.getString(R.styleable.StyledSwitch_textOn)
                R.styleable.StyledSwitch_android_textSize -> textSize = attributes.getDimensionPixelSize(R.styleable.StyledSwitch_android_textSize, 14)
                R.styleable.StyledSwitch_android_enabled -> mEnabled = attributes.getBoolean(R.styleable.StyledSwitch_android_enabled, false)
                R.styleable.StyledSwitch_colorBorder -> colorBorder = attributes.getColor(R.styleable.StyledSwitch_colorBorder, ContextCompat.getColor(context, R.color.colorOn))
                R.styleable.StyledSwitch_colorOn -> colorOn = attributes.getColor(R.styleable.StyledSwitch_colorOn, ContextCompat.getColor(context, R.color.colorOn))
                R.styleable.StyledSwitch_colorOff -> colorOff = attributes.getColor(R.styleable.StyledSwitch_colorOff, ContextCompat.getColor(context, android.R.color.white))
                R.styleable.StyledSwitch_backgroundColorOn -> backgroundColorOn = attributes.getColor(R.styleable.StyledSwitch_backgroundColorOn, ContextCompat.getColor(context, R.color.colorOn))
                R.styleable.StyledSwitch_backgroundColorOff -> backgroundColorOff = attributes.getColor(R.styleable.StyledSwitch_backgroundColorOff, ContextCompat.getColor(context, android.R.color.white))
                R.styleable.StyledSwitch_thumbColorOn -> thumbColorOn = attributes.getColor(R.styleable.StyledSwitch_thumbColorOn, ContextCompat.getColor(context, R.color.colorOn))
                R.styleable.StyledSwitch_thumbColorOff -> thumbColorOff = attributes.getColor(R.styleable.StyledSwitch_thumbColorOff, ContextCompat.getColor(context, android.R.color.white))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSwitchBackground(canvas)
        drawSwitchLabels(canvas)
        drawSwitchThumb(canvas)
    }

    private fun drawSwitchBackground(canvas: Canvas) {
        paint.textSize = textSize.toFloat()
        paint.color = if (isEnabled) colorBorder else colorDisabled
        canvas.run {
            drawArc(leftBgArc, 90f, 180f, false, paint)
            drawArc(rightBgArc, 90f, -180f, false, paint)
            drawRect(outerRadii.toFloat(), 0f, (mWidth - outerRadii).toFloat(), mHeight.toFloat(), paint)
        }

        paint.color = backgroundColorOff
        canvas.run {
            drawArc(leftFgArc, 90f, 180f, false, paint)
            drawArc(rightFgArc, 90f, -180f, false, paint)
            drawRect(outerRadii.toFloat(), (padding / 10).toFloat(), (mWidth - outerRadii).toFloat(), (mHeight - padding / 10).toFloat(), paint)
        }

        var alpha = ((thumbBounds.centerX() - thumbOffCenterX) / (thumbOnCenterX - thumbOffCenterX) * 255).toInt()
        alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
        paint.color = if (isEnabled) {
            Color.argb(alpha, Color.red(backgroundColorOn), Color.green(backgroundColorOn), Color.blue(backgroundColorOn))
        } else {
            Color.argb(alpha, Color.red(colorDisabled), Color.green(colorDisabled), Color.blue(colorDisabled))
        }

        canvas.run {
            drawArc(leftBgArc, 90f, 180f, false, paint)
            drawArc(rightBgArc, 90f, -180f, false, paint)
            drawRect(outerRadii.toFloat(), 0f, (mWidth - outerRadii).toFloat(), mHeight.toFloat(), paint)
        }

        alpha = ((thumbOnCenterX - thumbBounds.centerX()) / (thumbOnCenterX - thumbOffCenterX) * 255).toInt()
        alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
        paint.color = Color.argb(alpha, Color.red(backgroundColorOff), Color.green(backgroundColorOff), Color.blue(backgroundColorOff))

        canvas.run {
            drawArc(leftFgArc, 90f, 180f, false, paint)
            drawArc(rightFgArc, 90f, -180f, false, paint)
            drawRect(outerRadii.toFloat(), (padding / 10).toFloat(), (mWidth - outerRadii).toFloat(), (mHeight - padding / 10).toFloat(), paint)
        }
    }

    private fun drawSwitchLabels(canvas: Canvas) {
        val textCenter = paint.measureText("N") / 2
        if (isOn) {
            var alpha = ((mWidth.ushr(1) - thumbBounds.centerX()) / (mWidth.ushr(1) - thumbOffCenterX) * 255).toInt()
            alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
            paint.color = Color.argb(alpha, Color.red(thumbColorOn), Color.green(thumbColorOn), Color.blue(thumbColorOn))

            var centerX = (mWidth - padding - (padding + padding.ushr(1) + (thumbRadii shl 1))).ushr(1).toFloat()
            canvas.drawText(labelOff, (padding + padding.ushr(1)).toFloat() + (thumbRadii shl 1).toFloat() + centerX - paint.measureText(labelOff) / 2, mHeight.ushr(1) + textCenter, paint)

            alpha = ((thumbBounds.centerX() - mWidth.ushr(1)) / (thumbOnCenterX - mWidth.ushr(1)) * 255).toInt()
            alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha

            paint.color = Color.argb(alpha, Color.red(thumbColorOff), Color.green(thumbColorOff), Color.blue(thumbColorOff))

            val maxSize = mWidth - (padding shl 1) - (thumbRadii shl 1)

            centerX = (padding.ushr(1) + maxSize - padding).ushr(1).toFloat()
            canvas.drawText(labelOn, padding + centerX - paint.measureText(labelOn) / 2, mHeight.ushr(1) + textCenter, paint)
        } else {
            var alpha = ((thumbBounds.centerX() - mWidth.ushr(1)) / (thumbOnCenterX - mWidth.ushr(1)) * 255).toInt()
            alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha

            paint.color = Color.argb(alpha, Color.red(thumbColorOff), Color.green(thumbColorOff), Color.blue(thumbColorOff))

            val maxSize = mWidth - (padding shl 1) - (thumbRadii shl 1)
            var centerX = (padding.ushr(1) + maxSize - padding).ushr(1).toFloat()
            canvas.drawText(labelOn, padding + centerX - paint.measureText(labelOn) / 2, mHeight.ushr(1) + textCenter, paint)

            alpha = ((mWidth.ushr(1) - thumbBounds.centerX()) / (mWidth.ushr(1) - thumbOffCenterX) * 255).toInt()
            alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
            paint.color = if (isEnabled) {
                Color.argb(alpha, Color.red(thumbColorOn), Color.green(thumbColorOn), Color.blue(thumbColorOn))
            } else {
                Color.argb(alpha, Color.red(colorDisabled), Color.green(colorDisabled), Color.blue(colorDisabled))
            }

            centerX = (mWidth - padding - (padding + padding.ushr(1) + (thumbRadii shl 1))).ushr(1).toFloat()
            canvas.drawText(labelOff, (padding + padding.ushr(1)).toFloat() + (thumbRadii shl 1).toFloat() + centerX - paint.measureText(labelOff) / 2, mHeight.ushr(1) + textCenter, paint)
        }
    }

    private fun drawSwitchThumb(canvas: Canvas) {

        // IMPORTANT: Do not try to enclose in if-else blocks. We need to draw both for smooth animation.

        // Draw thumb for `on` state
        var alpha = ((thumbBounds.centerX() - thumbOffCenterX) / (thumbOnCenterX - thumbOffCenterX) * 255).toInt()
        alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
        paint.color = Color.argb(alpha, Color.red(thumbColorOff), Color.green(thumbColorOff), Color.blue(thumbColorOff))
        canvas.drawCircle(thumbBounds.centerX(), thumbBounds.centerY(), thumbRadii.toFloat(), paint)

        // Draw thumb for `off` state
        alpha = ((thumbOnCenterX - thumbBounds.centerX()) / (thumbOnCenterX - thumbOffCenterX) * 255).toInt()
        alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
        paint.color = if (isEnabled) {
            Color.argb(alpha, Color.red(thumbColorOn), Color.green(thumbColorOn), Color.blue(thumbColorOn))
        } else {
            Color.argb(alpha, Color.red(colorDisabled), Color.green(colorDisabled), Color.blue(colorDisabled))
        }
        canvas.drawCircle(thumbBounds.centerX(), thumbBounds.centerY(), thumbRadii.toFloat(), this.paint)

    }

    @SuppressLint("SwitchIntDef")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = resources.getDimensionPixelSize(R.dimen.styled_switch_default_width)
        val desiredHeight = resources.getDimensionPixelSize(R.dimen.styled_switch_default_height)

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        mWidth = when (widthMode) {
            View.MeasureSpec.EXACTLY -> widthSize
            View.MeasureSpec.AT_MOST -> Math.min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        mHeight = when (heightMode) {
            View.MeasureSpec.EXACTLY -> heightSize
            View.MeasureSpec.AT_MOST -> Math.min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(mWidth, mHeight)

        outerRadii = Math.min(mWidth, mHeight).ushr(1)
        thumbRadii = (Math.min(mWidth, mHeight) / 2.88f).toInt()
        padding = (mHeight - thumbRadii).ushr(1)

        thumbBounds.set((mWidth - padding - thumbRadii).toFloat(), padding.toFloat(), (mWidth - padding).toFloat(), (mHeight - padding).toFloat())
        thumbOnCenterX = thumbBounds.centerX()

        thumbBounds.set(padding.toFloat(), padding.toFloat(), (padding + thumbRadii).toFloat(), (mHeight - padding).toFloat())
        thumbOffCenterX = thumbBounds.centerX()

        if (isOn) {
            thumbBounds.set((mWidth - padding - thumbRadii).toFloat(), padding.toFloat(), (mWidth - padding).toFloat(), (mHeight - padding).toFloat())
        } else {
            thumbBounds.set(padding.toFloat(), padding.toFloat(), (padding + thumbRadii).toFloat(), (mHeight - padding).toFloat())
        }

        leftBgArc.set(0f, 0f, (outerRadii shl 1).toFloat(), mHeight.toFloat())
        rightBgArc.set((mWidth - (outerRadii shl 1)).toFloat(), 0f, mWidth.toFloat(), mHeight.toFloat())

        leftFgArc.set((padding / 10).toFloat(), (padding / 10).toFloat(), ((outerRadii shl 1) - padding / 10).toFloat(), (mHeight - padding / 10).toFloat())
        rightFgArc.set((mWidth - (outerRadii shl 1) + padding / 10).toFloat(), (padding / 10).toFloat(), (mWidth - padding / 10).toFloat(), (mHeight - padding / 10).toFloat())
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (isOn) {
            val switchColor = ValueAnimator.ofInt(mWidth - padding - thumbRadii, padding)
            switchColor.run {
                addUpdateListener { animation ->
                    val value = (animation.animatedValue as Int).toFloat()
                    thumbBounds.set(value, thumbBounds.top, value + thumbRadii, thumbBounds.bottom)
                    invalidate()
                }
                interpolator = AccelerateDecelerateInterpolator()
                duration = 250
                start()
            }
        } else {
            val switchColor = ValueAnimator.ofInt(padding, mWidth - padding - thumbRadii)
            switchColor.run {
                addUpdateListener { animation ->
                    val value = (animation.animatedValue as Int).toFloat()
                    thumbBounds.set(value, thumbBounds.top, value + thumbRadii, thumbBounds.bottom)
                    invalidate()
                }
                interpolator = AccelerateDecelerateInterpolator()
                duration = 250
                start()
            }
        }
        isOn = !isOn
        onToggledListener?.invoke(this, isOn)
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startTime = System.currentTimeMillis()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (x - thumbRadii.ushr(1) > padding && x + thumbRadii.ushr(1) < mWidth - padding) {
                    thumbBounds.set(x - thumbRadii.ushr(1), thumbBounds.top, x + thumbRadii.ushr(1), thumbBounds.bottom)
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val endTime = System.currentTimeMillis()
                val span = endTime - startTime
                if (span < 200) {
                    performClick()
                } else {
                    if (x >= mWidth.ushr(1)) {
                        val switchColor = ValueAnimator.ofInt(if (x > mWidth - padding - thumbRadii) mWidth - padding - thumbRadii else x.toInt(), mWidth - padding - thumbRadii)
                        switchColor.run {
                            addUpdateListener { animation ->
                                val value = (animation.animatedValue as Int).toFloat()
                                thumbBounds.set(value, thumbBounds.top, value + thumbRadii, thumbBounds.bottom)
                                invalidate()
                            }
                            interpolator = AccelerateDecelerateInterpolator()
                            duration = 250
                            start()
                        }
                        isOn = true
                    } else {
                        val switchColor = ValueAnimator.ofInt(if (x < padding) padding else x.toInt(), padding)
                        switchColor.run {
                            addUpdateListener { animation ->
                                val value = (animation.animatedValue as Int).toFloat()
                                thumbBounds.set(value, thumbBounds.top, value + thumbRadii, thumbBounds.bottom)
                                invalidate()
                            }
                            interpolator = AccelerateDecelerateInterpolator()
                            duration = 250
                            start()
                        }
                        isOn = false
                    }
                    onToggledListener?.invoke(this, isOn)
                }
                invalidate()
                return true
            }

            else -> return super.onTouchEvent(event)
        }
    }
}
