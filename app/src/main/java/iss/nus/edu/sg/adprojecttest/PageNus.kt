package iss.nus.edu.sg.adprojecttest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PageNus: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nus_page)

        val selectedNusBusStop = findViewById<EditText>(R.id.BusStopNameEdit)
        val selectedNusBusService = findViewById<EditText>(R.id.BusStopNumberEdit)
        val submitBtn = findViewById<Button>(R.id.NusSubmitButton)
        val nusResponse = findViewById<TextView>(R.id.Responsetextview)

        submitBtn.setOnClickListener {
            val busStop = selectedNusBusStop.text.toString()
            val busService = selectedNusBusService.text.toString()
            val URL = "http://10.0.2.2:8080/nusbus?busStopName=$busStop&busService=$busService"
            val client = OkHttpClient()
            val request = Request.Builder().url(URL).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread { nusResponse.text = "Failed: ${e.message}" }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.use { responseBody ->
                        val json = responseBody.string()
                        val jsonObject = JSONObject(json)

                        val serviceName = jsonObject.getString("serviceName")
                        val arrivalMinutes = jsonObject.getString("arrivalTime").toIntOrNull()
                        val nextArrivalMinutes = jsonObject.getString("nextArrivalTime").toIntOrNull()
                        val format = DateTimeFormatter.ofPattern("HH:mm")

                        val now = ZonedDateTime.now()
                        val arrivalTime = arrivalMinutes?.let {now.plusMinutes(it.toLong())}
                        val nextArrivalTime = nextArrivalMinutes?.let{now.plusMinutes(it.toLong())}

                        runOnUiThread {
                            if (arrivalTime != null) {
                                nusResponse.text =
                                    "Bus $serviceName arrives at ${arrivalTime.toLocalTime().format(format)}. Next bus arrives at ${nextArrivalTime?.toLocalTime()?.format(format)}"
                            } else {
                                nusResponse.text = "No bus found 10+ mins away"
                            }
                        }
                    }
                }
            })


        }

    }

}