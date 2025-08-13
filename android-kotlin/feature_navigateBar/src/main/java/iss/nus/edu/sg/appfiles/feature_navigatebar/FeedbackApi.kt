package iss.nus.edu.sg.appfiles.feature_navigatebar

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeedbackApi {
    @GET("/api/user/feedbacks")
    suspend fun getAllFeedbacks(): List<FeedbackDTO>

    @POST("/api/user/feedbacks")
    suspend fun createFeedback(@Body feedback: FeedbackDTO): FeedbackDTO
}