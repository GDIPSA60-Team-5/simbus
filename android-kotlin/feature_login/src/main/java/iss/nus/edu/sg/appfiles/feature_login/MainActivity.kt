package iss.nus.edu.sg.appfiles.feature_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
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
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var togglePasswordButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var detailTextView: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameEditText = findViewById(R.id.etUsername)
        passwordEditText = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        registerButton = findViewById(R.id.btnRegister)
        togglePasswordButton = findViewById(R.id.btnTogglePassword)
        resultTextView = findViewById(R.id.tvResult)
        logoutButton = findViewById(R.id.btnLogout)
        detailTextView = findViewById(R.id.tvDetails)


        loginButton.setOnClickListener {
            login()
        }

        registerButton.setOnClickListener {
            register()
        }

        togglePasswordButton.setOnClickListener {
            togglePasswordVisibility()
        }

        logoutButton.setOnClickListener {
            logout()
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
                    val secureStorage = SecureStorageManager(this@MainActivity)
                    secureStorage.saveToken(token)
                    secureStorage.saveUsername(username)

                    //get username
                    //val secureStorage = SecureStorageManager(this@MainActivity)
                    //val token = secureStorage.getToken()
                    val username = secureStorage.getUsername()
                    detailTextView.text = username

                    //when logout clear data
                    //val secureStorage = SecureStorageManager(this@MainActivity)
                    //secureStorage.clear()
                    resultTextView.text = "login successful\nToken:\n$token"
                } else {
                    resultTextView.text = "login failed：${response.code()}"
                }
            }

            override fun onFailure(call: Call<AuthResponse?>, t: Throwable) {
                resultTextView.text = "网络错误：${t.localizedMessage}"
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
                    resultTextView.text = "registration succesful"
                } else {
                    resultTextView.text = "registrtion failed：${response.errorBody()?.string() ?: response.code()}"
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                resultTextView.text = "网络错误：${t.localizedMessage}"
            }
        })
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            togglePasswordButton.text = "hide password"
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            togglePasswordButton.text = "show password"
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun logout() {
        val secureStorage = SecureStorageManager(this@MainActivity)
        secureStorage.clearAll()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}


