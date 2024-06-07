package com.example.mesrapp.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

var accessToken: String? = null

class SpotifyAuth(private val activity: Activity) {

    fun login() {
        val REDIRECT_URI = "mesrapp://main"

        val builder =
            AuthorizationRequest.Builder(
                "6789d81b6a2a440f9654602e00083bdf",
                AuthorizationResponse.Type.TOKEN,
                REDIRECT_URI
            )

        builder.setScopes(arrayOf("user-read-private","user-top-read","user-read-email","user-read-currently-playing"))
        builder.setShowDialog(true)

        val request = builder.build()

        val intent = AuthorizationClient.createLoginActivityIntent(activity, request)
        activity.startActivityForResult(intent, REQUEST_CODE_SPOTIFY_LOGIN)
    }

    companion object {
        const val REQUEST_CODE_SPOTIFY_LOGIN = 1234455
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("SpotifyAuth", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode, data=$data")
        if (requestCode == REQUEST_CODE_SPOTIFY_LOGIN) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            // Handle the response here
            Log.d("SpotifyAuth", "Response: $response")
            accessToken = response.accessToken
            Log.d("SpotifyAuth", "Access Token: $accessToken")
        }
    }
}
