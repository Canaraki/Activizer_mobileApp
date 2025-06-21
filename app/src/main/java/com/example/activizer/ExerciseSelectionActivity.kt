package com.example.activizer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ExerciseSelectionActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_selection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        container = findViewById(R.id.exerciseButtonContainer)

        fetchExerciseNamesFromServer()
    }

    private fun fetchExerciseNamesFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://${ServerAddresses.RaspberryPiAddress}/user/exercise-names")//route correct, check server code
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doInput = true

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                if (json.getString("status") != "success") {
                    throw Exception("Server response error: ${json.getString("message")}")
                }

                val exercisesArray = json.getJSONArray("exercises")
                val namesSet = mutableSetOf<String>()
                for (i in 0 until exercisesArray.length()) {
                    val exerciseName = exercisesArray.getString(i) //
                    namesSet.add(exerciseName)
                }

                withContext(Dispatchers.Main) {
                    if (namesSet.isEmpty()) {
                        Toast.makeText(this@ExerciseSelectionActivity, "Egzersiz bulunamadı!", Toast.LENGTH_SHORT).show()
                    } else {
                        for (name in namesSet) {
                            val btn = Button(this@ExerciseSelectionActivity).apply {
                                text = name
                                background = ContextCompat.getDrawable(this@ExerciseSelectionActivity, R.drawable.rounded_button)
                                setTextColor(ContextCompat.getColor(this@ExerciseSelectionActivity, android.R.color.white))
                                textSize = 18f
                                setPadding(24, 16, 24, 16)

                                setOnClickListener {
                                    val intent = Intent(this@ExerciseSelectionActivity, ExerciseStatsActivity::class.java)
                                    intent.putExtra("exerciseName", name)
                                    startActivity(intent)
                                }

                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(0, 16, 0, 0)
                                }
                            }

                            container.addView(btn)
                        }
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ExerciseSelection", "Hata oluştu: ${e.message}")
                    Toast.makeText(
                        this@ExerciseSelectionActivity,
                        "Egzersiz listesi alınamadı: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
