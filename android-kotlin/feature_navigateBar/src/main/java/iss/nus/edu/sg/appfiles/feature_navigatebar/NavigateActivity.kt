package iss.nus.edu.sg.appfiles.feature_navigatebar
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.appfiles.feature_navigatebar.bar.HomeFragment
import iss.nus.edu.sg.appfiles.feature_navigatebar.bar.MenuFragment
import iss.nus.edu.sg.appfiles.feature_navigatebar.bar.SchedulesFragment
import iss.nus.edu.sg.appfiles.feature_navigatebar.databinding.ActivityNavigateBinding

class NavigateActivity : AppCompatActivity(), BottomNavFragment.OnNavItemSelectedListener  {
    private lateinit var binding: ActivityNavigateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // add navigate bar
        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomNavContainer, BottomNavFragment())
            .commit()

        // default is HomeFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, HomeFragment())
                .commit()
        }

    }

    override fun onNavItemSelected(itemId: Int) {
        when (itemId) {
            R.id.nav_home -> {
                val fragment = HomeFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragmentContainer, fragment)
                    .commit()
            }
            R.id.nav_assistant -> {
                val intent = Intent(this, com.example.feature_chatbot.ui.ChatbotActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_schedules -> {
                val fragment = SchedulesFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragmentContainer, fragment)
                    .commit()
            }
            R.id.nav_menu -> {
                val fragment = MenuFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainFragmentContainer, fragment)
                    .commit()
            }
        }
    }

}
