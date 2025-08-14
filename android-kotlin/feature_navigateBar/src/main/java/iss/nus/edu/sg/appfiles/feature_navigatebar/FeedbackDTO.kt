package iss.nus.edu.sg.appfiles.feature_navigatebar


data class FeedbackDTO(
    val id:String?=null,
    val userName: String,
    val userId:String?=null,
    val feedbackText: String,
    val rating: Int,
    val tagList: String,
    val submittedAt: String?=null
)
