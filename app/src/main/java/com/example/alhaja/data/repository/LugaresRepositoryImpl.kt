package com.example.alhaja.data.repository

import com.example.alhaja.data.hardware.ProveedorUbicacion
import com.example.alhaja.data.local.LugarDao
import com.example.alhaja.data.local.LugarEntity
import com.example.alhaja.domain.model.LugarJoyeria
import com.example.alhaja.domain.model.ResultadoUbicacion
import com.example.alhaja.domain.repository.LugaresRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LugaresRepositoryImpl(
    private val dao: LugarDao,
    private val proveedorUbicacion: ProveedorUbicacion
) : LugaresRepository {

    override fun observarLugares(): Flow<List<LugarJoyeria>> =
        dao.observarLugares().map { lista -> lista.map { it.toDomain() } }

    override suspend fun guardarUbicacionActual(): ResultadoUbicacion {
        if (!proveedorUbicacion.tienePermiso()) {
            return ResultadoUbicacion.SinPermiso
        }
        val coordenadas = proveedorUbicacion.obtenerCoordenadas()
            ?: return ResultadoUbicacion.NoDisponible
        dao.guardar(
            LugarEntity(
                nombre = "Joyería visitada · ${fechaLegible()}",
                latitud = coordenadas.latitud,
                longitud = coordenadas.longitud,
                fechaMillis = System.currentTimeMillis()
            )
        )
        return ResultadoUbicacion.Guardado
    }

    override suspend fun guardarCaptura(fotoUri: String): ResultadoUbicacion {
        val coordenadas = if (proveedorUbicacion.tienePermiso()) {
            proveedorUbicacion.obtenerCoordenadas()
        } else {
            null
        }
        val fecha = fechaLegible()
        dao.guardar(
            LugarEntity(
                nombre = if (coordenadas != null) "Vitrina · $fecha" else "Foto de vitrina · $fecha",
                latitud = coordenadas?.latitud ?: 0.0,
                longitud = coordenadas?.longitud ?: 0.0,
                fechaMillis = System.currentTimeMillis(),
                fotoUri = fotoUri
            )
        )
        return ResultadoUbicacion.Guardado
    }

    override suspend fun eliminar(id: Long) {
        dao.eliminar(id)
    }
}

private fun fechaLegible(): String =
    SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())

private fun LugarEntity.toDomain() = LugarJoyeria(
    id = id,
    nombre = nombre,
    latitud = latitud,
    longitud = longitud,
    fechaMillis = fechaMillis,
    fotoUri = fotoUri
)
