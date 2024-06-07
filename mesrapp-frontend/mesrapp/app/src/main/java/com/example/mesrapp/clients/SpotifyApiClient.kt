package com.example.mesrapp.clients

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request

object SpotifyApiClient {
    fun createOkHttpClient(accessToken: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(request)
            })
            .build()
    }
}