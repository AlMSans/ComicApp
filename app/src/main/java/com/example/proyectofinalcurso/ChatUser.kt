package com.example.proyectofinalcurso

data class ChatUser(
    val uid: String,
    val name: String,
    val profileImageUrl: String? = null
)