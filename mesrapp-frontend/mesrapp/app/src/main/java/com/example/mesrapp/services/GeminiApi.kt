package com.example.mesrapp.services

import com.example.mesrapp.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

class GeminiApi {
    private val key = BuildConfig.GEMINI_API_KEY
    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = key,

    )

    val prompt = "Kelime sayısı maksimum 15, karakter sayısı maksimum 100 olacak şekilde bana bir hakkımda yazısı yazabilir misin? : "

    suspend fun generateText(aboutMe: String ): String? {
        val response = generativeModel.generateContent(prompt + aboutMe)
        println(response.promptFeedback)
        return response.text
    }
}