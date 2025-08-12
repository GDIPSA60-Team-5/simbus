package com.example.network.di

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    companion object {
        private val WHITELIST = listOf("/api/auth/login", "/api/auth/register")
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        if (WHITELIST.any { path.startsWith(it) }) {
            return chain.proceed(request)
        }

        val token = tokenProvider()
        val newRequest = token?.let {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $it")
                .build()
        } ?: request

        return chain.proceed(newRequest)
    }
}
