package com.example.mesrapp.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class SpotifyListeningService : Service() {

    private val TAG = "SpotifyListeningService"
    private val BASE_URL = "https://api.spotify.com/v1"
    private val HANDLER_INTERVAL = 10000L // 15 seconds
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var handler: Handler
    private var accessToken: String? = null

    override fun onCreate() {
        super.onCreate()
        okHttpClient = OkHttpClient()
        handler = Handler(mainLooper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        accessToken = intent?.getStringExtra("access_token")
        handler.post(checkCurrentlyPlayingRunnable)
        return START_STICKY
    }

    private val checkCurrentlyPlayingRunnable = object : Runnable {
        override fun run() {
            try {
                accessToken?.let { token ->
                    val request = Request.Builder()
                        .url("$BASE_URL/me/player/currently-playing")
                        .addHeader("Authorization", "Bearer $token")
                        .build()

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = okHttpClient.newCall(request).execute()
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string()
                                sendBroadcast(responseBody)
                            } else {
                                Log.e(TAG, "Failed to fetch currently playing track: ${response.code}")
                            }
                        } catch (e: IOException) {
                            Log.e(TAG, "Error fetching currently playing track", e)
                        }
                    }
                }
            } finally {
                // Schedule the next execution after 5 seconds
                handler.postDelayed(this, HANDLER_INTERVAL)
            }
        }
    }

    private fun sendBroadcast(responseBody: String?) {
        val intent = Intent("CURRENTLY_PLAYING_TRACK")
        intent.putExtra("response_body", responseBody)
        applicationContext.sendBroadcast(intent)
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkCurrentlyPlayingRunnable)
    }
}
