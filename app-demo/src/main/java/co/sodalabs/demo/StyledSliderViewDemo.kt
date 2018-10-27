package co.sodalabs.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import co.sodalabs.view.IStyledSliderListener
import kotlinx.android.synthetic.main.activity_demo_styled_slider.slMarker
import kotlinx.android.synthetic.main.activity_demo_styled_switch.btClose

class StyledSliderViewDemo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_demo_styled_slider)

        btClose.setOnClickListener {
            onBackPressed()
        }

        slMarker.setOnSeekBarChangeListener(object : IStyledSliderListener {
            override fun onMarkerLevelUpdated(seekBar: SeekBar, markerLevel: Int) {
                println("onMarkerLevelUpdated, markerLevel=$markerLevel")
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                println("onProgressChanged, progress=$progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                println("onStartTrackingTouch")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                println("onStopTrackingTouch")
            }
        })
    }
}