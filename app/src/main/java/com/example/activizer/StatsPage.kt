package com.example.activizer

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class StatsPage : AppCompatActivity() {
    private lateinit var userButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var statsButton: ImageButton
    private lateinit var statsRecyclerView: RecyclerView
    private lateinit var statsAdapter: StatsAdapter

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
            setupNavigationButtons()
            setupRecyclerView()
            fetchUserStats()
        } catch (e: Exception) {
            Log.d(TAG, "Stack: ${Log.getStackTraceString(Throwable())}")
        }
    }

    private fun setupNavigationButtons() {
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
        statsButton.setOnClickListener {
            val intent = Intent(this, StatsPage::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        statsRecyclerView = findViewById(R.id.statsRecyclerView)
        statsRecyclerView.layoutManager = LinearLayoutManager(this)
        statsAdapter = StatsAdapter(emptyList())
        statsRecyclerView.adapter = statsAdapter
    }

    private fun fetchUserStats() {
        // Get current date and date 30 days ago for the date range
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateUntil = dateFormat.format(calendar.time)
        
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        val dateFrom = dateFormat.format(calendar.time)

        // Create the request URL with date parameters
        val url = "http://192.168.137.110:5000/user/user-stats?dateFrom=$dateFrom&dateUntil=$dateUntil"

        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    if (response.getString("status") == "success") {
                        val statsArray = response.getJSONArray("message")
                        val statsList = parseStatsFromJson(statsArray)
                        statsAdapter = StatsAdapter(statsList)
                        statsRecyclerView.adapter = statsAdapter
                    } else {
                        Toast.makeText(this, "Failed to fetch statistics", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing stats: ${e.message}")
                    Toast.makeText(this, "Error parsing statistics", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e(TAG, "Error fetching stats: ${error.message}")
                Toast.makeText(this, "Error fetching statistics", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun parseStatsFromJson(jsonArray: JSONArray): List<ExerciseStats> {
        val statsList = mutableListOf<ExerciseStats>()
        
        for (i in 0 until jsonArray.length()) {
            val stat = jsonArray.getJSONObject(i)
            statsList.add(
                ExerciseStats(
                    exerciseName = stat.getString("exercise_name"),
                    date = stat.getString("date"),
                    duration = stat.getInt("duration"),
                    caloriesBurned = stat.getInt("calories"),
                    steps = stat.getInt("steps"),
                    accuracy = stat.getDouble("accuracy").toFloat()
                )
            )
        }
        
        return statsList.sortedByDescending { it.date }
    }
}