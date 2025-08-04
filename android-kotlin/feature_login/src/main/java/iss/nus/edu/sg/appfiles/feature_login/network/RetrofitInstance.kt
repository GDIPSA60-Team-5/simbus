package iss.nus.edu.sg.appfiles.feature_login.network;

import iss.nus.edu.sg.appfiles.feature_login.api.AuthApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
val api: AuthApi by lazy {
    Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // Android 模拟器访问本机服务
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
}
}

