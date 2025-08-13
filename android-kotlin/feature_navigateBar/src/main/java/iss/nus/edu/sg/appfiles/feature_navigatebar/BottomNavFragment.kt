package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import iss.nus.edu.sg.appfiles.feature_navigatebar.databinding.FragmentBottomNavBinding

class BottomNavFragment : Fragment() {

    interface OnNavItemSelectedListener {
        fun onNavItemSelected(itemId: Int)
    }

    private var listener: OnNavItemSelectedListener? = null
    private var _binding: FragmentBottomNavBinding? = null
    private val binding get() = _binding!!

    private val defaultTextColor by lazy {
        ContextCompat.getColor(requireContext(), R.color.default_text)
    }
    private val selectedTextColor by lazy {
        ContextCompat.getColor(requireContext(), R.color.selected_text)
    }

    private val defaultIcons = mapOf(
        R.id.btnHome to R.drawable.ic_home,
        R.id.btnAssistant to R.drawable.ic_assistant,
        R.id.btnSchedules to R.drawable.ic_schedule,
        R.id.btnMenu to R.drawable.ic_menu,
    )

    private val selectedIcons = mapOf(
        R.id.btnHome to R.drawable.ic_home_click,
        R.id.btnAssistant to R.drawable.ic_assistant_click,
        R.id.btnSchedules to R.drawable.ic_schedule_click,
        R.id.btnMenu to R.drawable.ic_menu_click,
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnNavItemSelectedListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBottomNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navButtons = listOf(
            binding.btnHome,
            binding.btnAssistant,
            binding.btnSchedules,
            binding.btnMenu,
        )

        fun resetNav() {
            navButtons.forEach { button ->
                val icon = button.getChildAt(0) as? android.widget.ImageView
                val label = button.getChildAt(1) as? android.widget.TextView
                icon?.setImageResource(defaultIcons[button.id] ?: 0)
                label?.setTextColor(defaultTextColor)
            }
        }

        fun activateButton(button: android.widget.LinearLayout) {
            val icon = button.getChildAt(0) as? android.widget.ImageView
            val label = button.getChildAt(1) as? android.widget.TextView
            icon?.setImageResource(selectedIcons[button.id] ?: 0)
            label?.setTextColor(selectedTextColor)
        }

        fun onNavClicked(button: android.widget.LinearLayout, navId: Int) {
            resetNav()
            activateButton(button)
            listener?.onNavItemSelected(navId)
        }

        resetNav()
        onNavClicked(binding.btnHome, R.id.nav_home)

        navButtons.forEach { button ->
            button.setOnClickListener {
                val navId = when (button.id) {
                    R.id.btnHome -> R.id.nav_home
                    R.id.btnAssistant -> R.id.nav_assistant
                    R.id.btnSchedules -> R.id.nav_schedules
                    R.id.btnMenu -> R.id.nav_menu
                    else -> 0
                }
                if (navId != 0) onNavClicked(button, navId)
            }
        }

        binding.btnMic.setOnClickListener {
            Toast.makeText(requireContext(), "Mic Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
