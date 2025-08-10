package iss.nus.edu.sg.adprojecttest

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotifChannel()
        requestNotificationPermission()

        val busStopCode = findViewById<EditText>(R.id.busstopedittext)
        val busNo = findViewById<EditText>(R.id.busnoet)
        val btn = findViewById<Button>(R.id.submitbtn)
        val resp = findViewById<TextView>(R.id.responseText)

        btn.setOnClickListener {
            var stopCode = busStopCode.text.toString()
            var busNumber = busNo.text.toString()
            val client = OkHttpClient()
            var url = "http://10.0.2.2:8080/sgbus?busStopCode=$stopCode&busNumber=$busNumber"
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread { resp.text = "Failed: ${e.message}" }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.use { responseBody ->
                        val json = responseBody.string()
                        val jsonObject = JSONObject(json)

                        val serviceNo = jsonObject.getString("serviceNo")
                        val arrivalsArray = jsonObject.getJSONArray("estimatedArrivals")

                        val formatter = DateTimeFormatter.ISO_DATE_TIME
                        val now = ZonedDateTime.now()
                        var chosenArrival: ZonedDateTime? = null

                        for (i in 0 until arrivalsArray.length()) {
                            val arrivalTime = ZonedDateTime.parse(arrivalsArray.getString(i), formatter)
                            val minutesAway = java.time.Duration.between(now, arrivalTime).toMinutes()
                            if (minutesAway >= 10) {
                                chosenArrival = arrivalTime
                                break
                            }
                        }

                        runOnUiThread {
                            if (chosenArrival != null) {
                                resp.text =
                                    "Bus $serviceNo arrives at ${chosenArrival.toLocalTime()} (>=10 mins)"
                                scheduleBusNotification(serviceNo, arrivalsArray)
                            } else {
                                resp.text = "No bus found 10+ mins away"
                            }
                        }
                    }
                }
            })
        }
        val testBtn = findViewById<Button>(R.id.testNotificationBtn)

        testBtn.setOnClickListener {
            val intent = Intent(this, BusAlertReceiver::class.java).apply {
                putExtra("serviceNo", "TEST123")
            }
            val uniqueId = System.currentTimeMillis().toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                uniqueId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = System.currentTimeMillis() + 5000

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

            resp.text = "Test notification scheduled in 5 seconds"
        }
    }


    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "bus_alerts",
                "Bus Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming buses"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun scheduleBusNotification(serviceNo: String, arrivalsArray: JSONArray) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + 10000

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val intent = Intent(this, BusAlertReceiver::class.java).apply {
            putExtra("serviceNo", serviceNo)
            putExtra("arrivals", arrivalsArray.toString())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

    }
}