package com.example.mesrapp.services

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mesrapp.R
import com.example.mesrapp.clients.Client
import com.example.mesrapp.pages.isSpotifyUserInitialized
import com.example.mesrapp.pages.spotifyUserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatchingService : Service() {
    private val HANDLER_INTERVAL = 20000L
    private lateinit var handler: Handler
    private val apiService = Client.apiService

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Service started")
        handler = Handler(mainLooper)
        handler.post(checkMatchesRunnable)


        return START_STICKY
    }

    private val checkMatchesRunnable = object : Runnable {
        override fun run() {
            if (!isSpotifyUserInitialized()) {
                return
            }
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = apiService.getMatches(spotifyUserProfile.get("id").toString(), "music")
                    response.execute().body()?.let { matches ->
                        var len = matches.size
                        if (len > 1) {
                            sendNotification("Yeni eşleşme!!", "Yeni bir eşleşmen var, gel ve onu keşfet!")
                        } else {
                            println("No new matches")
                        }
                        println(matches)
                    }
                    println("baslangic")
                    val isMatchedResponse = apiService.getIdMatches(spotifyUserProfile.get("id").toString())
                    val responseBody = isMatchedResponse.execute().body().toString()
                    println("bitis")
                    println(responseBody)
                    println(responseBody)
                    sendBroadcast(responseBody)
                    sendBroadcast(responseBody)

                    val isAcceptedUser = apiService.getAcceptedMatches(spotifyUserProfile.get("id").toString())
                    val responseBody_Accepted = isAcceptedUser.execute().body().toString()
                    sendBroadcast_Accepted(responseBody_Accepted)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            handler.postDelayed(this, HANDLER_INTERVAL)
        }
    }
    private fun sendBroadcast(responseBody: String?) {
        val intent = Intent("MATCHING_SERVICE_RESPONSE")
        intent.putExtra("response_body_matching", responseBody)
        applicationContext.sendBroadcast(intent)
    }

    private fun sendBroadcast_Accepted(responseBody: String?) {
        val intent = Intent("ACCEPTING_SERVICE_RESPONSE")
        intent.putExtra("response_body_accepting", responseBody)
        applicationContext.sendBroadcast(intent)
    }

    private fun sendNotification(title: String, message: String) {
        println(isAppInForeground())

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "MATCHING_SERVICE_CHANNEL"


        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        for (appProcess in appProcesses) {
            if (appProcess.processName == applicationContext.packageName && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }
}