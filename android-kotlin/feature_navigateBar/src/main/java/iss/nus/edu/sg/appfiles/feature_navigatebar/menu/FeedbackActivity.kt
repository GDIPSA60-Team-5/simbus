package iss.nus.edu.sg.appfiles.feature_navigatebar.menu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_navigatebar.FeedbackFragment
import iss.nus.edu.sg.appfiles.feature_navigatebar.databinding.ActivityFeedbackBinding

@AndroidEntryPoint
class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //back logic
        binding.btnBack.setOnClickListener {
            finish()
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, FeedbackFragment())
            .commit()
    }
}