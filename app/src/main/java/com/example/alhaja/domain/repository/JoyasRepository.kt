package com.example.alhaja.domain.repository

import com.example.alhaja.domain.model.EstadoCatalogo
import com.example.alhaja.domain.model.Joya
import kotlinx.coroutines.flow.Flow

interface JoyasRepository {
    val estadoCatalogo: Flow<EstadoCatalogo>

    fun observarFavoritas(): Flow<List<Joya>>

    fun obtenerPorId(id: Int): Joya?

    suspend fun cargarCatalogo()

    suspend fun alternarFavorita(joya: Joya)
}
