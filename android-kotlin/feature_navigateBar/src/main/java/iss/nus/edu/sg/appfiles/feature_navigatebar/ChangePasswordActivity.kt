package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var currentPasswordEdit: EditText
    private lateinit var newPasswordEdit: EditText
    private lateinit var confirmPasswordEdit: EditText
    private lateinit var submitButton: Button
    private lateinit var backButton: ImageView

    private lateinit var secureStorageManager: SecureStorageManager
    private val client = OkHttpClient()
    private val baseUrl = "http://10.0.2.2:8080/api/user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // init ui
        currentPasswordEdit = findViewById(R.id.currentPassword)
        newPasswordEdit = findViewById(R.id.newPassword)
        confirmPasswordEdit = findViewById(R.id.confirmPassword)
        submitButton = findViewById(R.id.submitBtn)
        backButton = findViewById(R.id.btnBack)

        // init secureStorage manager
        secureStorageManager = SecureStorageManager(this)
        val token = secureStorageManager.getToken()

        backButton.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val currentPassword = currentPasswordEdit.text.toString().trim()
            val newPassword = newPasswordEdit.text.toString().trim()
            val confirmPassword = confirmPasswordEdit.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "please fill all the part", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "The passwords you entered don't match up.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (token == null) {
                Toast.makeText(this, "Not logged in yet. Please log in first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendChangePasswordRequest(token, currentPassword, newPassword)
        }
    }

    private fun sendChangePasswordRequest(token: String, currentPassword: String, newPassword: String) {
        val json = JSONObject().apply {
            put("currentPassword", currentPassword)
            put("newPassword", newPassword)
        }

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("$baseUrl/change-password")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ChangePasswordActivity, "Network request failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChangePasswordActivity, "Password modification successful", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = response.body?.string() ?: "Password modification failed"
                        Toast.makeText(this@ChangePasswordActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
