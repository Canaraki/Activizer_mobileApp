package com.example.activizer

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ExerciseStatsActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var exerciseTitle: TextView
    private lateinit var lastExerciseHeader: TextView
    private lateinit var lastExerciseDuration: TextView
    private lateinit var lastExerciseMsg: TextView
    private lateinit var averageMsgText: TextView
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button

    private var selectedExercise: String = ""
    private var allStats: List<ExerciseStats> = listOf()
    private var startDate: Date = Date()
    private var endDate: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_stats)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selectedExercise = intent.getStringExtra("exerciseName") ?: "Unknown"

        barChart = findViewById(R.id.barChart)
        exerciseTitle = findViewById(R.id.exerciseTitle)
        lastExerciseHeader = findViewById(R.id.lastExerciseHeader)
        lastExerciseDuration = findViewById(R.id.lastExerciseDuration)
        lastExerciseMsg = findViewById(R.id.lastExerciseMsg)
        averageMsgText = findViewById(R.id.overallAverageMsg)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)

        exerciseTitle.text = selectedExercise

        val calendar = Calendar.getInstance()
        endDate = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        startDate = calendar.time

        startDateButton.setOnClickListener { showDatePicker(true) }
        endDateButton.setOnClickListener { showDatePicker(false) }

        fetchDataFromServer()
    }

    private fun fetchDataFromServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://${ServerAddresses.DatabaseAddress}/user/fetch-stats")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val calendar = Calendar.getInstance()
                val endCal = calendar.clone() as Calendar
                endCal.add(Calendar.DAY_OF_MONTH, 1)
                val startCal = calendar.clone() as Calendar
                startCal.add(Calendar.DAY_OF_MONTH, -30)

                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val requestJson = JSONObject().apply {
                    put("dateFrom", sdf.format(startCal.time))
                    put("dateUntil", sdf.format(endCal.time))
                    put("exerciseName", selectedExercise)
                }
                Log.d("fetchData", "Request JSON: "+requestJson.toString())

                val outputBytes = requestJson.toString().toByteArray(Charsets.UTF_8)
                connection.outputStream.write(outputBytes)

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("fetchData", "Raw JSON response: $response")

                val statsList = mutableListOf<ExerciseStats>()
                val json = JSONObject(response)
                Log.d("fetchData", "Parsed JSONObject: $json")
                val statsArray = json.getJSONArray("message")
                Log.d("fetchData", "Stats array: $statsArray")

                for (i in 0 until statsArray.length()) {
                    val item = statsArray.getJSONArray(i)
                    val score = item.getDouble(0).toFloat()
                    val date = item.getString(1)
                    statsList.add(ExerciseStats(selectedExercise, date, score = score))
                }

                allStats = statsList
                Log.d("fetchData", "Parsed stats list: $allStats")

                withContext(Dispatchers.Main) {
                    updateUI()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExerciseStatsActivity, "Veri çekilemedi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val cal = Calendar.getInstance()
        cal.time = if (isStart) startDate else endDate

        val dpd = DatePickerDialog(this, { _, year, month, day ->
            cal.set(year, month, day)
            if (isStart) startDate = cal.time else endDate = cal.time
            updateUI()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

        dpd.show()
    }

    private fun updateUI() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val filtered = allStats.filter {
            val date = sdf.parse(it.exerciseDate)
            date != null && !date.before(startDate) && !date.after(endDate)
        }.sortedBy { it.exerciseDate }

        val last = filtered.lastOrNull()
        last?.let {
            lastExerciseHeader.text = "Last Exercise"

            lastExerciseMsg.text = "exercise score (step/sec): %.2f".format(1/it.score)
        }

        val labels = filtered.map { it.exerciseDate }
        val entries = filtered.mapIndexed { index, stat ->
            BarEntry(index.toFloat(), stat.score)
        }

        val dataSet = BarDataSet(entries, "MSG (sec/step)")
        dataSet.color = Color.CYAN
        barChart.data = BarData(dataSet)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f
        xAxis.setDrawGridLines(false)

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.text = "Exercises Over Time"
        barChart.invalidate()

        val avgMsg = filtered.map { it.score }.average()
        averageMsgText.text = "Average MSG: %.2f sec/step".format(avgMsg)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
