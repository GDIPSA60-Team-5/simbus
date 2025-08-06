package iss.nus.edu.sg.appfiles.feature_navigatebar
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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


    }

    override fun onNavItemSelected(itemId: Int) {
        val fragment = when (itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_assistant -> AssistantFragment()
            R.id.nav_schedules -> SchedulesFragment()
            R.id.nav_menu -> MenuFragment()
            else -> null
        }

        fragment?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, it)
                .commit()
        }
    }
}
