package iss.nus.edu.sg.appfiles.feature_navigatebar.bar

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager
import iss.nus.edu.sg.appfiles.feature_navigatebar.menu.AboutUsActivity
import iss.nus.edu.sg.appfiles.feature_navigatebar.menu.ChangePasswordActivity
import iss.nus.edu.sg.appfiles.feature_navigatebar.menu.FAQActivity
import iss.nus.edu.sg.appfiles.feature_navigatebar.R

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_menu_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get username from sharePreference
        val username = view.findViewById<TextView>(R.id.username)
        val storageManager = SecureStorageManager(requireContext())
        username.text = storageManager.getUsername()

        // init changePasswordRow
        view.findViewById<LinearLayout>(R.id.changePasswordRow).setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        // init aboutUsRow
        view.findViewById<LinearLayout>(R.id.aboutUsRow).setOnClickListener {
            startActivity(Intent(requireContext(), AboutUsActivity::class.java))
        }

        // init FAQRow
        view.findViewById<LinearLayout>(R.id.faqRow).setOnClickListener {
            startActivity(Intent(requireContext(), FAQActivity::class.java))
        }

        // init logout
        view.findViewById<LinearLayout>(R.id.logoutRow).setOnClickListener {
            handleLogout(storageManager)
        }

        //init mute notification
        val switch1 = view.findViewById<SwitchMaterial>(R.id.switch1)
        handleMuteNotification(switch1)

    }

    private fun handleMuteNotification(switch1: SwitchMaterial) {
        switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this@MenuFragment.requireContext(), "mute", Toast.LENGTH_SHORT)
                    .show()
                switch1.thumbTintList = ColorStateList.valueOf("#D17A4B".toColorInt())
                // TODO: 开启静音逻辑
            } else {
                Toast.makeText(this@MenuFragment.requireContext(), "not mute", Toast.LENGTH_SHORT)
                    .show()
                switch1.thumbTintList = ColorStateList.valueOf("#AA9D96".toColorInt())
                // TODO: 关闭静音逻辑
            }
        }
    }

    private fun handleLogout(storageManager: SecureStorageManager) {
        storageManager.clearAll()
        val intent = Intent()
        intent.setClassName(
            requireContext().packageName,
            "com.example.busappkotlin.ui.MainActivity"
        )
        requireContext().startActivity(intent)
//        startActivity(Intent(requireContext(), LoginActivity::class.java))
    }
}