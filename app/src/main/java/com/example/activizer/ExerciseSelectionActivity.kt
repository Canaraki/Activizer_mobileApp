
package com.example.activizer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

import com.example.activizer.StatsDatabaseHelper



class ExerciseSelectionActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var dbHelper: StatsDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_selection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        container = findViewById(R.id.exerciseButtonContainer)
        dbHelper = StatsDatabaseHelper(this)

        val exerciseNames = dbHelper.getAllExerciseNames()
        for (name in exerciseNames) {
            val btn = Button(this).apply {
                text = name
                setOnClickListener {
                    val intent = Intent(this@ExerciseSelectionActivity, ExerciseStatsActivity::class.java)
                    intent.putExtra("exerciseName", name)
                    startActivity(intent)
                }
            }
            container.addView(btn)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

/*
package com.example.activizer



import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ExerciseSelectionActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_selection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        container = findViewById(R.id.exerciseButtonContainer)

        val dummyList = listOf("deneme 1 ", "deneme 2 ", "deneme 3","deneme 4", "deneme 5")

        for (name in dummyList) {
            val btn = Button(this).apply {
                text = name
                setTextColor(Color.WHITE)
                textSize = 16f
                background = ContextCompat.getDrawable(
                    this@ExerciseSelectionActivity,
                    R.drawable.rounded_button
                )
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(32, 24, 32, 24)
                layoutParams = params

                setOnClickListener {
                    val intent = Intent(this@ExerciseSelectionActivity, ExerciseStatsActivity::class.java)
                    intent.putExtra("exerciseName", name)
                    startActivity(intent)
                }
            }
            container.addView(btn)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
*/