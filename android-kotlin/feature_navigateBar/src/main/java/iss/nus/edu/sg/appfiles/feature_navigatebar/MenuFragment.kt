package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import iss.nus.edu.sg.appfiles.feature_login.ui.LoginActivity
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager


class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_menu_content, container, false)

        //changePasswordRow onClickListener
        val changePasswordRow = view.findViewById<LinearLayout>(R.id.changePasswordRow)
        changePasswordRow.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        //aboutUsRow onClickListener
        val aboutUsRow = view.findViewById<LinearLayout>(R.id.aboutUsRow)
        aboutUsRow.setOnClickListener {
            val intent = Intent(requireContext(), AboutUsActivity::class.java)
            startActivity(intent)
        }

        //faqRow onClickListener
        val faqRow = view.findViewById<LinearLayout>(R.id.faqRow)
        faqRow.setOnClickListener {
            val intent = Intent(requireContext(), FAQActivity::class.java)
            startActivity(intent)
        }

        //changePasswordRow onClickListener
        val logoutRow = view.findViewById<LinearLayout>(R.id.logoutRow)
        logoutRow.setOnClickListener {
            val storageManager = SecureStorageManager(requireContext())
            storageManager.clearAll()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}

