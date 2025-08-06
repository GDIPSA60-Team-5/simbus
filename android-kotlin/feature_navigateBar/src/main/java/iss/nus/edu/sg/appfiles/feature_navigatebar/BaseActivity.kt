package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigate) // 布局中有 bottomNavContainer 和 mainFragmentContainer

        supportFragmentManager.beginTransaction()
            .replace(R.id.bottomNavContainer, BottomNavFragment())
            .commit()
    }

    fun setMainContent(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()
    }
}
