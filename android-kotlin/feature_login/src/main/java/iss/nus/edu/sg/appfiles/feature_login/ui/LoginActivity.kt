package iss.nus.edu.sg.appfiles.feature_login.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_login.R
import iss.nus.edu.sg.appfiles.feature_login.api.AuthController
import iss.nus.edu.sg.appfiles.feature_login.data.AuthRequest
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var authController: AuthController

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.etUsername)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        registerButton = findViewById(R.id.btnRegister)

        loginButton.setOnClickListener { login() }
        registerButton.setOnClickListener { register() }
    }

    private fun login() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val request = AuthRequest(username, password)

        lifecycleScope.launch {
            val result = authController.login(request)
            result.fold(onSuccess = { authResponse ->
                val token = authResponse.token
                SecureStorageManager(this@LoginActivity).saveToken(token)
                SecureStorageManager(this@LoginActivity).saveUsername(username)
                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }, onFailure = { e ->
                val errorMessage = when (e) {
                    is HttpException -> when (e.code()) {
                        401 -> "Invalid username or password"
                        400 -> "Bad request"
                        else -> "Login failed: ${e.code()}"
                    }
                    else -> "Network error: ${e.localizedMessage}"
                }
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun register() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val request = AuthRequest(username, password)

        lifecycleScope.launch {
            val result = authController.register(request)
            result.fold(onSuccess = {
                Toast.makeText(this@LoginActivity, "Registration successful", Toast.LENGTH_SHORT).show()
            }, onFailure = { e ->
                val errorMessage = when (e) {
                    is HttpException -> "Registration failed: ${e.code()}"
                    else -> "Network error: ${e.localizedMessage}"
                }
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
            })
        }
    }
}
