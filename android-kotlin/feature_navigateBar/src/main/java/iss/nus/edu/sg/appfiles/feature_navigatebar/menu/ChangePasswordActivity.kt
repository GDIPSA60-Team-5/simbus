package iss.nus.edu.sg.appfiles.feature_navigatebar.menu

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.core.api.ChangePasswordRequest
import com.example.core.api.UserApi
import com.example.core.di.SecureStorageManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_navigatebar.R
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

        // view bindings from activity_change_password.xml
        backButton = findViewById(R.id.btnBack)
        currentPasswordEdit = findViewById(R.id.currentPassword)
        newPasswordEdit = findViewById(R.id.newPassword)
        confirmPasswordEdit = findViewById(R.id.confirmPassword)
        submitButton = findViewById(R.id.submitBtn)

        backButton.setOnClickListener { finish() }

        submitButton.setOnClickListener {
            val oldPwd = currentPasswordEdit.text?.toString()?.trim().orEmpty()
            val newPwd = newPasswordEdit.text?.toString()?.trim().orEmpty()
            val confirmPwd = confirmPasswordEdit.text?.toString()?.trim().orEmpty()

            // === Client-side validations (match backend rules) ===
            // (1) non-empty
            if (oldPwd.isEmpty() || newPwd.isEmpty()) {
                Toast.makeText(this, "Parameters cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // (2) min length 8
            if (newPwd.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // (3) new != old
            if (oldPwd == newPwd) {
                Toast.makeText(this, "New password must be different from the current password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // confirm matches
            if (newPwd != confirmPwd) {
                Toast.makeText(this, "Confirmation password does not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === Call API ===
            lifecycleScope.launch {
                try {
                    val response = userApi.changePassword(
                        ChangePasswordRequest(
                            currentPassword = oldPwd,
                            newPassword = newPwd
                        )
                    )
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ChangePasswordActivity,
                            "Password changed successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        // show backend message to user
                        val error = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@ChangePasswordActivity, error, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@ChangePasswordActivity,
                        "Network error: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
