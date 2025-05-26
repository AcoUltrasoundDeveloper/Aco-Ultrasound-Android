package com.aco.ultrasound.sample

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.lifecycle.lifecycleScope
import com.aco.ultrasound.AcoProbe
import com.aco.ultrasound.AcoUltrasound
import com.aco.ultrasound.sample.databinding.ActivityAcoUltraSoundSampleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by mr.chihlungchen on 2024/7/14, Hey Yo Man!
 */
class AcoUltraSoundSampleActivity : ComponentActivity() {

    private lateinit var acoProbe: AcoProbe

    private val viewBinding: ActivityAcoUltraSoundSampleBinding by lazy {
        ActivityAcoUltraSoundSampleBinding.inflate(layoutInflater)
    }

    private val streamChannel = Channel<Bitmap>(Channel.CONFLATED)

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
                lifecycleScope.launch(Dispatchers.Default) {
                    streamChannel.send(streamingData.bitmap)
                }
            }
            acoProbe.streaming()
        }

        lifecycleScope.launch(Dispatchers.Default) {
            for (frame in streamChannel) {
                // You can choose either flipping method below to observe the difference in processing speed,
                // which directly affects the smoothness of the ultrasound stream.
                //val processedImage = slowFlip(frame)
                 //val processedImage = fastFlip(frame)
                withContext(Dispatchers.Main) {
                    viewBinding.imageView.setImageBitmap(frame)
                }
            }
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
