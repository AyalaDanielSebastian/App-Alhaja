package com.example.alhaja.domain.model

data class Joya(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Double,
    val material: String,
    val imagenUrl: String = "",
    val esFavorita: Boolean = false
)
