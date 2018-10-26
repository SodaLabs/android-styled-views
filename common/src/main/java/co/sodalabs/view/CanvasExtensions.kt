package co.sodalabs.view

import android.graphics.Canvas

fun Canvas.runSafely(
    lambda: Canvas.() -> Unit
) {
    val c = save()
    lambda()
    restoreToCount(c)
}
