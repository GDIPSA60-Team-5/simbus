package iss.nus.edu.sg.appfiles.feature_navigatebar.menu

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.appfiles.feature_navigatebar.R

class AboutUsActivity: AppCompatActivity() {
    private lateinit var backButton: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        backButton = findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }
    }
}