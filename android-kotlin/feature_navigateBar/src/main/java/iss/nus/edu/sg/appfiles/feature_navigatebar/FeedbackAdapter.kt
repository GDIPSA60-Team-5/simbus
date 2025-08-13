package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedbackAdapter(private val feedbackList: List<FeedbackDTO>) :
    RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvFeedbackText: TextView = itemView.findViewById(R.id.tvFeedbackText)
        val tvTagList: TextView = itemView.findViewById(R.id.tvTagList)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_item, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.tvUserName.text = feedback.userName
        holder.tvFeedbackText.text = feedback.feedbackText
        holder.tvTagList.text = "label: ${feedback.tagList}"
        holder.ratingBar.rating = feedback.rating.toFloat()
    }

    override fun getItemCount(): Int = feedbackList.size
}


