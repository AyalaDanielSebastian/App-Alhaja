package com.example.alhaja.domain.model

data class LugarJoyeria(
    val id: Long,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val fechaMillis: Long,
    val fotoUri: String? = null
)
