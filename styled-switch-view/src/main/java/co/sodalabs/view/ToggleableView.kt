package co.sodalabs.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

typealias OnToggleListener = (toggleableView: ToggleableView, isOn: Boolean) -> Unit

open class ToggleableView : View {

    protected var mWidth: Int = 0
    protected var mHeight: Int = 0

    /**
     * Boolean state of this Switch.
     */
    open var isOn: Boolean = false

    /**
     * Field to determine whether switch is mEnabled/disabled.
     */
    var mEnabled: Boolean = true

    /**
     * Listener used to dispatch switch events.
     */
    protected var onToggledListener: OnToggleListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
