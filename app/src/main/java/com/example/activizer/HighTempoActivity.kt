package com.example.activizer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.View
import android.graphics.BitmapFactory
import android.widget.ImageView

class HighTempoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_tempo)

        // Setup navigation buttons
        val statsButton = findViewById<ImageButton>(R.id.stats4)
        val homeButton = findViewById<ImageButton>(R.id.home4)
        val userButton = findViewById<ImageButton>(R.id.user)

        statsButton.setOnClickListener {
            val intent = Intent(this, ExerciseSelectionActivity::class.java)
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        userButton.setOnClickListener {
            val intent = Intent(this, UserDetails::class.java)
            startActivity(intent)
        }

        val exercisesJson = intent.getStringExtra("exercises")

        val diagramView = findViewById<ImageView>(R.id.exerciseDiagram)

        try {
            val container = findViewById<LinearLayout>(R.id.exerciseContainer)

            val json = org.json.JSONObject(exercisesJson)
            val dataArray = json.getJSONArray("data")

            for (i in 0 until dataArray.length()) {
                val innerArray = dataArray.getJSONArray(i)
                val routine = innerArray.getString(0)

                val btn = Button(this).apply {
                    text = routine.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    background = ContextCompat.getDrawable(this@HighTempoActivity, R.drawable.rounded_button)
                    setPadding(32, 24, 32, 24)
                    setAllCaps(false)
                    elevation = 8f

                    // Set margins
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 20, 0, 20)
                    this.layoutParams = layoutParams

                    setOnClickListener {
                        // Show the diagram
                        val diagramName = "$routine.png"
                        try {
                            val assetManager = assets
                            val inputStream = assetManager.open("exercise-diagrams/$diagramName")
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            diagramView.setImageBitmap(bitmap)
                            diagramView.visibility = View.VISIBLE
                            inputStream.close()
                        } catch (e: Exception) {
                            diagramView.visibility = View.GONE // Hide if not found
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            val BASE_URL = "http://${ServerAddresses.RaspberryPiAddress}"
                            try {
                                val url = URL("$BASE_URL/exercise-selection")
                                val connection = url.openConnection() as HttpURLConnection
                                connection.readTimeout = 120000 // 2 minutes for exercise
                                connection.connectTimeout = 15000 // 15 seconds
                                connection.requestMethod = "POST"
                                connection.setRequestProperty("Content-Type", "application/json")
                                connection.doOutput = true
                                val payload = """
                                    {
                                      "name": "$routine"
                                    }
                                """.trimIndent()
                                connection.outputStream.use { os ->
                                    os.write(payload.toByteArray())
                                    os.flush()
                                }
                                val response = if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                                    BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                                } else {
                                    BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
                                }
                                withContext(Dispatchers.Main) {
                                    Log.d("tag:exerciseStats", "Raw Pi server response: $response")
                                    try {
                                        val jsonResponse = org.json.JSONObject(response)
                                        val stat = jsonResponse.getDouble("stat").toFloat()
                                        val intervalJsonArray = jsonResponse.getJSONArray("interval")
                                        val interval = List(intervalJsonArray.length()) { i ->
                                            intervalJsonArray.getDouble(i).toFloat()
                                        }
                                        val duration = jsonResponse.getDouble("duration").toFloat()
                                        val exercise = jsonResponse.getString("exercise")

                                        Log.d("tag:exerciseStats", "Received exercise stats. Exercise: $exercise, Stat: $stat, Duration: $duration, Intervals: ${interval.joinToString(", ")}")

                                        val intent = Intent(this@HighTempoActivity, ResultPopupActivity::class.java)
                                        intent.putExtra("duration", duration)
                                        intent.putExtra("score", stat)
                                        intent.putExtra("steps", interval.toFloatArray())
                                        diagramView.visibility = View.GONE
                                        startActivity(intent)

                                        // Send stats to the database server
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val dbUrl = URL("http://${ServerAddresses.DatabaseAddress}/user/user-stats")
                                                val dbConnection = dbUrl.openConnection() as HttpURLConnection
                                                dbConnection.requestMethod = "POST"
                                                dbConnection.setRequestProperty("Content-Type", "application/json; utf-8")
                                                dbConnection.doOutput = true

                                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                                val currentDate = sdf.format(Date())

                                                val jsonPayload = """
                                                    {
                                                        "statDate": "$currentDate",
                                                        "score": $stat,
                                                        "exercise": "$exercise"
                                                    }
                                                """.trimIndent()

                                                dbConnection.outputStream.use { os ->
                                                    os.write(jsonPayload.toByteArray(Charsets.UTF_8))
                                                }

                                                val dbResponse = if (dbConnection.responseCode == HttpURLConnection.HTTP_OK) {
                                                    BufferedReader(InputStreamReader(dbConnection.inputStream, "utf-8")).use { it.readText() }
                                                } else {
                                                    BufferedReader(InputStreamReader(dbConnection.errorStream, "utf-8")).use { it.readText() }
                                                }

                                                withContext(Dispatchers.Main) {
                                                    Log.d("tag:exerciseStats", "Database Server Response: $dbResponse")
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    val errorMessage = "Database server error: ${e.message}"
                                                    Log.e("tag:exerciseStats", errorMessage)
                                                }
                                            }
                                        }
                                    } catch (e: org.json.JSONException) {
                                        val errorMessage = "Error parsing Pi server response: ${e.message}"
                                        Log.e("tag:exerciseStats", errorMessage)
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Log.e("tag:exerciseStats", "Error: ${e.message}")
                                }
                            }
                        }
                    }
                }

                btn.translationY = 100f
                btn.alpha = 0f
                container.addView(btn)

                btn.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .setStartDelay(i * 100L)
                    .start()
            }

        } catch (e: Exception) {
            Log.e("tag:exerciseStats", "Failed to load exercises")
        }
    }
}