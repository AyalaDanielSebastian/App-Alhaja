package com.example.alhaja.domain.repository

import com.example.alhaja.domain.model.PreferenciasUsuario
import kotlinx.coroutines.flow.Flow

interface PreferenciasRepository {
    val preferencias: Flow<PreferenciasUsuario>

    suspend fun cambiarModoOscuro(activo: Boolean)

    suspend fun cambiarMoneda(moneda: String)
}
