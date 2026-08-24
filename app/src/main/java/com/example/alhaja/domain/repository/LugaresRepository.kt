package com.example.alhaja.domain.repository

import com.example.alhaja.domain.model.LugarJoyeria
import com.example.alhaja.domain.model.ResultadoUbicacion
import kotlinx.coroutines.flow.Flow

interface LugaresRepository {
    fun observarLugares(): Flow<List<LugarJoyeria>>

    suspend fun guardarUbicacionActual(): ResultadoUbicacion

    suspend fun guardarCaptura(fotoUri: String): ResultadoUbicacion

    suspend fun eliminar(id: Long)
}
