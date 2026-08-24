package com.example.alhaja

import android.app.Application
import com.example.alhaja.data.hardware.ProveedorUbicacion
import com.example.alhaja.data.local.AlhajaDatabase
import com.example.alhaja.data.preferences.PreferenciasRepositoryImpl
import com.example.alhaja.data.remote.RedAlhaja
import com.example.alhaja.data.repository.JoyasRepositoryImpl
import com.example.alhaja.data.repository.LugaresRepositoryImpl
import com.example.alhaja.domain.repository.JoyasRepository
import com.example.alhaja.domain.repository.LugaresRepository
import com.example.alhaja.domain.repository.PreferenciasRepository

class AlhajaApplication : Application() {
    private val database by lazy { AlhajaDatabase.obtener(this) }

    val joyasRepository: JoyasRepository by lazy {
        JoyasRepositoryImpl(
            dao = database.favoritaDao(),
            api = RedAlhaja.api
        )
    }

    val lugaresRepository: LugaresRepository by lazy {
        LugaresRepositoryImpl(
            dao = database.lugarDao(),
            proveedorUbicacion = ProveedorUbicacion(this)
        )
    }

    val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepositoryImpl(this)
    }
}
