package com.floatscroll

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.app.Activity

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SettingsStore.init(this)

        setupScrollDistanceSlider()
        setupOpacitySlider()
        setupSizeSelector()
        setupAccessibilityButton()
    }

    private fun setupScrollDistanceSlider() {
        val seekBar = findViewById<SeekBar>(R.id.seekbar_scroll_distance)
        val label = findViewById<TextView>(R.id.label_scroll_distance_value)

        // SeekBar range: 0 to 1300 maps to 200–1500
        val currentDistance = SettingsStore.scrollDistance
        seekBar.max = 1300
        seekBar.progress = currentDistance - 200
        label.text = "${currentDistance}px"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val distance = progress + 200
                label.text = "${distance}px"
                if (fromUser) SettingsStore.scrollDistance = distance
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupOpacitySlider() {
        val seekBar = findViewById<SeekBar>(R.id.seekbar_opacity)
        val label = findViewById<TextView>(R.id.label_opacity_value)

        // SeekBar range: 0 to 80 maps to 20%–100%
        val currentOpacity = (SettingsStore.buttonOpacity * 100).toInt()
        seekBar.max = 80
        seekBar.progress = currentOpacity - 20
        label.text = "${currentOpacity}%"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val percent = progress + 20
                label.text = "${percent}%"
                if (fromUser) SettingsStore.buttonOpacity = percent / 100f
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupSizeSelector() {
        val radioGroup = findViewById<RadioGroup>(R.id.radio_group_size)

        val checkedId = when (SettingsStore.buttonSize) {
            "small" -> R.id.radio_small
            "large" -> R.id.radio_large
            else -> R.id.radio_medium
        }
        radioGroup.check(checkedId)

        radioGroup.setOnCheckedChangeListener { _, id ->
            SettingsStore.buttonSize = when (id) {
                R.id.radio_small -> "small"
                R.id.radio_large -> "large"
                else -> "medium"
            }
        }
    }

    private fun setupAccessibilityButton() {
        findViewById<Button>(R.id.btn_open_accessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}
