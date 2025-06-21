package com.example.activizer
import android.content.Intent
import android.os.Bundle
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

class DancingActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dancing)

        // Setup navigation buttons
        val statsButton = findViewById<ImageButton>(R.id.stats4)
        val homeButton = findViewById<ImageButton>(R.id.home4)
        val userButton = findViewById<ImageButton>(R.id.user)

        statsButton.setOnClickListener {
            val intent = Intent(this, StatsPage::class.java)
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

        try {
            val container = findViewById<LinearLayout>(R.id.exerciseContainer)

            val json = org.json.JSONObject(exercisesJson)
            val dataArray = json.getJSONArray("data")

            for (i in 0 until dataArray.length()) {
                val innerArray = dataArray.getJSONArray(i)
                val routine = innerArray.getString(0)

                val btn = Button(this).apply {
                    text = routine
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    background = ContextCompat.getDrawable(this@DancingActivity, R.drawable.rounded_button)
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
                        CoroutineScope(Dispatchers.IO).launch {
                            val BASE_URL = "http://${ServerAddresses.RaspberryPiAddress}"
                            try {
                                val url = URL("$BASE_URL/exercise-selection")
                                val connection = url.openConnection() as HttpURLConnection
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
                                    Toast.makeText(this@DancingActivity, "Server: $response", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@DancingActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, "Failed to load exercises", Toast.LENGTH_SHORT).show()
        }
    }
}