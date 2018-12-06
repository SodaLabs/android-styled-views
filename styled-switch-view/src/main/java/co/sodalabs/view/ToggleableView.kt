package co.sodalabs.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

typealias OnToggleListener = (toggleableView: ToggleableView, isOn: Boolean, userTriggered: Boolean) -> Unit

abstract class ToggleableView : View {

    /**
     * Boolean state of this Switch.
     */
    open val isOn: Boolean = false

    abstract fun setOnOff(value: Boolean, userTriggered: Boolean)

    /**
     * Listener used to dispatch switch events.
     */
    var onToggledListener: OnToggleListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
