package com.example.proyectofinalcurso

data class ChatMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val mensaje: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val senderProfileImageUrl: String? = null
)
