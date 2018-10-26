package co.sodalabs.view

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import co.sodalabs.view.StyledRecyclerView.Companion.PREVIEW_LAYOUT_AS_GRID
import co.sodalabs.view.StyledRecyclerView.Companion.PREVIEW_LAYOUT_AS_LIST
import co.sodalabs.view.rv.R

/**
 * A custom [RecyclerView] supporting the style attribute [R.styleable.StyledRecyclerView].
 *
 * @see [R.attr.rvDividerDrawable] The drawable reference.
 * @see [R.attr.rvDividerMode] There are [DividerMode.SHOW_DIVIDER_BEGINNING],
 * [DividerMode.SHOW_DIVIDER_MIDDLE], [DividerMode.SHOW_DIVIDER_END] and [DividerMode.SHOW_DIVIDER_NONE].
 * @see [R.attr.rvPreviewLayout] There are [PREVIEW_LAYOUT_AS_LIST] and [PREVIEW_LAYOUT_AS_GRID].
 */
open class StyledRecyclerView : RecyclerView {

    companion object {

        /**
         * To preview the item displayed in a list in XML previewer.
         */
        @JvmStatic
        val PREVIEW_LAYOUT_AS_LIST = 0
        /**
         * To preview the item displayed in a grid in XML previewer.
         */
        @JvmStatic
        val PREVIEW_LAYOUT_AS_GRID = 1
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        decorateView(getOrientation(attrs), attrs)
    }

    private fun decorateView(
        orientation: Int,
        attrs: AttributeSet?
    ) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StyledRecyclerView, 0, 0)

        // Divider and the mode
        if (typedArray.hasValue(R.styleable.StyledRecyclerView_rvDividerDrawable)) {
            val dividerDrawable = typedArray.getDrawable(R.styleable.StyledRecyclerView_rvDividerDrawable)
            val dividerMode = typedArray.getInt(
                R.styleable.StyledRecyclerView_rvDividerMode,
                DividerMode.SHOW_DIVIDER_BEGINNING.or(
                    DividerMode.SHOW_DIVIDER_MIDDLE).or(
                    DividerMode.SHOW_DIVIDER_END))

            val decoration = DrawableDividerDecoration(
                dividerDrawable = dividerDrawable,
                dividerMode = dividerMode)
            addItemDecoration(decoration)
        }

        // Preview list item
        if (isInEditMode) {
            // In XML, you could add attribute "app:rvPreviewLayout=grid" to
            // display the items in a grid
            val layout = typedArray.getInt(R.styleable.StyledRecyclerView_rvPreviewLayout, PREVIEW_LAYOUT_AS_LIST)
            val layoutManager = when (layout) {
                PREVIEW_LAYOUT_AS_GRID -> GridLayoutManager(context, 3, orientation, false)
                else -> LinearLayoutManager(context, orientation, false)
            }

            this.layoutManager = layoutManager
        }

        typedArray.recycle()
    }

    private fun getOrientation(attrs: AttributeSet?): Int {
        val b = context.theme.obtainStyledAttributes(
            attrs,
            intArrayOf(android.R.attr.orientation), 0, 0)
        val orientation = b.getInt(0, RecyclerView.VERTICAL)
        b.recycle()
        return orientation
    }
}
