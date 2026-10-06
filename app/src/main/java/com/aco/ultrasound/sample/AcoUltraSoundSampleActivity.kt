package com.aco.ultrasound.sample

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.lifecycle.lifecycleScope
import com.aco.ultrasound.AcoProbe
import com.aco.ultrasound.AcoProbeRoi
import com.aco.ultrasound.AcoUltrasound
import com.aco.ultrasound.AcoUltrasoundScanMode
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

    private var lastColorMapColors: IntArray? = null

    // The probe keeps its scan mode across sessions. A fresh scan session of this sample always
    // starts in B mode: if the probe reports another mode right after streaming starts, switch
    // it back once. Not set on rotation (streaming is already running then) or after the user
    // picked a mode themselves.
    private var resetToBModeOnStart = false

    private val scanModeCheckedListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        resetToBModeOnStart = false
        val mode = when (checkedId) {
            viewBinding.colorModeButton.id -> AcoUltrasoundScanMode.COLOR_MODE
            viewBinding.powerModeButton.id -> AcoUltrasoundScanMode.POWER_MODE
            else -> AcoUltrasoundScanMode.B_MODE
        }
        acoProbe.setScanMode(mode)
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
                lifecycleScope.launch(Dispatchers.Default) {
                    streamChannel.send(streamingData.bitmap)
                }
            }
            // The activity is recreated on rotation (to swap in layout-land); the probe keeps
            // streaming across that, so only start it when it isn't already running.
            if (acoProbe.streamingMutableLiveData.value != true) {
                resetToBModeOnStart = true
                acoProbe.streaming()
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            var frameCount = 0
            for (frame in streamChannel) {
                // You can choose either flipping method below to observe the difference in processing speed,
                // which directly affects the smoothness of the ultrasound stream.
                //val processedImage = slowFlip(frame)
                 //val processedImage = fastFlip(frame)
                withContext(Dispatchers.Main) {
                    viewBinding.imageView.setImageBitmap(frame)
                    if (++frameCount % 10 == 1) updateColorMapView()
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

        viewBinding.scanModeRadioGroup.setOnCheckedChangeListener(scanModeCheckedListener)

        viewBinding.setCfmGainButton.setOnClickListener {
            acoProbe.setCfmGain("${viewBinding.cfmGainEditText.text}".toInt())
        }
        viewBinding.setCfmScaleButton.setOnClickListener {
            acoProbe.setCfmScale("${viewBinding.cfmScaleEditText.text}".toInt())
        }
        viewBinding.setCfmBaseLineButton.setOnClickListener {
            acoProbe.setCfmBaseLine("${viewBinding.cfmBaseLineEditText.text}".toInt())
        }
        viewBinding.setCfmRoiButton.setOnClickListener {
            acoProbe.setCfmRoi(
                AcoProbeRoi(
                    x1 = "${viewBinding.cfmRoiX1EditText.text}".toFloatOrNull() ?: 0f,
                    y1 = "${viewBinding.cfmRoiY1EditText.text}".toFloatOrNull() ?: 0f,
                    x2 = "${viewBinding.cfmRoiX2EditText.text}".toFloatOrNull() ?: 0f,
                    y2 = "${viewBinding.cfmRoiY2EditText.text}".toFloatOrNull() ?: 0f,
                )
            )
        }

        acoProbe.observerScanModeChange(this) { mode ->
            if (resetToBModeOnStart && mode != AcoUltrasoundScanMode.B_MODE) {
                resetToBModeOnStart = false
                acoProbe.setScanMode(AcoUltrasoundScanMode.B_MODE)
            }
            // The probe keeps its scan mode across sessions, so it can change without a click
            // (e.g. already in Color when connecting): make the buttons follow it.
            val checkedId = when (mode) {
                AcoUltrasoundScanMode.COLOR_MODE -> viewBinding.colorModeButton.id
                AcoUltrasoundScanMode.POWER_MODE -> viewBinding.powerModeButton.id
                AcoUltrasoundScanMode.B_MODE -> viewBinding.bModeButton.id
                else -> -1
            }
            if (viewBinding.scanModeRadioGroup.checkedRadioButtonId != checkedId) {
                viewBinding.scanModeRadioGroup.setOnCheckedChangeListener(null)
                if (checkedId == -1) {
                    viewBinding.scanModeRadioGroup.clearCheck()
                } else {
                    viewBinding.scanModeRadioGroup.check(checkedId)
                }
                viewBinding.scanModeRadioGroup.setOnCheckedChangeListener(scanModeCheckedListener)
            }
            val isColor = mode == AcoUltrasoundScanMode.COLOR_MODE || mode == AcoUltrasoundScanMode.POWER_MODE

            val cfmVisibility = if (isColor) View.VISIBLE else View.GONE
            viewBinding.cfmSectionHeader.visibility = cfmVisibility
            viewBinding.cfmGainRow.visibility = cfmVisibility
            viewBinding.cfmScaleRow.visibility = cfmVisibility
            viewBinding.cfmBaseLineRow.visibility = cfmVisibility
            viewBinding.cfmRoiRow.visibility = cfmVisibility
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

            viewBinding.cfmGainHintText.text =
                "min: ${parameters.cfm.gain.min}, max: ${parameters.cfm.gain.max}, step: ${parameters.cfm.gain.step}"
            viewBinding.cfmScaleHintText.text =
                "min: ${parameters.cfm.scale.min}, max: ${parameters.cfm.scale.max}, step: ${parameters.cfm.scale.step}"
            viewBinding.cfmBaseLineHintText.text =
                "min: ${parameters.cfm.baseline.min}, max: ${parameters.cfm.baseline.max}, step: ${parameters.cfm.baseline.step}"
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

    /** Draws the SDK's current Color / Power Doppler color map at the image's top-right corner. */
    private fun updateColorMapView() {
        val colors = acoProbe.getCfmColorMapColors()
        if (colors == null || colors.isEmpty()) {
            lastColorMapColors = null
            viewBinding.colorMapView.visibility = View.GONE
            return
        }
        if (lastColorMapColors?.contentEquals(colors) == true) return
        lastColorMapColors = colors
        val pixels = IntArray(colors.size) {
            val c = colors[it]
            Color.rgb((c shr 16) and 0xFF, (c shr 8) and 0xFF, c and 0xFF)
        }
        viewBinding.colorMapView.setImageBitmap(
            Bitmap.createBitmap(pixels, 1, pixels.size, Bitmap.Config.ARGB_8888)
        )
        viewBinding.colorMapView.visibility = View.VISIBLE
    }

    private suspend fun slowFlip(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val copied = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        for (i in 0 until bitmap.width) {
            var start = 0
            var end = bitmap.height - 1
            while (start < end) {
                val c1 = copied[i, start]
                val c2 = copied[i, end]
                copied[i, start] = c2
                copied[i, end] = c1
                start++
                end--
            }
        }

        copied
    }

    private val matrix = Matrix().apply { preScale(1f, -1f) }
    private suspend fun fastFlip(bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }
}
