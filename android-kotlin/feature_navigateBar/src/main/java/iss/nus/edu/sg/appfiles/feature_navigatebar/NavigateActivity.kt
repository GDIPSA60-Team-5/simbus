package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.feature_home.ui.HomeFragment
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_navigatebar.bar.MenuFragment
import iss.nus.edu.sg.appfiles.feature_navigatebar.databinding.ActivityNavigateBinding

@AndroidEntryPoint
class NavigateActivity : AppCompatActivity(), BottomNavFragment.OnNavItemSelectedListener {
    private lateinit var binding: ActivityNavigateBinding
    private var currentSelectedNavItemId = R.id.nav_home
    private var bottomNavFragment: BottomNavFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigateBinding.inflate(layoutInflater)
        setContentView(binding.root)

// Restore selected nav item if needed
        currentSelectedNavItemId = savedInstanceState?.getInt("currentSelectedNavItemId") ?: R.id.nav_home

        // Add bottom nav fragment
        bottomNavFragment = BottomNavFragment()
        supportFragmentManager.beginTransaction()
            .replace(binding.bottomNavContainer.id, bottomNavFragment!!)
            .commit()

        // Default fragment is HomeFragment
        if (savedInstanceState == null) {
            replaceMainFragment(MenuFragment())
        }
    }

    private fun replaceMainFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.mainFragmentContainer.id, fragment)
            .commit()
    }

    override fun onNavItemSelected(itemId: Int) {
        when (itemId) {
            R.id.nav_home -> replaceMainFragment(HomeFragment())
            R.id.nav_assistant -> startActivity(Intent(this, com.example.feature_chatbot.ui.ChatbotActivity::class.java))
            R.id.nav_schedules -> startActivity(Intent(this, iss.nus.edu.sg.feature_saveroute.SavedRoutesActivity::class.java))
            R.id.nav_menu -> replaceMainFragment(MenuFragment())
        }
    }
}
