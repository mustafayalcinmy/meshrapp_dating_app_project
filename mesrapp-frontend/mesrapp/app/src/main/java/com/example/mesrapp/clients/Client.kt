package com.example.mesrapp.clients
import com.example.mesrapp.services.ApiService
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Client {
    // Emulator kullandığı için sadece bu ip'de çalışır
    const val BASE_URL = "http://10.0.2.2:8080/"
    //const val BASE_URL = "https://63f0-176-219-107-245.ngrok-free.app/"

    val apiService: ApiService by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        retrofit.create(ApiService::class.java)
    }
}