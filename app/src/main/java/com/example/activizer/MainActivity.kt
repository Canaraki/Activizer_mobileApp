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

    private val SERVER_IP = "192.168.137.110"//IP address of Raspberry Pi on hotspot
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

            // Activity button click listeners - Uncomment when server is ready
            lightSteppingButton.setOnClickListener {
                sendActivityRequest("stepping")
            }

            highTempoButton.setOnClickListener {
                sendActivityRequest("tempo")
            }

            danceButton.setOnClickListener {
                sendActivityRequest("dance")
            }


        } catch (e: Exception) {
            //e.printStackTrace()
            Log.d(TAG, "Stack: ${Log.getStackTraceString(Throwable())}")
        }
    }

    // Network request function
    private fun sendActivityRequest(activityType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First, get the exercises for the selected type
                val exercisesUrl = URL("$BASE_URL/api/exercises")
                val exercisesConnection = exercisesUrl.openConnection() as HttpURLConnection
                exercisesConnection.requestMethod = "POST"
                exercisesConnection.setRequestProperty("Content-Type", "application/json")
                exercisesConnection.doOutput = true

                // Create JSON payload for getting exercises
                val exercisesPayload = """
                    {
                        "type": "$activityType"
                    }
                """.trimIndent()

                // Write payload to output stream
                exercisesConnection.outputStream.use { os ->
                    os.write(exercisesPayload.toByteArray())
                    os.flush()
                }

                // Get exercises response
                val exercisesResponse = if (exercisesConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(exercisesConnection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    "Error: ${exercisesConnection.responseCode}"
                }

                // Now send the exercise selection
                val selectionUrl = URL("$BASE_URL/exercise-selection")
                val selectionConnection = selectionUrl.openConnection() as HttpURLConnection
                selectionConnection.requestMethod = "GET"
                selectionConnection.setRequestProperty("Content-Type", "application/json")
                selectionConnection.doOutput = true

                // Create JSON payload for exercise selection
                val selectionPayload = """
                    {
                        "text": "$activityType"
                    }
                """.trimIndent()

                // Write payload to output stream
                selectionConnection.outputStream.use { os ->
                    os.write(selectionPayload.toByteArray())
                    os.flush()
                }

                // Get selection response
                val selectionResponse = if (selectionConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(selectionConnection.inputStream)).use { reader ->
                        reader.readText()
                    }
                } else {
                    "Error: ${selectionConnection.responseCode}"
                }

                // Show response in UI thread
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Activity request sent: $activityType\nExercises: $exercisesResponse\nSelection: $selectionResponse",
                        Toast.LENGTH_LONG
                    ).show()
                    if (activityType == "stepping") {
                        Log.d("ResponseDebug", "Exercises JSON: $exercisesResponse")
                        val intent = Intent(this@MainActivity, LightSteppingActivity::class.java)
                        intent.putExtra("exercises", exercisesResponse)
                        startActivity(intent)
                    }
                    if (activityType == "tempo") {
                        val intent = Intent(this@MainActivity, HighTempoActivity::class.java)
                        intent.putExtra("exercises", exercisesResponse)
                        startActivity(intent)
                    }
                    if (activityType == "dance") {
                        val intent = Intent(this@MainActivity, DancingActivity::class.java)
                        intent.putExtra("exercises", exercisesResponse)
                        startActivity(intent)
                    }

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
    //*/
}