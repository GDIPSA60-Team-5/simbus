package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FAQActivity:AppCompatActivity() {
    private lateinit var backButton: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)
        backButton = findViewById(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }
    }
}