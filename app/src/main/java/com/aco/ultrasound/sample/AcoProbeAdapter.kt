package com.aco.ultrasound.sample

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.aco.ultrasound.AcoProbe
import com.aco.ultrasound.AcoProbeConnectStatus
import com.aco.ultrasound.sample.databinding.ItemAcoProbeBinding

/**
 * Created by mr.chihlungchen on 2024/6/22, Hey Yo Man!
 */
class AcoProbeAdapter : RecyclerView.Adapter<AcoProbeViewHolder>() {

    var acoProbeList: MutableList<AcoProbe> = mutableListOf()
    var onStreamingClick: (AcoProbe) -> Unit = {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcoProbeViewHolder {
        return AcoProbeViewHolder(
            ItemAcoProbeBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            parent.findViewTreeLifecycleOwner()
        )
    }

    override fun getItemCount(): Int {
        return acoProbeList.size
    }

    override fun onBindViewHolder(viewHolder: AcoProbeViewHolder, position: Int) {
        val acoProbe: AcoProbe = acoProbeList[position]
        viewHolder.itemView.tag = position
        viewHolder.viewBinding.nameText.text = acoProbe.getConfig().model.identifier
        viewHolder.viewBinding.ssidText.text = acoProbe.getConfig().ssid
        viewHolder.viewBinding.macText.text = acoProbe.getConfig().mac
        viewHolder.viewBinding.connectButton.setOnClickListener {
            AlertDialog.Builder(viewHolder.itemView.context)
                .apply {
                    val editText = EditText(viewHolder.itemView.context)
                    setTitle("Key")
                    setView(editText)
                    setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        acoProbe.connect("${editText.text}")
                    }
                    setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                }
                .show()
        }
        viewHolder.viewBinding.disconnectButton.setOnClickListener {
            acoProbe.disconnect()
        }
        viewHolder.viewBinding.startScanButton.setOnClickListener {
            onStreamingClick(acoProbe)
        }
        viewHolder.viewBinding.stopScanButton.setOnClickListener {
            acoProbe.stop()
        }

        viewHolder.lifecycleOwner?.also { lifecycleOwner ->
            acoProbe.onErrorListener = { exception ->
                AlertDialog.Builder(viewHolder.itemView.context)
                    .setTitle("Exception")
                    .setMessage(exception.message)
                    .show()
            }
            acoProbe.observerConnectStatus(
                lifecycleOwner
            ) { acoProbeConnectStatus: AcoProbeConnectStatus ->
                when (acoProbeConnectStatus) {
                    AcoProbeConnectStatus.DISCONNECTED -> {
                        viewHolder.viewBinding.disconnectButton.visibility = View.GONE
                        viewHolder.viewBinding.connectButton.visibility = View.VISIBLE
                        viewHolder.viewBinding.startScanButton.visibility = View.GONE
                        viewHolder.viewBinding.stopScanButton.visibility = View.GONE
                        viewHolder.viewBinding.streamingLayout.visibility = View.GONE
                    }

                    AcoProbeConnectStatus.CONNECTED -> {
                        viewHolder.viewBinding.disconnectButton.visibility = View.VISIBLE
                        viewHolder.viewBinding.connectButton.visibility = View.GONE
                        viewHolder.viewBinding.streamingLayout.visibility = View.VISIBLE
                        viewHolder.viewBinding.startScanButton.visibility = View.VISIBLE
                        viewHolder.viewBinding.stopScanButton.visibility = View.GONE
                    }

                    AcoProbeConnectStatus.CONNECTING -> {
                        viewHolder.viewBinding.disconnectButton.visibility = View.GONE
                        viewHolder.viewBinding.connectButton.visibility = View.GONE
                        viewHolder.viewBinding.startScanButton.visibility = View.GONE
                        viewHolder.viewBinding.stopScanButton.visibility = View.GONE
                        viewHolder.viewBinding.streamingLayout.visibility = View.GONE
                    }

                    AcoProbeConnectStatus.UNAVAILABLE -> {
                        viewHolder.viewBinding.disconnectButton.visibility = View.GONE
                        viewHolder.viewBinding.connectButton.visibility = View.GONE
                        viewHolder.viewBinding.startScanButton.visibility = View.GONE
                        viewHolder.viewBinding.stopScanButton.visibility = View.GONE
                        viewHolder.viewBinding.streamingLayout.visibility = View.GONE
                    }
                }
                viewHolder.viewBinding.statusText.text = acoProbeConnectStatus.name
            }

            acoProbe.streamingMutableLiveData.observe(lifecycleOwner) { streaming ->
                if (streaming == true) {
                    viewHolder.viewBinding.startScanButton.visibility = View.GONE
                    viewHolder.viewBinding.stopScanButton.visibility = View.VISIBLE
                } else {
                    viewHolder.viewBinding.startScanButton.visibility = View.VISIBLE
                    viewHolder.viewBinding.stopScanButton.visibility = View.GONE
                }
            }
        }
    }
}