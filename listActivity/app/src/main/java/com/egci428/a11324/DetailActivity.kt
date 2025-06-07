package com.egci428.a11324

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listactivity.R
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class DetailActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lastUpdate: Long = 0
    private lateinit var imageView: ImageView
    private lateinit var saveButton: FloatingActionButton
    private lateinit var messageTextView: TextView
    private lateinit var textView2: TextView
    private lateinit var textView3: TextView
    private lateinit var backButton: ImageView
    private var currentFortune: FortuneCookie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Initialize views safely
        imageView = findViewById(R.id.imageView4) ?: throw RuntimeException("imageView4 not found!")
        saveButton = findViewById(R.id.saveButton) as FloatingActionButton
        messageTextView = findViewById(R.id.messageTextView) ?: throw RuntimeException("messageTextView not found!")
        textView2 = findViewById(R.id.textView2)
        textView3 = findViewById(R.id.textView3)

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        imageView.setImageResource(R.drawable.closedcookies)
        saveButton.isEnabled = false

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lastUpdate = System.currentTimeMillis()

        saveButton.setOnClickListener {
            currentFortune?.let { cookie ->
                DataProvider.addFortune(cookie, this)
            }
            finish()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event)
        }
    }

    private fun getAccelerometer(event: SensorEvent) {
        val values = event.values
        val x = values[0]
        val y = values[1]
        val z = values[2]
        val accel = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH)

        val actualTime = System.currentTimeMillis()
        if (accel >= 2) {
            if (actualTime - lastUpdate < 200) {
                return
            }
            lastUpdate = actualTime
            handleShake()
        }
    }

    private fun handleShake() {
        Toast.makeText(this, "Waiting", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val fortune = withContext(Dispatchers.IO) {
                    fetchFortuneFromApi()
                }
                currentFortune = fortune
                runOnUiThread {
                    imageView.setImageResource(R.drawable.openedcookies)
                    messageTextView.text = fortune.message
                    textView2.text = fortune.message
                    textView3.text = "Date: " + fortune.time

                    // Update color determination based on status
                    textView2.setTextColor(
                        if (fortune.status == "positive") Color.parseColor("#FFA500") else Color.BLUE
                    )

                    saveButton.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@DetailActivity,
                        "Error fetching fortune: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                e.printStackTrace()
            }
        }
    }

    private suspend fun fetchFortuneFromApi(): FortuneCookie {
        val num = Random.nextInt(0, 9).toString()
        val jsonURL = "https://egci428-d78f6-default-rtdb.firebaseio.com/fortunecookies/$num.json"

        val response = URL(jsonURL).readText()
        val jsonObject = JSONObject(response)
        return FortuneCookie(
            message = jsonObject.getString("message"),
            time = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()),
            status = jsonObject.getString("status") // Get the status directly
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(
            this,
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }
}