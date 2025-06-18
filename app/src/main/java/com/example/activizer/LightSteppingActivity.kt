package com.example.activizer

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.json.JSONArray

class LightSteppingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_light_stepping)

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
                    background = ContextCompat.getDrawable(this@LightSteppingActivity, R.drawable.rounded_button)
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
                        Toast.makeText(this@LightSteppingActivity, "Selected: $routine", Toast.LENGTH_SHORT).show()
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
