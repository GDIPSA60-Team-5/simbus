package iss.nus.edu.sg.feature_saveroute

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.feature_saveroute.Data.savedLocationData
import iss.nus.edu.sg.feature_saveroute.databinding.CreateSavedLocationBinding

class CreateSavedLocationActivity : AppCompatActivity() {

    private lateinit var binding: CreateSavedLocationBinding

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

            val lowerCased = name.lowercase()
            val list = SavedLocation.load(this)

            val exists = list.any{it.name.trim().lowercase()==lowerCased}
            if(exists) {
                binding.nameInput.error = "Name already exisits"
                return@setOnClickListener
            }

            SavedLocation.add(this, savedLocationData(name, postal))
            val data = Intent().apply {
                putExtra("target", intent.getStringExtra("target"))
                putExtra("savedName", name)
                putExtra("savedPostal", postal)
            }

            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}