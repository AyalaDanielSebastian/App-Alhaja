package com.example.alhaja.domain.model

sealed interface ResultadoUbicacion {
    data object Guardado : ResultadoUbicacion
    data object SinPermiso : ResultadoUbicacion
    data object NoDisponible : ResultadoUbicacion
}
