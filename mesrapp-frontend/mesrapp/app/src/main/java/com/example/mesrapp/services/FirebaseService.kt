package com.example.mesrapp.services

import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {
    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()

    fun sendMessage(chatId: String, senderId: String, receiverId: String, message: String) {
        val chat = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("chats").document(chatId).collection("messages").add(chat)
    }

    fun sendImageMessage(chatId: String, senderId: String, receiverId: String, message: String) {
        val chat = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "isImage" to true
        )
        firestore.collection("chats").document(chatId).collection("messages").add(chat)
    }


    fun getMessages(chatId: String, callback: (List<Message>) -> Unit) {
        firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { value, _ ->
                val messages = value?.documents?.map { doc ->
                    Message(
                        senderId = doc.getString("senderId") ?: "",
                        receiverId = doc.getString("receiverId") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        isImage = doc.getBoolean("isImage") ?: false
                    )
                } ?: emptyList()
                callback(messages)
            }
    }

    fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }
}

data class Message(
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long,
    val isImage: Boolean = false
)