package com.example.activizer

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.activizer.R.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var userButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var statsButton: ImageButton
    private lateinit var lightSteppingButton: Button
    private lateinit var highTempoButton: Button
    private lateinit var danceButton: Button

    // Server configuration - to be updated with actual server IP
    private val SERVER_IP = "192.168.1.100" // Replace with actual server IP
    private val SERVER_PORT = 5000
    private val BASE_URL = "http://$SERVER_IP:$SERVER_PORT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_main)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the button by ID and set up a click listener
        try {
            homeButton = findViewById(id.jojo)
            userButton = findViewById(id.bizarre)
            statsButton = findViewById(id.stats)
            lightSteppingButton = findViewById(id.stepping)
            highTempoButton = findViewById(id.highTempo)
            danceButton = findViewById(id.dance)

            userButton.setOnClickListener {
                val intent = Intent(this, UserDetails::class.java)
                startActivity(intent)
            }
            homeButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            statsButton.setOnClickListener{
                val intent = Intent(this, StatsPage::class.java)
                startActivity(intent)
            }

            /* Activity button click listeners - Uncomment when server is ready
            lightSteppingButton.setOnClickListener {
                sendActivityRequest("light_stepping")
            }

            highTempoButton.setOnClickListener {
                sendActivityRequest("high_tempo")
            }

            danceButton.setOnClickListener {
                sendActivityRequest("dance")
            }
            */

        } catch (e: Exception) {
            //e.printStackTrace()
            Log.d(TAG, "Stack: ${Log.getStackTraceString(Throwable())}")
        }
    }

    /* Network request function Uncomment when server is ready!!!
    private fun sendActivityRequest(activityType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$BASE_URL/start_activity")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                // Create JSON payload
                val jsonPayload = """
                    {
                        "activity_type": "$activityType",
                        "timestamp": "${System.currentTimeMillis()}"
                    }
                """.trimIndent()

                // Write payload to output stream
                connection.outputStream.use { os ->
                    os.write(jsonPayload.toByteArray())
                    os.flush()
                }

                // Get response
                val responseCode = connection.responseCode
                val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    "Error: $responseCode"
                }

                // Show response in UI thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Activity request sent: $activityType\nResponse: $response",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error sending request: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    */
}