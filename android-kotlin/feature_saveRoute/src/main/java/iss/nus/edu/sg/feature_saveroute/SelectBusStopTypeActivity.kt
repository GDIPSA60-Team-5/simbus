package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectBusStopTypeActivity : AppCompatActivity() {

    @Inject
    lateinit var routeController: RouteController

    private val selectBusStopLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, result.data)
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectbusstoptype)

        val sgStopsButton = findViewById<Button>(R.id.sgBusButton)
        val nusStopsButton = findViewById<Button>(R.id.nusBusButton)

        sgStopsButton.setOnClickListener {
            val intent = Intent(this, SelectBusStopActivity::class.java).apply {
                putExtra("BusStopType", "SG")
            }
            selectBusStopLauncher.launch(intent)
        }

        nusStopsButton.setOnClickListener {
            val intent = Intent(this, SelectBusStopActivity::class.java).apply {
                putExtra("BusStopType", "NUS")
            }
            selectBusStopLauncher.launch(intent)
        }
    }
}