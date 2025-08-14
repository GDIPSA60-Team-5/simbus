package iss.nus.edu.sg.appfiles.feature_login.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.core.di.SecureStorageManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_login.R
import iss.nus.edu.sg.appfiles.feature_login.api.AuthController
import iss.nus.edu.sg.appfiles.feature_login.data.AuthRequest
import iss.nus.edu.sg.appfiles.feature_navigatebar.NavigateActivity
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject lateinit var authController: AuthController
    @Inject lateinit var secureStorageManager: SecureStorageManager

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signUpButton)

        loginButton.setOnClickListener { login() }
        signupButton.setOnClickListener { register() }
    }

    private fun login() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        val request = AuthRequest(username, password)

        lifecycleScope.launch {
            val result = authController.login(request)
            result.fold(onSuccess = { authResponse ->
                val token = authResponse.token
                secureStorageManager.saveToken(token)
                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, NavigateActivity::class.java))
                finish()
            }, onFailure = { e ->
                val msg = when (e) {
                    is IllegalStateException -> e.message ?: "Login failed"
                    is HttpException -> {
                        val raw = e.response()?.errorBody()?.string()
                        try {
                            if (!raw.isNullOrBlank())
                                JSONObject(raw).optString("message", "Login failed (${e.code()})")
                            else
                                "Login failed (${e.code()})"
                        } catch (_: Exception) {
                            "Login failed (${e.code()})"
                        }
                    }
                    else -> "Network error, please try again"
                }
                Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
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
                val okMsg = it.message.ifBlank { "Registration successful" }
                Toast.makeText(this@LoginActivity, okMsg, Toast.LENGTH_SHORT).show()
            }, onFailure = { e ->
                val errorMessage = when (e) {
                    is IllegalStateException -> e.message ?: "Registration failed"
                    is HttpException -> {
                        val raw = e.response()?.errorBody()?.string()
                        try {
                            if (!raw.isNullOrBlank())
                                JSONObject(raw).optString("message", "Registration failed (${e.code()})")
                            else
                                "Registration failed (${e.code()})"
                        } catch (_: Exception) {
                            "Registration failed (${e.code()})"
                        }
                    }
                    else -> "Network error, please try again"
                }
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
            })
        }
    }
}
