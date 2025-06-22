package com.example.activizer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private lateinit var logoutButton: com.google.android.material.button.MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get username from global variable
        val username = GlobalUser.username ?: ""
        if (username.isEmpty()) {
            finish()
            return
        }

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
            val intent = Intent(this, ExerciseSelectionActivity::class.java)
            startActivity(intent)
        }

        editButton.setOnClickListener {
            toggleEditMode(username)
        }

        logoutButton = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            performLogout()
        }

        // Fetch user data from server
        fetchUserData()
    }

    @SuppressLint("SetTextI18n")
    private fun fetchUserData() {
        val BASE_URL = "http://${ServerAddresses.DatabaseAddress}"
        val url = "$BASE_URL/user/user-data"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val responseCode = connection.responseCode
                val response = if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    java.io.BufferedReader(java.io.InputStreamReader(connection.inputStream)).use { it.readText() }
                } else {
                    java.io.BufferedReader(java.io.InputStreamReader(connection.errorStream)).use { it.readText() }
                }
                val json = org.json.JSONObject(response)
                if (json.getString("status") == "success") {
                    val data = json.getJSONArray("data")
                    // Correct order: userName, fullName, email, gender, age, weight, height
                    val userName = data.optString(0, "")
                    val fullName = data.optString(1, "")
                    val email = data.optString(2, "")
                    val gender = data.optString(3, "")
                    val age = data.optString(4, "")
                    val weight = data.optString(5, "")
                    val height = data.optString(6, "")
                    withContext(Dispatchers.Main) {
                        // Greeting shows userName
                        greetingTextView.text = if (userName.isNotEmpty()) "Hello, $userName" else "Hello"
                        // Name field shows fullName
                        nameTextView.text = fullName
                        mailTextView.text = email
                        ageTextView.text = age
                        genderTextView.text = gender
                        weightTextView.text = weight
                        heightTextView.text = height
                        // Also update EditTexts
                        nameEditText.setText(fullName)
                        mailEditText.setText(email)
                        ageEditText.setText(age)
                        genderEditText.setText(gender)
                        weightEditText.setText(weight)
                        heightEditText.setText(height)
                        // Update label for age
                        val ageBar = findViewById<TextView>(R.id.age_bar)
                        ageBar?.text = "Age:"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    greetingTextView.text = "Failed to load user data"
                }
            }
        }
    }

    private fun updateUserAttributesJson(username: String, weight: String, height: String, age: String) {
        val BASE_URL = "http://${ServerAddresses.DatabaseAddress}"
        val url = "$BASE_URL/user/edit-user"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                val json = org.json.JSONObject().apply {
                    put("weight", weight)
                    put("height", height)
                    put("age", age)
                    put("userName", username)
                }
                connection.outputStream.use { os ->
                    os.write(json.toString().toByteArray())
                    os.flush()
                }
                val responseCode = connection.responseCode
                val response = if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    java.io.BufferedReader(java.io.InputStreamReader(connection.inputStream)).use { it.readText() }
                } else {
                    java.io.BufferedReader(java.io.InputStreamReader(connection.errorStream)).use { it.readText() }
                }
                android.util.Log.d("editresponse", "Response code: $responseCode, body: $response")
            } catch (e: Exception) {
                android.util.Log.d("editresponse", "Exception: ${e.message}")
            }
        }
    }

    private fun toggleEditMode(username: String) {
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
            // Switch to view mode and update server
            nameTextView.text = nameEditText.text
            mailTextView.text = mailEditText.text
            ageTextView.text = ageEditText.text
            genderTextView.text = genderEditText.text
            weightTextView.text = weightEditText.text
            heightTextView.text = heightEditText.text

            // Send all updated fields as JSON
            updateUserAttributesJson(username, weightEditText.text.toString(), heightEditText.text.toString(), ageEditText.text.toString())

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

    private fun performLogout() {
        val BASE_URL = "http://${ServerAddresses.DatabaseAddress}"
        val url = "$BASE_URL/logout"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.doOutput = true
                connection.connect()
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) {
                val intent = Intent(this@UserDetails, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}