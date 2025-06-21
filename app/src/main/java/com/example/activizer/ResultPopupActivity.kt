package com.example.activizer

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ResultPopupActivity : AppCompatActivity() {

    private lateinit var durationText: TextView
    private lateinit var scoreText: TextView
    private lateinit var lineChart: LineChart

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_popup)

        durationText = findViewById(R.id.durationText)
        scoreText = findViewById(R.id.scoreText)
        lineChart = findViewById(R.id.lineChart)

        val duration = intent.getFloatExtra("duration", 0.0f)
        val score = intent.getFloatExtra("score", 0.0f)
        val steps = intent.getFloatArrayExtra("steps") ?: floatArrayOf()

        durationText.text = "Duration: %.2f sec".format(duration)
        scoreText.text = "Score (MSG): %.2f step/sec".format(score)

        showLineChart(steps)
    }

    private fun showLineChart(steps: FloatArray) {
        val entries = steps.mapIndexed { index, value ->
            Entry(index.toFloat() + 1, value) // adım 1'den başlasın
        }

        val dataSet = LineDataSet(entries, "Step Durations")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.RED)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f
        xAxis.setDrawGridLines(false)

        lineChart.axisLeft.axisMinimum = 0f
        lineChart.axisRight.isEnabled = false
        lineChart.description.text = "Step durations in seconds"
        lineChart.invalidate()
    }
}
