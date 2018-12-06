@file:Suppress("NOTHING_TO_INLINE")

package co.sodalabs.view

import android.graphics.Canvas

/**
 * @link https://github.com/android/android-ktx/blob/89ee2e1cde1e1b0226ed944b9abd55cee0f9b9d4/src/main/java/androidx/core/graphics/Canvas.kt
 */
fun Canvas.runSafely(
    lambda: Canvas.() -> Unit
) {
    val c = save()
    lambda.invoke(this)
    restoreToCount(c)
}