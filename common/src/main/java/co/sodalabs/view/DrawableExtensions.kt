package co.sodalabs.view

import android.graphics.drawable.Drawable

fun Drawable.determineSelfCenterBound() {
    val hw = intrinsicWidth.toFloat() / 2f
    val hh = intrinsicHeight.toFloat() / 2f
    this.setBounds(-(hw.toFloorInt()), -(hh.toFloorInt()), hw.toCeilInt(), hh.toCeilInt())
}