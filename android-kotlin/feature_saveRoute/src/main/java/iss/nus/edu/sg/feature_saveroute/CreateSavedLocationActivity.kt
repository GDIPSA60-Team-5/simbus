package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.feature_saveroute.Data.savedLocationData
import iss.nus.edu.sg.feature_saveroute.databinding.CreateSavedLocationBinding
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateSavedLocationActivity : AppCompatActivity() {

    private lateinit var binding: CreateSavedLocationBinding

    @Inject lateinit var savedLocationStore: SavedLocationStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateSavedLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveButton.setOnClickListener {
            val name   = binding.nameInput.text?.toString()?.trim().orEmpty()
            val postal = binding.postalInput.text?.toString()?.trim().orEmpty()

            var hasError = false
            if (name.isEmpty()) {
                binding.nameInput.error = "Enter a name"
                hasError = true
            } else {
                binding.nameInput.error = null
            }

            if (postal.length != 6 || !postal.all { it.isDigit() }) {
                binding.postalInput.error = "Enter a valid 6-digit postal code"
                hasError = true
            } else {
                binding.postalInput.error = null
            }
            if (hasError) return@setOnClickListener

            lifecycleScope.launch {
                val existing = savedLocationStore.load()
                val exists = existing.any { it.name.trim().equals(name, ignoreCase = true) }
                if (exists) {
                    binding.nameInput.error = "Name already exists"
                    return@launch
                }

                val result = savedLocationStore.add(savedLocationData(name, postal))
                result.onSuccess {
                    val data = Intent().apply {
                        putExtra("target", intent.getStringExtra("target"))
                        putExtra("savedName", name)
                        putExtra("savedPostal", postal)
                    }
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }.onFailure { e ->
                    Toast.makeText(
                        this@CreateSavedLocationActivity,
                        "Failed to save: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}