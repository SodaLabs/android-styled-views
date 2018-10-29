package co.sodalabs.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.support.v7.content.res.AppCompatResources

fun TypedArray.getCompatDrawable(
    context: Context,
    stylableAttr: Int
): Drawable? {
    val resID = getResourceId(stylableAttr, -1)
    return if (resID == -1) {
        null
    } else {
        AppCompatResources.getDrawable(context, resID)
    }
}