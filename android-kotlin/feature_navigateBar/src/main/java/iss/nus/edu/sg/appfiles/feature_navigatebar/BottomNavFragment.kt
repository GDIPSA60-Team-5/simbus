package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import iss.nus.edu.sg.appfiles.feature_navigatebar.databinding.FragmentBottomNavBinding

class BottomNavFragment : Fragment() {

    interface OnNavItemSelectedListener {
        fun onNavItemSelected(itemId: Int)
    }

    private var listener: OnNavItemSelectedListener? = null
    private var _binding: FragmentBottomNavBinding? = null
    private val binding get() = _binding!!
    private var currentActiveButton: LinearLayout? = null

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
        savedInstanceState: Bundle?
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

        // --- Animation helpers ---
        fun animateIconChange(icon: ImageView, newResId: Int, isSelected: Boolean) {
            val scaleDownX = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 0.7f).apply {
                duration = 150
                interpolator = AccelerateDecelerateInterpolator()
            }
            val scaleDownY = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 0.7f).apply {
                duration = 150
                interpolator = AccelerateDecelerateInterpolator()
            }
            val fadeOut = ObjectAnimator.ofFloat(icon, "alpha", 1f, 0f).apply { duration = 150 }

            val scaleUpX = ObjectAnimator.ofFloat(
                icon,
                "scaleX",
                0.7f,
                if (isSelected) 1.2f else 1f
            ).apply {
                duration = 200
                interpolator = if (isSelected) OvershootInterpolator(1.5f) else AccelerateDecelerateInterpolator()
            }
            val scaleUpY = ObjectAnimator.ofFloat(
                icon,
                "scaleY",
                0.7f,
                if (isSelected) 1.2f else 1f
            ).apply {
                duration = 200
                interpolator = if (isSelected) OvershootInterpolator(1.5f) else AccelerateDecelerateInterpolator()
            }
            val fadeIn = ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f).apply { duration = 200 }

            fadeOut.addUpdateListener {
                if (it.animatedFraction > 0.5f) icon.setImageResource(newResId)
            }

            AnimatorSet().apply {
                play(scaleDownX).with(scaleDownY).with(fadeOut)
                play(scaleUpX).with(scaleUpY).with(fadeIn).after(scaleDownX)
                start()
            }
        }

        fun animateTextChange(label: TextView, color: Int) {
            val slideUp = ObjectAnimator.ofFloat(label, "translationY", 0f, -20f).apply {
                duration = 150
                interpolator = AccelerateDecelerateInterpolator()
            }
            val slideDown = ObjectAnimator.ofFloat(label, "translationY", -20f, 0f).apply {
                duration = 200
                interpolator = BounceInterpolator()
            }
            val fadeOut = ObjectAnimator.ofFloat(label, "alpha", 1f, 0f).apply { duration = 150 }
            val fadeIn = ObjectAnimator.ofFloat(label, "alpha", 0f, 1f).apply { duration = 200 }

            slideUp.addUpdateListener {
                if (it.animatedFraction > 0.5f) label.setTextColor(color)
            }

            AnimatorSet().apply {
                play(slideUp).with(fadeOut)
                play(slideDown).with(fadeIn).after(slideUp)
                start()
            }
        }

        fun activateButton(button: LinearLayout) {
            val icon = button.getChildAt(0) as? ImageView
            val label = button.getChildAt(1) as? TextView
            icon?.let { animateIconChange(it, selectedIcons[button.id] ?: 0, true) }
            label?.let { animateTextChange(it, selectedTextColor) }
            currentActiveButton = button
        }

        fun resetButton(button: LinearLayout) {
            val icon = button.getChildAt(0) as? ImageView
            val label = button.getChildAt(1) as? TextView
            icon?.let { animateIconChange(it, defaultIcons[button.id] ?: 0, false) }
            label?.let { animateTextChange(it, defaultTextColor) }
        }

        fun onNavClicked(button: LinearLayout, navId: Int) {
            if (currentActiveButton != button) {
                currentActiveButton?.let { resetButton(it) }
                activateButton(button)
                listener?.onNavItemSelected(navId)
            }
        }

        // Initialize first active button
        onNavClicked(binding.btnHome, R.id.nav_home)

        // Set click listeners
        navButtons.forEach { button ->
            button.setOnClickListener {
                it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
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

        // Mic button animation
        binding.btnMic.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            val pulseScale = ObjectAnimator.ofFloat(it, "scaleX", 1f, 1.3f, 1f).apply {
                duration = 300
                interpolator = BounceInterpolator()
            }
            val pulseScaleY = ObjectAnimator.ofFloat(it, "scaleY", 1f, 1.3f, 1f).apply {
                duration = 300
                interpolator = BounceInterpolator()
            }
            val pulseRotation = ObjectAnimator.ofFloat(it, "rotation", 0f, 360f).apply {
                duration = 600
                interpolator = AccelerateDecelerateInterpolator()
            }

            AnimatorSet().apply {
                play(pulseScale).with(pulseScaleY).with(pulseRotation)
                start()
            }
        }

        // Entrance animation for nav bar
        binding.root.apply {
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(100)
                .setInterpolator(OvershootInterpolator())
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Public method to programmatically select a nav item
    fun selectNavItem(navId: Int) {
        val button = when (navId) {
            R.id.nav_home -> binding.btnHome
            R.id.nav_assistant -> binding.btnAssistant
            R.id.nav_schedules -> binding.btnSchedules
            R.id.nav_menu -> binding.btnMenu
            else -> null
        }
        button?.let {
            if (currentActiveButton != button) {
                currentActiveButton?.let { resetButton(it) }
                activateButton(button)
                listener?.onNavItemSelected(navId)
            }
        }
    }

    private fun onNavClicked(button: LinearLayout, navId: Int) {
        if (currentActiveButton != button) {
            currentActiveButton?.let { resetButton(it) }
            activateButton(button)
            listener?.onNavItemSelected(navId)
        }
    }

    private fun resetButton(button: LinearLayout) {
        val icon = button.getChildAt(0) as? ImageView
        val label = button.getChildAt(1) as? TextView
        icon?.let { animateIconChange(it, defaultIcons[button.id] ?: 0, false) }
        label?.let { animateTextChange(it, defaultTextColor) }
    }

    private fun activateButton(button: LinearLayout) {
        val icon = button.getChildAt(0) as? ImageView
        val label = button.getChildAt(1) as? TextView
        icon?.let { animateIconChange(it, selectedIcons[button.id] ?: 0, true) }
        label?.let { animateTextChange(it, selectedTextColor) }
        currentActiveButton = button
    }

    private fun animateTextChange(label: TextView, color: Int) {
        val slideUp = ObjectAnimator.ofFloat(label, "translationY", 0f, -20f).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
        }
        val slideDown = ObjectAnimator.ofFloat(label, "translationY", -20f, 0f).apply {
            duration = 200
            interpolator = BounceInterpolator()
        }
        val fadeOut = ObjectAnimator.ofFloat(label, "alpha", 1f, 0f).apply { duration = 150 }
        val fadeIn = ObjectAnimator.ofFloat(label, "alpha", 0f, 1f).apply { duration = 200 }

        slideUp.addUpdateListener {
            if (it.animatedFraction > 0.5f) label.setTextColor(color)
        }

        AnimatorSet().apply {
            play(slideUp).with(fadeOut)
            play(slideDown).with(fadeIn).after(slideUp)
            start()
        }
    }

    private fun animateIconChange(icon: ImageView, newResId: Int, isSelected: Boolean) {
        val scaleDownX = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 0.7f).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleDownY = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 0.7f).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()
        }
        val fadeOut = ObjectAnimator.ofFloat(icon, "alpha", 1f, 0f).apply { duration = 150 }

        val scaleUpX = ObjectAnimator.ofFloat(
            icon,
            "scaleX",
            0.7f,
            if (isSelected) 1.2f else 1f
        ).apply {
            duration = 200
            interpolator = if (isSelected) OvershootInterpolator(1.5f) else AccelerateDecelerateInterpolator()
        }
        val scaleUpY = ObjectAnimator.ofFloat(
            icon,
            "scaleY",
            0.7f,
            if (isSelected) 1.2f else 1f
        ).apply {
            duration = 200
            interpolator = if (isSelected) OvershootInterpolator(1.5f) else AccelerateDecelerateInterpolator()
        }
        val fadeIn = ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f).apply { duration = 200 }

        fadeOut.addUpdateListener {
            if (it.animatedFraction > 0.5f) icon.setImageResource(newResId)
        }

        AnimatorSet().apply {
            play(scaleDownX).with(scaleDownY).with(fadeOut)
            play(scaleUpX).with(scaleUpY).with(fadeIn).after(scaleDownX)
            start()
        }
    }
}