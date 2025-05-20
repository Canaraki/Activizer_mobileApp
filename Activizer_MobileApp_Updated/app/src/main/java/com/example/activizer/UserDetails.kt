package com.example.activizer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserDetails : AppCompatActivity() {
    private lateinit var userButton: ImageButton
    private lateinit var homeButton: ImageButton
    private lateinit var statsButton: ImageButton
    private lateinit var editButton: FloatingActionButton
    private lateinit var greetingTextView: TextView

    private lateinit var nameTextView: TextView
    private lateinit var mailTextView: TextView
    private lateinit var ageTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var weightTextView: TextView
    private lateinit var heightTextView: TextView

    private lateinit var nameEditText: EditText
    private lateinit var mailEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var heightEditText: EditText

    private var isEditMode = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE)

        // Initialize navigation buttons
        userButton = findViewById(R.id.user2)
        homeButton = findViewById(R.id.home2)
        statsButton = findViewById(R.id.stats2)
        editButton = findViewById(R.id.editButton)
        greetingTextView = findViewById(R.id.User)

        // Initialize TextViews
        nameTextView = findViewById(R.id.name)
        mailTextView = findViewById(R.id.mail)
        ageTextView = findViewById(R.id.age)
        genderTextView = findViewById(R.id.gender)
        weightTextView = findViewById(R.id.weight)
        heightTextView = findViewById(R.id.height)

        // Load saved data
        loadUserData()

        // Initialize EditTexts
        nameEditText = EditText(this).apply {
            id = R.id.name_edit
            layoutParams = nameTextView.layoutParams
            setText(nameTextView.text)
            visibility = android.view.View.GONE
        }
        mailEditText = EditText(this).apply {
            id = R.id.mail_edit
            layoutParams = mailTextView.layoutParams
            setText(mailTextView.text)
            visibility = android.view.View.GONE
        }
        ageEditText = EditText(this).apply {
            id = R.id.age_edit
            layoutParams = ageTextView.layoutParams
            setText(ageTextView.text)
            visibility = android.view.View.GONE
        }
        genderEditText = EditText(this).apply {
            id = R.id.gender_edit
            layoutParams = genderTextView.layoutParams
            setText(genderTextView.text)
            visibility = android.view.View.GONE
        }
        weightEditText = EditText(this).apply {
            id = R.id.weight_edit
            layoutParams = weightTextView.layoutParams
            setText(weightTextView.text)
            visibility = android.view.View.GONE
        }
        heightEditText = EditText(this).apply {
            id = R.id.height_edit
            layoutParams = heightTextView.layoutParams
            setText(heightTextView.text)
            visibility = android.view.View.GONE
        }

        // Add EditTexts to the layout
        val infoFrame = findViewById<android.widget.FrameLayout>(R.id.info_frame)
        infoFrame.addView(nameEditText)
        infoFrame.addView(mailEditText)
        infoFrame.addView(ageEditText)
        infoFrame.addView(genderEditText)
        infoFrame.addView(weightEditText)
        infoFrame.addView(heightEditText)

        // Set up click listeners
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

        editButton.setOnClickListener {
            toggleEditMode()
        }
    }

    private fun loadUserData() {
        val name = sharedPreferences.getString("name", "") ?: ""
        val mail = sharedPreferences.getString("mail", "") ?: ""
        val age = sharedPreferences.getString("age", "") ?: ""
        val gender = sharedPreferences.getString("gender", "") ?: ""
        val weight = sharedPreferences.getString("weight", "") ?: ""
        val height = sharedPreferences.getString("height", "") ?: ""

        nameTextView.text = name
        mailTextView.text = mail
        ageTextView.text = age
        genderTextView.text = gender
        weightTextView.text = weight
        heightTextView.text = height

        // Update greeting text
        greetingTextView.text = if (name.isNotEmpty()) "Hello, $name" else "Hello"
    }

    private fun saveUserData() {
        with(sharedPreferences.edit()) {
            putString("name", nameTextView.text.toString())
            putString("mail", mailTextView.text.toString())
            putString("age", ageTextView.text.toString())
            putString("gender", genderTextView.text.toString())
            putString("weight", weightTextView.text.toString())
            putString("height", heightTextView.text.toString())
            apply()
        }
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        if (isEditMode) {
            // Switch to edit mode
            nameTextView.visibility = android.view.View.GONE
            mailTextView.visibility = android.view.View.GONE
            ageTextView.visibility = android.view.View.GONE
            genderTextView.visibility = android.view.View.GONE
            weightTextView.visibility = android.view.View.GONE
            heightTextView.visibility = android.view.View.GONE

            nameEditText.visibility = android.view.View.VISIBLE
            mailEditText.visibility = android.view.View.VISIBLE
            ageEditText.visibility = android.view.View.VISIBLE
            genderEditText.visibility = android.view.View.VISIBLE
            weightEditText.visibility = android.view.View.VISIBLE
            heightEditText.visibility = android.view.View.VISIBLE
        } else {
            // Switch to view mode
            nameTextView.text = nameEditText.text
            mailTextView.text = mailEditText.text
            ageTextView.text = ageEditText.text
            genderTextView.text = genderEditText.text
            weightTextView.text = weightEditText.text
            heightTextView.text = heightEditText.text

            // Save the data when switching back to view mode
            saveUserData()
            // Update greeting text
            greetingTextView.text = "Hello, ${nameTextView.text}"

            nameTextView.visibility = android.view.View.VISIBLE
            mailTextView.visibility = android.view.View.VISIBLE
            ageTextView.visibility = android.view.View.VISIBLE
            genderTextView.visibility = android.view.View.VISIBLE
            weightTextView.visibility = android.view.View.VISIBLE
            heightTextView.visibility = android.view.View.VISIBLE

            nameEditText.visibility = android.view.View.GONE
            mailEditText.visibility = android.view.View.GONE
            ageEditText.visibility = android.view.View.GONE
            genderEditText.visibility = android.view.View.GONE
            weightEditText.visibility = android.view.View.GONE
            heightEditText.visibility = android.view.View.GONE
        }
    }
}