package com.example.activizer

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StatsPage : AppCompatActivity() {
    private lateinit var userButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var statsButton: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        try {
            homeButton = findViewById(R.id.home3)
            userButton = findViewById(R.id.user3)
            statsButton = findViewById(R.id.stats3)

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