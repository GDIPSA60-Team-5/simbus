package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SelectBusStopTypeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selectbusstoptype)

        val sgStopsButton = findViewById<Button>(R.id.sgBusButton)
        val nusStopsButton = findViewById<Button>(R.id.nusBusButton)

        sgStopsButton.setOnClickListener {
            val intent = Intent(this, SelectBusStopActivity::class.java)
            intent.putExtra("BusStopType", "SG")
            startActivityForResult(intent, 100)
        }
        nusStopsButton.setOnClickListener {
            val intent = Intent(this, SelectBusStopActivity::class.java)
            intent.putExtra("BusStopType", "NUS")
            startActivityForResult(intent, 100)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

}