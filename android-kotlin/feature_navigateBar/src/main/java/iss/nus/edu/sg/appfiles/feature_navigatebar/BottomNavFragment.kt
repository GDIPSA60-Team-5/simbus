package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class BottomNavFragment : Fragment() {

    interface OnNavItemSelectedListener {
        fun onNavItemSelected(itemId: Int)
    }

    private var listener: OnNavItemSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNavItemSelectedListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_nav, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnHome = view.findViewById<LinearLayout>(R.id.btnHome)
        val btnAssistant = view.findViewById<LinearLayout>(R.id.btnAssistant)
        val btnSchedules = view.findViewById<LinearLayout>(R.id.btnSchedules)
        val btnMenu = view.findViewById<LinearLayout>(R.id.btnMenu)
        val btnMic = view.findViewById<ImageView>(R.id.btnMic)

        val navButtons = listOf(btnHome, btnAssistant, btnSchedules, btnMenu)

        fun resetIconsAndText() {
            for (button in navButtons) {
                val icon = button.getChildAt(0) as ImageView
                val label = button.getChildAt(1) as TextView

                when (button.id) {
                    R.id.btnHome -> icon.setImageResource(R.drawable.ic_home)
                    R.id.btnAssistant -> icon.setImageResource(R.drawable.ic_assistant)
                    R.id.btnSchedules -> icon.setImageResource(R.drawable.ic_schedule)
                    R.id.btnMenu -> icon.setImageResource(R.drawable.ic_menu)
                }
                label.setTextColor(resources.getColor(R.color.default_text, null))
            }
        }

        fun activateButton(button: LinearLayout, iconRes: Int) {
            val icon = button.getChildAt(0) as ImageView
            val label = button.getChildAt(1) as TextView
            icon.setImageResource(iconRes)
            label.setTextColor(resources.getColor(R.color.selected_text, null))
        }

        btnHome.setOnClickListener {
            resetIconsAndText()
            activateButton(btnHome, R.drawable.ic_home_click)
            listener?.onNavItemSelected(R.id.nav_home)
        }

        btnAssistant.setOnClickListener {
            resetIconsAndText()
            activateButton(btnAssistant, R.drawable.ic_assistant_click)
            listener?.onNavItemSelected(R.id.nav_assistant)
        }

        btnSchedules.setOnClickListener {
            resetIconsAndText()
            activateButton(btnSchedules, R.drawable.ic_schedule_click)
            listener?.onNavItemSelected(R.id.nav_schedules)
        }

        btnMenu.setOnClickListener {
            resetIconsAndText()
            activateButton(btnMenu, R.drawable.ic_menu_click)
            listener?.onNavItemSelected(R.id.nav_menu)
        }

        btnMic.setOnClickListener {
            Toast.makeText(requireContext(), "Mic Clicked", Toast.LENGTH_SHORT).show()
        }
    }

}

