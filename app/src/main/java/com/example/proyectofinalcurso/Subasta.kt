package com.example.proyectofinalcurso

data class Subasta(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val imagenUrl: String = "",
    val precioInicial: Double = 0.0,
    val mejorOferta: Double = 0.0,
    val mejorPostor: String = "",
    val nombrePostor: String = "",
    val propietarioId: String = "",
    val cerrada: Boolean = false
)