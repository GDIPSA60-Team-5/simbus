package iss.nus.edu.sg.appfiles.feature_login

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.appfiles.feature_login.model.AuthRequest
import iss.nus.edu.sg.appfiles.feature_login.model.AuthResponse
import iss.nus.edu.sg.appfiles.feature_login.model.MessageResponse
import iss.nus.edu.sg.appfiles.feature_login.network.RetrofitInstance
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

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

        loginButton.setOnClickListener {
            login()
        }

        registerButton.setOnClickListener {
            register()
        }
    }

    private fun login() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        val request = AuthRequest(username, password)
        val call = RetrofitInstance.api.login(request)

        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.token ?: ""
                    val secureStorage = SecureStorageManager(this@LoginActivity)
                    secureStorage.saveToken(token)
                    secureStorage.saveUsername(username)
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun register() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        val request = AuthRequest(username, password)
        val call = RetrofitInstance.api.register(request)

        call.enqueue(object : Callback<MessageResponse> {
            override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@LoginActivity, "Registration failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
