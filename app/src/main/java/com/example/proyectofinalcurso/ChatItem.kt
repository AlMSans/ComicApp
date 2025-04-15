package com.example.proyectofinalcurso

data class ChatItem(
    val userId: String,         // ID del otro usuario
    val userName: String,       // Email o nombre para mostrar
    val lastMessage: String     // Último mensaje del chat
)
