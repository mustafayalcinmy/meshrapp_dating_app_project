package com.example.mesrapp

import androidx.lifecycle.ViewModel
import com.example.mesrapp.services.FirebaseService
import com.example.mesrapp.services.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> get() = _messages

    fun loadMessages(chatId: String) {
        FirebaseService.getMessages(chatId) { messages ->
            _messages.update { messages }
        }
    }

    fun sendMessage(chatId: String, senderId: String, receiverId: String, message: String) {
        FirebaseService.sendMessage(chatId, senderId, receiverId, message)
    }

    fun sendImageMessage(chatId: String, senderId: String, receiverId: String, message: String) {
        FirebaseService.sendImageMessage(chatId, senderId, receiverId, message)
    }
}