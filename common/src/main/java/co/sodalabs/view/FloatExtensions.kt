package co.sodalabs.view

fun Float.toFloorInt(): Int {
    return Math.floor(this.toDouble()).toInt()
}

fun Float.toCeilInt(): Int {
    return Math.ceil(this.toDouble()).toInt()
}