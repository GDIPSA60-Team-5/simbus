package iss.nus.edu.sg.appfiles.feature_login.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_login.api.AuthController
import iss.nus.edu.sg.appfiles.feature_login.data.LoginRequest
import iss.nus.edu.sg.appfiles.feature_login.data.RegisterRequest
import iss.nus.edu.sg.appfiles.feature_login.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    @Inject
    lateinit var authController: AuthController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnRegister.isEnabled = false
            binding.btnRegister.text = "Registering..."

            CoroutineScope(Dispatchers.IO).launch {
                val result = authController.register(RegisterRequest(username, email, password))
                runOnUiThread {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                    result.fold(
                        onSuccess = {
                            Toast.makeText(this@RegisterActivity, it.message, Toast.LENGTH_LONG).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        },
                        onFailure = {
                            Toast.makeText(this@RegisterActivity, it.localizedMessage ?: "Error", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }

        binding.tvLoginRedirect.setOnClickListener {
            finish()
        }
    }
}
