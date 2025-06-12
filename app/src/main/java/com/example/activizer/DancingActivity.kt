package com.example.activizer
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
                    setOnClickListener {
                        Toast.makeText(this@DancingActivity, "Selected: $routine", Toast.LENGTH_SHORT).show()
                    }
                }

                container.addView(btn)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load exercises", Toast.LENGTH_SHORT).show()
        }
    }
}