package iss.nus.edu.sg.appfiles.feature_navigatebar.bar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import iss.nus.edu.sg.appfiles.feature_navigatebar.R

class SchedulesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return return inflater.inflate(R.layout.fragment_schedules, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}