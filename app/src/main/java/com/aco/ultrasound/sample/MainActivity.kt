package com.aco.ultrasound.sample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.aco.ultrasound.AcoProbe
import com.aco.ultrasound.AcoProbeConnectMethod
import com.aco.ultrasound.AcoProbeDiscoveredListener
import com.aco.ultrasound.AcoUltrasound
import com.aco.ultrasound.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val viewBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val acoProbeAdapter: AcoProbeAdapter = AcoProbeAdapter().apply {
        onStreamingClick = { acoProbe ->
            AcoUltrasound.stopDiscoverProbes()
            val intent = Intent(this@MainActivity, AcoUltraSoundSampleActivity::class.java)
            startActivity(intent)
        }
    }

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private val requiredPermissionArray: Array<String> by lazy {
        val basePermissions = arrayOf(
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        )

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            basePermissions + Manifest.permission.NEARBY_WIFI_DEVICES
        } else {
            basePermissions
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        viewBinding.recyclerView.adapter = acoProbeAdapter
        viewBinding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        viewBinding.startFindProbeButton.setOnClickListener {
            if (AcoUltrasound.isDiscoveringProbes()) {
                AcoUltrasound.stopDiscoverProbes()
                viewBinding.startFindProbeButton.text = "START FIND PROBES"
            } else {
                locationPermissionRequest.launch(requiredPermissionArray)
            }
        }

        Log.e("application", "${application}")

        AcoUltrasound.initialize(application, AcoProbeConnectMethod.WIFI_DIRECT)

        AcoUltrasound.registerProbeDiscoveredListener(object : AcoProbeDiscoveredListener {
            override fun onDiscovered(acoProbeList: List<AcoProbe>) {
                acoProbeAdapter.acoProbeList.clear()
                acoProbeAdapter.acoProbeList.addAll(acoProbeList)
                acoProbeAdapter.notifyDataSetChanged()
            }
        })

        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            for (permission: String in it.keys) {
                Log.e(javaClass.simpleName, "${permission}: ${it[permission]}")
                if (it[permission] == false) {
                    return@registerForActivityResult
                }
            }
            AcoUltrasound.startDiscoverProbes()
            viewBinding.startFindProbeButton.text = "FINDING...(CLICK TO STOP)"
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@MainActivity).setMessage("Exit App?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        })
    }

    override fun onDestroy() {
        AcoUltrasound.destroy()
        super.onDestroy()
    }
}
