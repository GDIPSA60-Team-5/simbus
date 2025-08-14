package iss.nus.edu.sg.appfiles.feature_login.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.text.HtmlCompat
import com.example.core.di.SecureStorageManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_login.R
import iss.nus.edu.sg.appfiles.feature_login.api.AuthController
import iss.nus.edu.sg.appfiles.feature_login.data.AuthRequest
import iss.nus.edu.sg.appfiles.feature_login.databinding.ActivityLoginBinding
import iss.nus.edu.sg.appfiles.feature_navigatebar.NavigateActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var authController: AuthController

    @Inject
    lateinit var secureStorageManager: SecureStorageManager

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Style Sign Up text with different color
        binding.tvSignUp.text = HtmlCompat.fromHtml(
            getString(R.string.signup_text), // "No account yet? <font color='#CC99400F'>Sign up</font>"
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        binding.btnLogin.setOnClickListener { login() }
        binding.tvSignUp.setOnClickListener { navigateToRegister() }
    }

    private fun login() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        val request = AuthRequest(username, password)

        lifecycleScope.launch {
            val result = authController.login(request)
            result.fold(onSuccess = { authResponse ->
                val token = authResponse.token
                secureStorageManager.saveToken(token)
                secureStorageManager.saveUsername(username)
                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this@LoginActivity, NavigateActivity::class.java))
                finish() // prevent going back to login
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

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}
