package com.example.mesrapp.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.mesrapp.clients.Client
import com.example.mesrapp.models.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OtherProfile : ComponentActivity() {

    val apiService = Client.apiService
    private val user: MutableState<User> = mutableStateOf(User("user", "1", "1", "Erkek", "May 18, 2020"))
    private var spotiId: String = ""

    @OptIn(DelicateCoroutinesApi::class)
    private fun getUser() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getUser(spotiId).execute()
                user.value = response.body()!!
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        spotiId = intent.getStringExtra("spotiId")!!
        getUser()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ProfileScreen(user = user)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}


