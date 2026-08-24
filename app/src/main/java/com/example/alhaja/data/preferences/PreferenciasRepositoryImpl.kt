package com.example.alhaja.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.alhaja.domain.model.PreferenciasUsuario
import com.example.alhaja.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "preferencias_alhaja")

class PreferenciasRepositoryImpl(
    private val context: Context
) : PreferenciasRepository {

    private object Claves {
        val modoOscuro = booleanPreferencesKey("modo_oscuro")
        val moneda = stringPreferencesKey("moneda")
    }

    override val preferencias: Flow<PreferenciasUsuario> = context.dataStore.data.map { valores ->
        PreferenciasUsuario(
            modoOscuro = valores[Claves.modoOscuro] ?: false,
            moneda = valores[Claves.moneda] ?: "USD"
        )
    }

    override suspend fun cambiarModoOscuro(activo: Boolean) {
        context.dataStore.edit { it[Claves.modoOscuro] = activo }
    }

    override suspend fun cambiarMoneda(moneda: String) {
        context.dataStore.edit { it[Claves.moneda] = moneda }
    }
}
