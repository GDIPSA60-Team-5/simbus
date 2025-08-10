package iss.nus.edu.sg.appfiles.feature_navigatebar.menu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.core.di.SecureStorageManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_navigatebar.ChangePasswordRequest
import iss.nus.edu.sg.appfiles.feature_navigatebar.R
import iss.nus.edu.sg.appfiles.feature_navigatebar.UserApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var currentPasswordEdit: EditText
    private lateinit var newPasswordEdit: EditText
    private lateinit var confirmPasswordEdit: EditText
    private lateinit var submitButton: Button
    private lateinit var backButton: ImageView

    @Inject
    lateinit var secureStorageManager: SecureStorageManager

    @Inject
    lateinit var userApi: UserApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // init UI
        currentPasswordEdit = findViewById(R.id.currentPassword)
        newPasswordEdit = findViewById(R.id.newPassword)
        confirmPasswordEdit = findViewById(R.id.confirmPassword)
        submitButton = findViewById(R.id.submitBtn)
        backButton = findViewById(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val currentPassword = currentPasswordEdit.text.toString().trim()
            val newPassword = newPasswordEdit.text.toString().trim()
            val confirmPassword = confirmPasswordEdit.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = secureStorageManager.getToken()
            if (token == null) {
                Toast.makeText(this, "Not logged in yet. Please log in first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = userApi.changePassword(
                        ChangePasswordRequest(currentPassword, newPassword)
                    )
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChangePasswordActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val error = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@ChangePasswordActivity, error, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ChangePasswordActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
