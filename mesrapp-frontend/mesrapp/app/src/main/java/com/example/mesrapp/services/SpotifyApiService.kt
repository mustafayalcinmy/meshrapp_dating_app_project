package com.example.mesrapp.services

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request

class SpotifyApiService(private val okHttpClient: OkHttpClient) {
    private val gson = Gson()
    private val baseUrl = "https://api.spotify.com/v1"

    fun getCurrentUserProfile(accessToken: String): Map<*, *>? {
        val request = Request.Builder()
            .url("$baseUrl/me")
            .addHeader("Authorization", accessToken)
            .build()
        val response = okHttpClient.newCall(request).execute()
        return gson.fromJson(response.body?.string(), Map::class.java)
    }

    fun getCurrentUserCurrentlyPlaying(accessToken: String): Map<*, *>? {
        val request = Request.Builder()
            .url("$baseUrl/me/player/currently-playing")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        val response = okHttpClient.newCall(request).execute()
        return gson.fromJson(response.body?.string(), Map::class.java)
    }

    fun getCurrentUserTopTracks(accessToken: String): Map<*, *>? {
        val request = Request.Builder()
            .url("$baseUrl/me/top/tracks?limit=10&time_range=short_term")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        val response = okHttpClient.newCall(request).execute()
        return gson.fromJson(response.body?.string(), Map::class.java)
    }

    fun getTrack(trackId: String, accessToken: String): Map<*, *>? {
        val request = Request.Builder()
            .url("$baseUrl/tracks/$trackId")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        val response = okHttpClient.newCall(request).execute()
        return gson.fromJson(response.body?.string(), Map::class.java)
    }
}