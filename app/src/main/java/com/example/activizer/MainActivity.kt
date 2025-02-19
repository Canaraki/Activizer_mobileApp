package com.example.activizer

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.activizer.R.*

class MainActivity : AppCompatActivity() {
    private lateinit var userButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var statsButton: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_main)

        // Handle window insets for edge-to-edge experience
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
        } catch (e: Exception) {
            //e.printStackTrace()
            Log.d(TAG, "Stack: ${Log.getStackTraceString(Throwable())}")
        }
    }
}