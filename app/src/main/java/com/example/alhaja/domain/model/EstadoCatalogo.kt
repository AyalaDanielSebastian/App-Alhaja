package com.example.alhaja.domain.model

sealed interface EstadoCatalogo {
    data object Cargando : EstadoCatalogo
    data class Exito(val joyas: List<Joya>) : EstadoCatalogo
    data class Error(val mensaje: String) : EstadoCatalogo
}
