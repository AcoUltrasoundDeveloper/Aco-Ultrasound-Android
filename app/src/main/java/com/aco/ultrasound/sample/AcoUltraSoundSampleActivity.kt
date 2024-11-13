package com.aco.ultrasound.sample

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.aco.ultrasound.AcoProbe
import com.aco.ultrasound.AcoUltrasound
import com.aco.ultrasound.sample.databinding.ActivityAcoUltraSoundSampleBinding

/**
 * Created by mr.chihlungchen on 2024/7/14, Hey Yo Man!
 */
class AcoUltraSoundSampleActivity : ComponentActivity() {

    private lateinit var acoProbe: AcoProbe

    private val viewBinding: ActivityAcoUltraSoundSampleBinding by lazy {
        ActivityAcoUltraSoundSampleBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val connectedAcoProbe = AcoUltrasound.getConnectedProbe()
        if (connectedAcoProbe == null) {
            finish()
            return
        } else {
            acoProbe = connectedAcoProbe
        }

        acoProbe.also { acoProbe ->
            acoProbe.onStreamingListener = { streamingData ->
                viewBinding.imageView.setImageBitmap(streamingData.bitmap)
            }
            acoProbe.streaming()
        }

        viewBinding.setGainButton.setOnClickListener {
            acoProbe.setGain("${viewBinding.gainEditText.text}".toInt())
        }
        viewBinding.setDepthButton.setOnClickListener {
            acoProbe.setDepth("${viewBinding.depthEditText.text}".toInt())
        }
        viewBinding.setPresetButton.setOnClickListener {
            acoProbe.setPreset("${viewBinding.presetEditText.text}".toInt())
        }
        viewBinding.freezeButton.setOnClickListener {
            acoProbe.freeze()
        }
        viewBinding.unfreezeButton.setOnClickListener {
            acoProbe.unFreeze()
        }

        acoProbe.observerParametersChange(this) { parameters ->
            viewBinding.gainValueText.text = "${parameters.gain.value}"
            viewBinding.depthValueText.text = "${parameters.depth.value}"
            viewBinding.presetValueText.text = "${parameters.preset.value}"

            viewBinding.gainHintText.text =
                "min: ${parameters.gain.min}, max: ${parameters.gain.max}, step: ${parameters.gain.step}"
            viewBinding.depthHintText.text =
                "min: ${parameters.depth.min}, max: ${parameters.depth.max}, step: ${parameters.depth.step}"
            viewBinding.presetHintText.text =
                "min: ${parameters.preset.min}, max: ${parameters.preset.max}, menu: ${parameters.preset.menu}"

        }

        acoProbe.observerStatusChange(this) { status ->
            viewBinding.tisValueText.text = "${status.tis}"
            viewBinding.tibValueText.text = "${status.tib}"
            viewBinding.ticValueText.text = "${status.tic}"
            viewBinding.miValueText.text = "${status.mi}"
            viewBinding.temperatureValueText.text = "${status.temperature}"
            viewBinding.batteryLevelValueText.text = "${status.batteryLevel}%"
        }

        acoProbe.onErrorListener = { exception ->
            AlertDialog.Builder(this).setMessage(exception.message).show()
        }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    androidx.appcompat.app.AlertDialog.Builder(this@AcoUltraSoundSampleActivity)
                        .setMessage("Exit Scan?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            dialog.dismiss()
                            acoProbe.also { acoProbe ->
                                acoProbe.stop()
                            }
                            finish()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
        )
    }
}