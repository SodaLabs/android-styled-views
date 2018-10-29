package co.sodalabs.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

typealias OnToggleListener = (toggleableView: ToggleableView, isOn: Boolean) -> Unit

open class ToggleableView : View {

    /**
     * Boolean state of this Switch.
     */
    open var isOn: Boolean = false

    /**
     * Listener used to dispatch switch events.
     */
    var onToggledListener: OnToggleListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
