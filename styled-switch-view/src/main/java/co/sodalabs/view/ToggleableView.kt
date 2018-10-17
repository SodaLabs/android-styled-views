package co.sodalabs.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

open class ToggleableView : View {

    protected var mWidth: Int = 0
    protected var mHeight: Int = 0

    /**
     *
     * Boolean state of this Switch.
     *
     */
    open var isOn: Boolean = false

    /**
     * Field to determine whether switch is mEnabled/disabled.
     *
     * @see .isEnabled
     * @see .setMEnabled
     */
    var mEnabled: Boolean = false

    /**
     * Listener used to dispatch switch events.
     *
     * @see .setOnToggledListener
     */
    protected var onToggledListener: OnToggleListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
