package co.sodalabs.view

import android.widget.SeekBar

interface IStyledSliderListener : SeekBar.OnSeekBarChangeListener {

    fun onMarkerLevelUpdated(seekBar: SeekBar, markerLevel: Int)
}