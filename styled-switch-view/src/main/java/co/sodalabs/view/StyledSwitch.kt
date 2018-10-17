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
     * Label for `on` state.
     */
    private var labelOn: String = "ON"
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Label for `off` state.
     */
    private var labelOff: String = "OFF"
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
        labelOn = "ON"
        labelOff = "OFF"
        mEnabled = true
        textSize = 12

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorOn = resources.getColor(R.color.colorOn, context.theme)
            colorBorder = colorOn
        } else {
            colorOn = ContextCompat.getColor(context, R.color.colorOn)
            colorBorder = colorOn
        }

        paint.isAntiAlias = true

        this.colorOff = Color.parseColor("#FFFFFF")
        this.colorDisabled = Color.parseColor("#D3D3D3")
    }

    private fun initProperties(attrs: AttributeSet?) {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.StyledSwitch, 0, 0)
        for (i in 0 until attributes.indexCount) {
            val attr = attributes.getIndex(i)
            when (attr) {
                R.styleable.StyledSwitch_on -> isOn = attributes.getBoolean(R.styleable.StyledSwitch_on, false)
                R.styleable.StyledSwitch_colorOff -> colorOff = attributes.getColor(R.styleable.StyledSwitch_colorOff, Color.parseColor("#FFFFFF"))
                R.styleable.StyledSwitch_colorBorder -> {
                    val color = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        resources.getColor(R.color.colorOn, context.theme)
                    } else {
                        ContextCompat.getColor(context, R.color.colorOn)
                    }
                    colorBorder = attributes.getColor(R.styleable.StyledSwitch_colorBorder, color)
                }
                R.styleable.StyledSwitch_colorOn -> {
                    val color = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        resources.getColor(R.color.colorOn, context.theme)
                    } else {
                        ContextCompat.getColor(context, R.color.colorOn)
                    }
                    colorOn = attributes.getColor(R.styleable.StyledSwitch_colorOn, color)
                }
                R.styleable.StyledSwitch_colorDisabled -> colorDisabled = attributes.getColor(R.styleable.StyledSwitch_colorOff, Color.parseColor("#D3D3D3"))
                R.styleable.StyledSwitch_textOff -> labelOff = attributes.getString(R.styleable.StyledSwitch_textOff)
                R.styleable.StyledSwitch_textOn -> labelOn = attributes.getString(R.styleable.StyledSwitch_textOn)
                R.styleable.StyledSwitch_android_textSize -> {
                    val defaultTextSize = (12f * resources.displayMetrics.scaledDensity).toInt()
                    textSize = attributes.getDimensionPixelSize(R.styleable.StyledSwitch_android_textSize, defaultTextSize)
                }
                R.styleable.StyledSwitch_android_enabled -> mEnabled = attributes.getBoolean(R.styleable.StyledSwitch_android_enabled, false)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSwitchBackground(paint, canvas)
        drawSwitchLabels(paint, canvas)
        drawSwitchThumb(paint, canvas)
    }

    private fun drawSwitchBackground(p: Paint, canvas: Canvas) {
        p.textSize = textSize.toFloat()
        p.color = if (isEnabled) colorBorder else colorDisabled
        canvas.run {
            drawArc(leftBgArc, 90f, 180f, false, p)
            drawArc(rightBgArc, 90f, -180f, false, p)
            drawRect(outerRadii.toFloat(), 0f, (mWidth - outerRadii).toFloat(), mHeight.toFloat(), p)
        }

        p.color = colorOff
        canvas.run {
            drawArc(leftFgArc, 90f, 180f, false, p)
            drawArc(rightFgArc, 90f, -180f, false, p)
            drawRect(outerRadii.toFloat(), (padding / 10).toFloat(), (mWidth - outerRadii).toFloat(), (mHeight - padding / 10).toFloat(), p)
        }

        var alpha = ((thumbBounds.centerX() - thumbOffCenterX) / (thumbOnCenterX - thumbOffCenterX) * 255).toInt()
        alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
        p.color = if (isEnabled) {
            Color.argb(alpha, Color.red(colorOn), Color.green(colorOn), Color.blue(colorOn))
        } else {
            Color.argb(alpha, Color.red(colorDisabled), Color.green(colorDisabled), Color.blue(colorDisabled))
        }

        canvas.run {
            drawArc(leftBgArc, 90f, 180f, false, p)
            drawArc(rightBgArc, 90f, -180f, false, p)
            drawRect(outerRadii.toFloat(), 0f, (mWidth - outerRadii).toFloat(), mHeight.toFloat(), p)
        }

        alpha = ((thumbOnCenterX - thumbBounds.centerX()) / (thumbOnCenterX - thumbOffCenterX) * 255).toInt()
        alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
        val offColor = Color.argb(alpha, Color.red(colorOff), Color.green(colorOff), Color.blue(colorOff))
        p.color = offColor

        canvas.run {
            drawArc(leftFgArc, 90f, 180f, false, p)
            drawArc(rightFgArc, 90f, -180f, false, p)
            drawRect(outerRadii.toFloat(), (padding / 10).toFloat(), (mWidth - outerRadii).toFloat(), (mHeight - padding / 10).toFloat(), p)
        }
    }

    private fun drawSwitchLabels(p: Paint, canvas: Canvas) {
        val textCenter = p.measureText("N") / 2
        if (isOn) {
            var alpha = ((mWidth.ushr(1) - thumbBounds.centerX()) / (mWidth.ushr(1) - thumbOffCenterX) * 255).toInt()
            alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
            p.color = Color.argb(alpha, Color.red(colorOn), Color.green(colorOn), Color.blue(colorOn))

            var centerX = (mWidth - padding - (padding + padding.ushr(1) + (thumbRadii shl 1))).ushr(1).toFloat()
            canvas.drawText(labelOff, (padding + padding.ushr(1)).toFloat() + (thumbRadii shl 1).toFloat() + centerX - p.measureText(labelOff) / 2, mHeight.ushr(1) + textCenter, p)

            alpha = ((thumbBounds.centerX() - mWidth.ushr(1)) / (thumbOnCenterX - mWidth.ushr(1)) * 255).toInt()
            alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha

            p.color = Color.argb(alpha, Color.red(colorOff), Color.green(colorOff), Color.blue(colorOff))

            val maxSize = mWidth - (padding shl 1) - (thumbRadii shl 1)

            centerX = (padding.ushr(1) + maxSize - padding).ushr(1).toFloat()
            canvas.drawText(labelOn, padding + centerX - p.measureText(labelOn) / 2, mHeight.ushr(1) + textCenter, p)
        } else {
            var alpha = ((thumbBounds.centerX() - mWidth.ushr(1)) / (thumbOnCenterX - mWidth.ushr(1)) * 255).toInt()
            alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha

            p.color = Color.argb(alpha, Color.red(colorOff), Color.green(colorOff), Color.blue(colorOff))

            val maxSize = mWidth - (padding shl 1) - (thumbRadii shl 1)
            var centerX = (padding.ushr(1) + maxSize - padding).ushr(1).toFloat()
            canvas.drawText(labelOn, padding + centerX - p.measureText(labelOn) / 2, mHeight.ushr(1) + textCenter, p)

            alpha = ((mWidth.ushr(1) - thumbBounds.centerX()) / (mWidth.ushr(1) - thumbOffCenterX) * 255).toInt()
            alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha
            p.color = if (isEnabled) {
                Color.argb(alpha, Color.red(colorOn), Color.green(colorOn), Color.blue(colorOn))
            } else {
                Color.argb(alpha, Color.red(colorDisabled), Color.green(colorDisabled), Color.blue(colorDisabled))
            }

            centerX = (mWidth - padding - (padding + padding.ushr(1) + (thumbRadii shl 1))).ushr(1).toFloat()
            canvas.drawText(labelOff, (padding + padding.ushr(1)).toFloat() + (thumbRadii shl 1).toFloat() + centerX - p.measureText(labelOff) / 2, mHeight.ushr(1) + textCenter, p)
        }
    }

    private fun drawSwitchThumb(p: Paint, canvas: Canvas) {
        var alpha = ((thumbBounds.centerX() - thumbOffCenterX) / (thumbOnCenterX - thumbOffCenterX) * 255).toInt()
        alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha

        p.color = Color.argb(alpha, Color.red(colorOff), Color.green(colorOff), Color.blue(colorOff))

        canvas.drawCircle(thumbBounds.centerX(), thumbBounds.centerY(), thumbRadii.toFloat(), p)

        alpha = ((thumbOnCenterX - thumbBounds.centerX()) / (thumbOnCenterX - thumbOffCenterX) * 255).toInt()
        alpha = if (alpha < 0) 0 else if (alpha > 255) 255 else alpha

        p.color = if (isEnabled) {
            Color.argb(alpha, Color.red(colorOn), Color.green(colorOn), Color.blue(colorOn))
        } else {
            Color.argb(alpha, Color.red(colorDisabled), Color.green(colorDisabled), Color.blue(colorDisabled))
        }
        canvas.drawCircle(thumbBounds.centerX(), thumbBounds.centerY(), thumbRadii.toFloat(), paint)
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
                                val value = animation.animatedValue as Float
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
                                val value = animation.animatedValue as Float
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
