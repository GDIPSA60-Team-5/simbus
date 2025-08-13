package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import iss.nus.edu.sg.appfiles.feature_navigatebar.databinding.FragmentFeedbackBinding
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FeedbackTag(val displayName: String) {
    CHATBOT("chatbot"),
    DIRECTION("direction"),
    PERFORMANCE("performance"),
    SCHEDULING("scheduling")
}

@AndroidEntryPoint
class FeedbackFragment : Fragment() {

    @Inject
    lateinit var feedbackApi: FeedbackApi

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    private val feedbackList = mutableListOf<FeedbackDTO>()
    private lateinit var adapter: FeedbackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView
        adapter = FeedbackAdapter(feedbackList)
        binding.rvFeedback.adapter = adapter
        binding.rvFeedback.layoutManager = LinearLayoutManager(context)

        // Spinner use enum label
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            FeedbackTag.values().map { it.displayName }
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTags.adapter = spinnerAdapter

        // load feedback
        loadFeedbacks()


        // submit feedback
        binding.btnSubmit.setOnClickListener {
            val feedbackText = binding.etFeedback.text.toString()
            if (feedbackText.isBlank()) {
                Toast.makeText(context, "Please input ur feedback", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedTagName = binding.spinnerTags.selectedItem.toString()
            val selectedTag = FeedbackTag.values().first { it.displayName == selectedTagName }

            val feedback = FeedbackDTO(
                userName = "user",
                feedbackText = feedbackText,
                rating = binding.ratingBar.rating.toInt(),
                tagList = selectedTag.name
            )

            submitFeedback(feedback)
        }
    }

    private fun loadFeedbacks() {
        lifecycleScope.launch {
            try {
                val list = feedbackApi.getAllFeedbacks()
                feedbackList.clear()
                feedbackList.addAll(list)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(context, "Loading failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitFeedback(feedback: FeedbackDTO) {
        lifecycleScope.launch {
            try {
                val created = feedbackApi.createFeedback(feedback)
                feedbackList.add(0, created)
                adapter.notifyItemInserted(0)
                binding.etFeedback.text.clear()
                binding.ratingBar.rating = 0f
                Toast.makeText(context, "Feedback submit successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = when(e) {
                    is retrofit2.HttpException -> "Submit failed: ${e.code()} ${e.response()?.errorBody()?.string()}"
                    else -> "Submit failed: ${e.localizedMessage}"
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
