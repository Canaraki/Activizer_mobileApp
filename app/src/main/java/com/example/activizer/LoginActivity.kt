package com.example.activizer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LoginActivity : AppCompatActivity() {
    private lateinit var loginForm: LinearLayout
    private lateinit var registerForm: LinearLayout
    private lateinit var titleText: TextView
    private lateinit var switchToRegisterButton: Button
    private lateinit var switchToLoginButton: Button
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var skipLoginButton: Button

    // Server configuration
    private var BASE_URL = "http://${ServerAddresses.DatabaseAddress}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        loginForm = findViewById(R.id.loginForm)
        registerForm = findViewById(R.id.registerForm)
        titleText = findViewById(R.id.titleText)
        switchToRegisterButton = findViewById(R.id.switchToRegisterButton)
        switchToLoginButton = findViewById(R.id.switchToLoginButton)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        skipLoginButton = findViewById(R.id.skipLoginButton)

        // Login and register buttons are enabled by default
        loginButton.isEnabled = true
        registerButton.isEnabled = true

        // Set up click listeners
        switchToRegisterButton.setOnClickListener {
            showRegisterForm()
        }

        switchToLoginButton.setOnClickListener {
            showLoginForm()
        }

        //animations

        loginButton.alpha = 0f
        loginButton.translationY = 100f
        loginButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(300)
            .start()

        skipLoginButton.alpha = 0f
        skipLoginButton.translationY = 100f
        skipLoginButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(300)
            .start()

        titleText.alpha = 0f
        titleText.translationY = -100f
        titleText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(300)
            .start()

        loginButton.setOnClickListener {
            val username = findViewById<TextInputEditText>(R.id.usernameInput).text.toString()
            val password = findViewById<TextInputEditText>(R.id.passwordInput).text.toString()
            
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(username, password)
        }

        registerButton.setOnClickListener {
            val username = findViewById<TextInputEditText>(R.id.regUsernameInput).text.toString()
            val password = findViewById<TextInputEditText>(R.id.regPasswordInput).text.toString()
            val fullName = findViewById<TextInputEditText>(R.id.fullNameInput).text.toString()
            val email = findViewById<TextInputEditText>(R.id.emailInput).text.toString()
            val age = findViewById<TextInputEditText>(R.id.ageInput).text.toString()
            val gender = findViewById<TextInputEditText>(R.id.genderInput).text.toString()
            val weight = findViewById<TextInputEditText>(R.id.weightInput).text.toString()
            val height = findViewById<TextInputEditText>(R.id.heightInput).text.toString()

            if (username.isBlank() || password.isBlank() || fullName.isBlank() || 
                email.isBlank() || age.isBlank() || gender.isBlank() || 
                weight.isBlank() || height.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            register(username, password, fullName, email, age, gender, weight, height)
        }

        skipLoginButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun showRegisterForm() {
        loginForm.visibility = View.GONE
        registerForm.visibility = View.VISIBLE
        titleText.text = "Register"
    }

    private fun showLoginForm() {
        registerForm.visibility = View.GONE
        loginForm.visibility = View.VISIBLE
        titleText.text = "Login"
    }

    private fun login(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Prepare request parameters
                val reqParam = URLEncoder.encode("userName", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8") +
                             "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")

                val url = URL("$BASE_URL/login")
                Log.d("LoginActivity", "Sending login request to: $url")
                Log.d("LoginActivity", "Request parameters: $reqParam")

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Connection", "close")
                    connectTimeout = 5000
                    readTimeout = 5000

                    // Write request
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(reqParam)
                        writer.flush()
                    }

                    // Get response code
                    val responseCode = responseCode
                    Log.d("LoginActivity", "Response code: $responseCode")

                    // Read response
                    val response = try {
                        val responseText = if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                                val response = StringBuffer()
                                var inputLine = reader.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = reader.readLine()
                                }
                                response.toString()
                            }
                        } else {
                            BufferedReader(InputStreamReader(errorStream)).use { reader ->
                                val response = StringBuffer()
                                var inputLine = reader.readLine()
                                while (inputLine != null) {
                                    response.append(inputLine)
                                    inputLine = reader.readLine()
                                }
                                response.toString()
                            }
                        }
                        Log.d("LoginActivity", "Response: $responseText")
                        responseText
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Error reading response: ${e.message}", e)
                        throw e
                    }

                    withContext(Dispatchers.Main) {
                        if (response.contains(" has successfully logged in")) {
                            // Login successful, navigate to main activity
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, 
                                "Login failed. Please check your credentials.", 
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error during login: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, 
                        "Error: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun register(
        username: String,
        password: String,
        fullName: String,
        email: String,
        age: String,
        gender: String,
        weight: String,
        height: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$BASE_URL/register")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInput = JSONObject().apply {
                    put("userName", username)
                    put("password", password)
                    put("fullName", fullName)
                    put("email", email)
                    put("age", age)
                    put("gender", gender)
                    put("weight", weight)
                    put("height", height)
                }

                // Print request details
                Log.d("LoginActivity", "Sending register request to: $url")
                Log.d("LoginActivity", "Request payload: $jsonInput")

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonInput.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
                }

                // Log server response
                Log.d("registerresponse", "Response code: $responseCode, body: $response")

                val jsonResponse = JSONObject(response)
                withContext(Dispatchers.Main) {
                    if (jsonResponse.getString("status") == "success") {
                        // Registration successful, go to main page and set username
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, 
                            jsonResponse.optString("message", "Registration failed."), 
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error during registration: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, 
                        "Error: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
} 